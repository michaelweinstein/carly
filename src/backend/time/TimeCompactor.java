package backend.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import backend.database.StorageService;
import data.Assignment;
import data.ITimeBlockable;
import data.UnavailableBlock;


public class TimeCompactor {
	
	private static final int MILLIS_IN_DAY = 86400000;
	
	/**
	 * This function currently compacts all movable blocks in the Date range [start, end]
	 * in order to prevent "external fragmentation" along the client's time stream
	 * @param allBlocks A List of ITimeBlockables sorted by start Date
	 * @param start The Date at which to begin compaction of the parameter List
	 * @param end The Date at which to end compaction of the parameter List
	 * @param lastTimePlaced A reference to a Date where the last successful insertion into
	 * 						"allBlocks" was
	 * @return a Date indicating where the block corresponding to the "lastTimePlaced" Date
	 * 			has been moved -- so upon exiting this function, the reference to lastTimePlaced
	 *			can be reset to the output of this function
	 */
	public static Date compact(List<ITimeBlockable> allBlocks, Date start, Date end, 
			Date lastTimePlaced) {
		
		if(allBlocks.size() == 0)
			return lastTimePlaced;
		
		int startInd = TimeUtilities.indexOfFitLocn(allBlocks, start);
		Date timeToPushTo = allBlocks.get(startInd).getEnd();
		
		for(int i = startInd + 1; i < allBlocks.size(); ++i) {
			
			ITimeBlockable block = allBlocks.get(i);
			
			//If a block goes past the given "end" parameter, stop compacting and return
			if(block.getEnd().getTime() > end.getTime())
				return lastTimePlaced;
			
			//If a block is unmovable, use the end of that block as the time to push to
			//for safety purposes (i.e. when compacting future blocks, it is not known
			//how much space there is between "block" and the last item that was compacted)
			if(!block.isMovable()) {
				timeToPushTo = block.getEnd();
				continue;
			}
			
			//Compact the block backwards in time
			long delta = block.getLength();
			Date newEnd = new Date(timeToPushTo.getTime() + delta);
			
			//Reset the value in time that the "lastTimePlaced" reference points
			//to if it is encountered
			if(block.getStart().getTime() == lastTimePlaced.getTime()) {
				lastTimePlaced.setTime(timeToPushTo.getTime());
			}
			
			block.setStart(new Date(timeToPushTo.getTime()));
			block.setEnd(new Date(newEnd.getTime()));
			
			//Reset the timeToPushTo pointer to be the newEnd object
			timeToPushTo = newEnd;
		}
	
		return lastTimePlaced;
	}

