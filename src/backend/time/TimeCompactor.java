package backend.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import backend.database.StorageService;
import data.Assignment;
import data.ITimeBlockable;


public class TimeCompactor {
	
	private static final int MILLIS_IN_DAY = 86400000;
	
	//This function currently compacts all movable blocks in the Date range [start, end]
	//in order to prevent "external fragmentation" along the client's time stream
	public static void compact(List<ITimeBlockable> allBlocks, Date start, Date end, 
			Date lastTimePlaced) {
		
		if(allBlocks.size() == 0)
			return;
		
		int startInd = TimeUtilities.indexOfFitLocn(allBlocks, start);
		Date timeToPushTo = allBlocks.get(startInd).getEnd();
		
		for(int i = startInd + 1; i < allBlocks.size(); ++i) {
			
			ITimeBlockable block = allBlocks.get(i);
			
			//If a block goes past the given "end" parameter, stop compacting and return
			if(block.getEnd().getTime() > end.getTime())
				return;
			
			if(!block.isMovable()) {
				//TODO: Currently I reset the time-to-push-to here to guarantee
				//		no errors... however, I may be able to do a check to make sure
				//		a block can fit between the last timeToPushTo and this unmovable block
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
		
	}

	//This function uses several human-friendly heuristics to de-compact a schedule
	//to allow (1) breaks between work, (2) a variety of assignments in succession,
	//(3) work during the preferred time of day
	public static void decompact(List<ITimeBlockable> allBlocks, Date start, Date end) {
		
		//Iterate over blocks.  If a block is movable, look at its preferred time of day.
		//See if there is a block already there.  If not, try to move it there.  If so,
		//try to switch (in the case of either Pareto improvement or indifference on one end.)
		
		long timeUnavailableMillis = 0;
		long freeTimeBankMillis = 0;
		long avgFreeTimeMillis = 0;
		List<ITimeBlockable> asgnBlocks = new ArrayList<ITimeBlockable>();
		Date timeToStartFrom = new Date(end.getTime() - MILLIS_IN_DAY);
		//TODO: ^^ this timeToStartFrom can be tweaked
		
		//TODO: THIS ALGORITHM CURRENTLY FAILS FOR CASES WHERE THERE IS NO FREE TIME AVAILABLE
		//		AT THE END, BUT RATHER, WHERE IT IS ALL AVAILABLE AT THE BEGINNING.
		//		--Poss solution: get measures of density of blocks over certain ranges of the list
		//		then determine whether it is better to start iterating from the end of the list,
		//		or from the beginning.
		
		//1. Iterate over the allBlocks set and count the number of hours unavailable in
		//	 the [start, end] range.
		for(int i = TimeUtilities.indexOfFitLocn(allBlocks, start); i < allBlocks.size(); ++i) {
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
		
		//TODO: I HAVE BEEN TWEAKING THE COMMENTS AND STUFF HERE TO TEST DIFFERENT
		//		LEVELS OF EFFECTIVENESS RE: THE DECOMPACTION ALGORITHM
		//2. Calculate the amount of available free time in the [start, end] range, and
		//	 the average amount of space that can be placed between blocks.
		freeTimeBankMillis = end.getTime() - start.getTime() - timeUnavailableMillis;
		for(ITimeBlockable itb : asgnBlocks) {
			freeTimeBankMillis -= itb.getEnd().getTime() - itb.getStart().getTime();
		}
		
		avgFreeTimeMillis = freeTimeBankMillis / allBlocks.size();
		
		//3. Iterate over the block list in reverse order, trying to place as much free time between
		//	 AssignmentBlocks as possible		
		for(int i = asgnBlocks.size() - 1; i >= 0; --i) {
			//4. Try to put the average amount of free time between the previously placed block
			//	 and the block currently being placed.  If there is a conflict with an unmovable block
			//	 that resides there, find the end of the unmovable zone (WATCH for multiple unmovables)
			//	 then try to place the block there.
			//EDGE CASE: What if a block cannot be moved?
			ITimeBlockable block = asgnBlocks.get(i);
			long delta = block.getLength();
			long recommendedStart = timeToStartFrom.getTime() - avgFreeTimeMillis;
			long newStart = getBlockInsertLocation(block, allBlocks, recommendedStart);
			long newEnd = newStart + delta;

			//Be careful to consider all blocks and their corresponding Assignment start/end times.
			Assignment blockAsgn = StorageService.getAssignment(block.getTask().getAssignmentID());			
			if(newStart < start.getTime()) {
				System.err.println("Bad START-insertion attempt!");
				timeToStartFrom = (Date) block.getStart().clone();
				//break;
				continue;
			}
			//if(newEnd > end.getTime()) {
			if(newEnd > blockAsgn.getDueDate().getTime()) {
				System.err.println("Bad END-insertion attempt!");
				timeToStartFrom = (Date) block.getStart().clone();
				//break;
				continue;
			}

			//Place the block in its new location and decrement from the time bank
			block.getStart().setTime(newStart);
			block.getEnd().setTime(newEnd);

			//Reset the location of the block in the list
			allBlocks.remove(block);
			TimeUtilities.insertIntoSortedList(allBlocks, block);
			

			freeTimeBankMillis -= (newStart - timeToStartFrom.getTime());
			//freeTimeBankMillis -= (newEnd - newStart);
			
			//Reset the time for where to start on the next iteration
			timeToStartFrom = (Date) block.getStart().clone();
			
			if(recommendedStart != newStart) {
				//Get the previous item in the list, and use this block's start as the new "timeToStartFrom"
				int insertedLocn = TimeUtilities.indexOfFitLocn(allBlocks, new Date(newStart - 1));
				ITimeBlockable prevBlock = allBlocks.get(insertedLocn - 1);
				timeToStartFrom.setTime(prevBlock.getStart().getTime());
				
				//THIS is the old temp solution -- back up timeToStartFrom by fixed amt
				//timeToStartFrom.setTime(timeToStartFrom.getTime() - delta);
			}
			
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
		
		//Try to switch the order of consecutive blocks that are of the same type
		trySwitchBlockOrder(allBlocks);
		
		//Try to move assignments to their preferred time-of-day if possible
		//optimizePreferredTime(allBlocks);
	}
	
	
	private static void trySwitchBlockOrder(List<ITimeBlockable> allBlocks) {
		//When iterating, look at the next and previous assignments, and make sure that there
		//are no long successions of the same assignment (consecutive HOUR-wise, not necessarily
		//just block-wise)
				
		//TODO: tweak this val
		final long lim = TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES);
		
		for(int i = 1; i < allBlocks.size() - 1; ++i) {
			
			//Check for (a) 1-2 (b) 2-1 (c) 3 -- look at 1-3-1 if possible  
			
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
			if (prevID.equals(currID) && currID.equals(nextID)) {
				//Try to switch with a prev if possible
				if(i != 1) {
					ITimeBlockable pp = allBlocks.get(i - 2);
					//TODO
				}
				
				//Try to switch with a next if possible
				if(i != allBlocks.size() - 2) {
					ITimeBlockable nn = allBlocks.get(i + 2);
					//TODO
				}
				
				i += 2;
			}
			
		}
	}
	
	
	private static void optimizePreferredTime(List<ITimeBlockable> allBlocks) {
		//TODO: try to put blocks in their preferred time of day, if possible
		
		for(int i = 0; i < allBlocks.size(); ++i) {
			
		}
		
	}
	
	
	private static long getBlockInsertLocation(ITimeBlockable block, List<ITimeBlockable> allBlocks, 
			long recommendedStart) {
		ITimeBlockable pred = null;
		ITimeBlockable succ = null;
		
		long newStart = recommendedStart;
		long delta = block.getLength();
		long newEnd = newStart + delta;
		

		while(true) {
			int indFit = TimeUtilities.indexOfFitLocn(allBlocks, new Date(newStart));
			pred = (indFit == 0 ? null : allBlocks.get(indFit - 1));
			succ = (indFit >= allBlocks.size() - 1 ? null : allBlocks.get(indFit));
			
			//Ensure no overlap with successor
			if(succ != null && succ.getStart().getTime() < newEnd) {
				newStart = succ.getEnd().getTime();
				newEnd = newStart + delta;
				continue;
			}
		
			//Ensure no overlap with predecessor
			if(pred != null && pred.getEnd().getTime() > newStart) {
				newStart = pred.getEnd().getTime();
				newEnd = newStart + delta;
				continue;
			}
			
			//If no overlap occurs with either the predecessor or the successor, break this loop
			break;
		}
		
		return newStart;
	}
	
	
}
