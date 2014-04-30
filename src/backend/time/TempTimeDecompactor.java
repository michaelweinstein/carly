package backend.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.database.StorageService;
import data.Assignment;
import data.ITimeBlockable;

public class TempTimeDecompactor {

	private static final int MILLIS_IN_DAY = 86400000;

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
		Date timeToStartFrom = new Date(end.getTime() - MILLIS_IN_DAY);
		//TODO: ^^ this timeToStartFrom can be tweaked



		int startInd = TimeUtilities.indexOfFitLocn(allBlocks, start);
		int endInd = TimeUtilities.indexOfFitLocn(allBlocks, end);
		
		//This ArrayList will contain the items in sorted order as they are de-compacted.
		List<ITimeBlockable> underConstruction = new ArrayList<ITimeBlockable>(endInd - startInd);
		
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
			long recommendedStart = timeToStartFrom.getTime() - avgFreeTimeMillis - delta;
			long newStart = getBlockInsertLocation(block, allBlocks, recommendedStart);
			long newEnd = newStart + delta;

			
			//Don't let a block be pushed back it's original time
			if(newStart < block.getStart().getTime()) {
				break;
			}
			
			//TODO: This is a pretty unlikely case, but it does need to be addressed...
			//if(newEnd > blockAsgn.getDueDate().getTime()) {
//				System.err.println("Bad END-insertion attempt!");
//				timeToStartFrom = (Date) block.getStart().clone();
//				//break;
//				continue;
			//}

			//Place the block in its new location
			block.getStart().setTime(newStart);
			block.getEnd().setTime(newEnd);

			//Append the block to the front of the "underConstruction" list
			underConstruction.set(i, block);
			
			//Decrement the amount of time placed from the time bank
			//TODO: This will not work if there is an unavailable block in between the last time placed
			//		and the newly-placed block
			freeTimeBankMillis -= (newEnd - timeToStartFrom.getTime());

			//Reset the time for where to start on the next iteration
			timeToStartFrom = (Date) block.getStart().clone();

			if(recommendedStart != newStart) {
				//Get the previous item in the list, and use this block's start as the new "timeToStartFrom"
				int insertedLocn = TimeUtilities.indexOfFitLocn(allBlocks, new Date(newStart - 1));
				ITimeBlockable prevBlock = allBlocks.get(insertedLocn - 1);
				timeToStartFrom.setTime(prevBlock.getStart().getTime());
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

		
		//Insert the underConstruction blocks in sorted order into the "allBlocks" list
		for(int i = startInd, constrInd = 0; i < endInd; ++i, ++constrInd) {
			allBlocks.set(i, underConstruction.get(constrInd));
		}
		
		//Try to switch the order of consecutive blocks that are of the same type
		//trySwitchBlockOrder(allBlocks);

		//Try to move assignments to their preferred time-of-day if possible
		//optimizePreferredTime(allBlocks);

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