	//This function uses several human-friendly heuristics to de-compact a schedule
	//to allow (1) breaks between work, (2) a variety of assignments in succession,
	//(3) work during the preferred time of day
	/**
	 * This function uses several human-friendly heuristics to de-compact a schedule
	 * to allow (1) breaks between work, (2) a variety of assignments in succession,
	 * (3) work during the preferred time of day
	 * @param allBlocks A List of ITimeBlockables sorted by start Date
	 * @param start The Date at which to begin de-compaction of the parameter List
	 * @param end The Date at which to end de-compaction of the parameter List
	 */
	public static void decompact(List<ITimeBlockable> allBlocks, Date start, Date end) {

		//Iterate over blocks.  If a block is movable, look at its preferred time of day.
		//See if there is a block already there.  If not, try to move it there.  If so,
		//try to switch (in the case of either Pareto improvement or indifference on one end.)

		long timeUnavailableMillis = 0;
		long freeTimeBankMillis = 0;
		long avgFreeTimeMillis = 0;
		
		//This ArrayList contains the items to-be-de-compacted from the original list
		//(i.e. all items in the range [start, end])
		List<ITimeBlockable> asgnBlocks = new ArrayList<ITimeBlockable>();
		
		List<UnavailableBlock> unavailables = StorageService.getAllUnavailableBlocksWithinRange(start, end);
		List<ITimeBlockable> unavailList = new ArrayList<ITimeBlockable>(unavailables.size());
		//TODO: This is a temp solution because of type problems
		for(int i = 0; i < unavailables.size(); ++i)
			unavailList.add(unavailables.get(i));
		
		Date timeToStartFrom = new Date(end.getTime() - MILLIS_IN_DAY);
		//TODO: ^^ this timeToStartFrom can be tweaked


		//The range of indices with which we are concerned in the "allBlocks" list
		int startInd = TimeUtilities.indexOfFitLocn(allBlocks, start);
		int endInd = TimeUtilities.indexOfFitLocn(allBlocks, end);
		
		//This ArrayList will contain the items in sorted order as they are de-compacted.
		List<ITimeBlockable> underConstruction = new ArrayList<ITimeBlockable>(endInd - startInd);
		for(int i = 0; i < endInd - startInd; ++i)
			underConstruction.add(null);
		
		//1. Iterate over the allBlocks set and count the number of hours unavailable in
		//	 the [start, end] range.
		for(int i = startInd; i < endInd; ++i) {
			ITimeBlockable block = allBlocks.get(i);

			if(!block.isMovable()) {
				//Only count unavailable hours in the time range in-question
				if(block.getEnd().getTime() > end.getTime()) {
					timeUnavailableMillis += end.getTime() - block.getStart().getTime();
					break;
				}
				else {
					timeUnavailableMillis += block.getLength();
				}
			}
			else {
				//Track the set of AssignmentBlocks in the range for traversal later
				asgnBlocks.add(block);
			}
		}

		
		//2. Calculate the amount of available free time in the [start, end] range, and
		//	 the average amount of space that can be placed between blocks.
		freeTimeBankMillis = end.getTime() - start.getTime() - timeUnavailableMillis;
		for(ITimeBlockable itb : asgnBlocks) {
			freeTimeBankMillis -= itb.getEnd().getTime() - itb.getStart().getTime();
		}

		avgFreeTimeMillis = freeTimeBankMillis / allBlocks.size();

		//3. Iterate over the AssignmentBlock list in reverse order, trying to place as much free time
		//	 between AssignmentBlocks as possible		
		for(int i = asgnBlocks.size() - 1; i >= 0; --i) {
			//4. Try to put the average amount of free time between the previously placed block
			//	 and the block currently being placed.  If there is a conflict with an unmovable block
			//	 that resides there, find the end of the unmovable zone (WATCH for multiple unmovables)
			//	 then try to place the block there.
			ITimeBlockable block = asgnBlocks.get(i);
			long delta = block.getLength();
			
			//Get the new start/end time for this block
			long recommendedStart = timeToStartFrom.getTime() - avgFreeTimeMillis - delta;
			long newStart = getBlockInsertLocation(block, unavailList, recommendedStart);
			long newEnd = newStart + delta;

			//Don't let a block be pushed back its original time
			Assignment blockAsgn = StorageService.getAssignment(block.getTask().getAssignmentID());	
			if(newStart <= block.getStart().getTime()) {
				underConstruction.set(i, block);
				
				//Decrement the free time used, despite the fact that the block was not moved
				freeTimeBankMillis -= (timeToStartFrom.getTime() - block.getEnd().getTime());
				
				//Reset the time for where to start on the next iteration
				timeToStartFrom = (Date) block.getStart().clone();
				
				continue;
			}
			//Don't let a block be pushed past its due date
			if(newEnd > blockAsgn.getDueDate().getTime()) {
				underConstruction.set(i, block);
				
				//Decrement the free time used, despite the fact that the block was not moved
				freeTimeBankMillis -= (timeToStartFrom.getTime() - block.getEnd().getTime());
				
				//Reset the time for where to start on the next iteration
				timeToStartFrom = (Date) block.getStart().clone();
				continue;
			}

			//Place the block in its new location
			block.getStart().setTime(newStart);
			block.getEnd().setTime(newEnd);

			//Append the block to the front of the "underConstruction" list
			underConstruction.set(i, block);
			
			//Decrement the amount of time placed from the time bank
			//--This case indicates that there are unavailable blocks in between the time that
			//	"newStart" was actually allocated and the original recommended time.
			if(recommendedStart > newStart) {
				//Subtract the amount of free time in between recommendedStart and newStart
				int newStartInd = TimeUtilities.indexOfFitLocn(allBlocks, new Date(newStart));
				int recStartInd = TimeUtilities.indexOfFitLocn(allBlocks, new Date(recommendedStart));
				
				//Iterate over all blocks in between the two times, and subtract the empty space between them
				for(int j = newStartInd; j < recStartInd; ++j) {
					if(j + 1 > allBlocks.size())
						break;
					
					ITimeBlockable b1 = allBlocks.get(j);
					ITimeBlockable b2 = allBlocks.get(j + 1);
					freeTimeBankMillis -= (b2.getStart().getTime() - b1.getEnd().getTime());
				}
			}
			//Otherwise, decrement the amount of time as normal
			else {
				freeTimeBankMillis -= (timeToStartFrom.getTime() - newEnd);
			}
			
			//Reset the time for where to start on the next iteration
			timeToStartFrom = (Date) block.getStart().clone();

			//Reset avgFreeTimeMillis in case not as much free time was used while de-compacting
			//previous blocks (and vice versa)
			if(i != 0)
				avgFreeTimeMillis = freeTimeBankMillis / i;

			//TODO: WILL THIS CASE OCCUR?
			//5. Track the amount of free time in the free bank - if the bank is too low on time, restart
			//	 the iteration from the beginning and remove a small amount of free time from between 
			//	 all succeeding elements of blocks in order to ensure that there is enough free time
			//	 to place the originally-requested block.
		}

		
		//Insert the underConstruction blocks in sorted order into the "allBlocks" list
		for(int i = startInd, constrInd = 0; i < endInd; ++i, ++constrInd) {
			allBlocks.set(i, underConstruction.get(constrInd));
		}
		
		//Try to switch the order of consecutive blocks that are of the same type
		trySwitchBlockOrder(allBlocks);

		//Try to move assignments to their preferred time-of-day if possible
		//optimizePreferredTime(allBlocks);

	}
	
	
	/**
	 * Iterates over the parameter List and switches ITimeBlockables that are of the same
	 * Assignment type, so that a variety of different Assignments appear in a row for
	 * user-friendly work schedules.
	 * @param allBlocks A List of ITimeBlockables, sorted by start Date
	 */
	private static void trySwitchBlockOrder(List<ITimeBlockable> allBlocks) {
		//When iterating, look at the next and previous assignments, and make sure that there
		//are no long successions of the same assignment (consecutive HOUR-wise, not necessarily
		//just block-wise)
				
		//TODO: tweak this val
		final long lim = TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES);
		
		for(int i = 1; i < allBlocks.size() - 1; ++i) {
			
			//Check for (a) 1-2 consecutive or (b) 2-1 consecutive
			
			ITimeBlockable prev = allBlocks.get(i - 1);
			ITimeBlockable curr = allBlocks.get(i);
			ITimeBlockable next = allBlocks.get(i + 1);
			
			//Don't attempt a switch if one of these blocks is unmovable
			if(!prev.isMovable() || !curr.isMovable() || !next.isMovable())
				continue;
			
			String prevID = prev.getTask().getAssignmentID();
			String currID = curr.getTask().getAssignmentID();
			String nextID = next.getTask().getAssignmentID();
			
			
			//Check for curr.template == next.template != prev.template
			if (!prevID.equals(currID) && currID.equals(nextID)) {
				
				//Don't switch them if there is a sizable gap between them
				if(next.getStart().getTime() - curr.getEnd().getTime() > lim)
					continue;
				

				//TODO: What do I want to do with the output of this function?
				//		Note: this function tries several different ways of switching
				//		blocks, regardless of their lengths
				if(TimeUtilities.switchTimeBlocks(allBlocks, prev, curr)) {
					System.out.println("hooray!");
				}
				else {
					System.out.println("block switch failed");
				}
				
				
				//Increment i so that this doesn't get repeated
				++i;
			}
			if (prevID.equals(currID) && !currID.equals(nextID)) {
				
				//Don't switch them if there is a sizable gap between them
				if(curr.getStart().getTime() - prev.getEnd().getTime() > lim)
					continue;
				

				//TODO: What do I want to do with the output of this function?
				//		Note: this function tries several different ways of switching
				//		blocks, regardless of their lengths
				if(TimeUtilities.switchTimeBlocks(allBlocks, curr, next)) {
					System.out.println("hooray!");
				}
				else {
					System.out.println("block switch failed");
				}
				
				//Increment i so that this doesn't get repeated
				++i;
			}
		}
	}
	
	
	/**
	 * Moves blocks around in the list so that they are in their preferred time of day.
	 * @param allBlocks A List of ITimeBlockables sorted by start Date
	 */
	private static void optimizePreferredTime(List<ITimeBlockable> allBlocks) {
		//TODO: try to put blocks in their preferred time of day, if possible
		
		for(int i = 0; i < allBlocks.size(); ++i) {
			
		}
		
	}
	
	/**
	 * 
	 * @param block The ITimeBlockable to-be-inserted
	 * @param unavailBlocks The List of UnavailableBlocks in range.
	 * @param recommendedStart A Date indicating the recommended time for "block" to be inserted
	 * @return Returns a long representing then number of milliseconds since January 1, 1970, which
	 * 			is the recommended location to insert the parameter "block"
	 */
	private static long getBlockInsertLocation(ITimeBlockable block, List<ITimeBlockable> unavailBlocks, 
			long recommendedStart) {
		ITimeBlockable pred = null;
		ITimeBlockable succ = null;
		
		long newStart = recommendedStart;
		long delta = block.getLength();
		long newEnd = newStart + delta;

		//Make sure that the block does not overlap any unavailable blocks
		while(true) {
			int indFit = TimeUtilities.indexOfFitLocn(unavailBlocks, new Date(newStart));
			pred = (indFit == 0 ? null : unavailBlocks.get(indFit - 1));
			succ = (indFit >= unavailBlocks.size() - 1 ? null : unavailBlocks.get(indFit));
			
			//Ensure no overlap with successor
			if(succ != null && succ.getStart().getTime() < newEnd) {
				newEnd = succ.getStart().getTime();
				newStart = newEnd - delta;
				continue;
			}
		
			//Ensure no overlap with predecessor
			if(pred != null && pred.getEnd().getTime() > newStart) {
				newEnd = pred.getStart().getTime();
				newStart = newEnd - delta;
				continue;
			}
			
			//If no overlap occurs with either the predecessor or the successor, break this loop
			break;
		}
		
		return newStart;
	}
}
