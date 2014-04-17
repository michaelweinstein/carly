package backend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import data.ITimeBlockable;


public class TimeCompactor {
	
	
	//This function currently compacts all movable blocks in the Date range [start, end]
	//in order to prevent "external fragmentation" along the client's time stream
	//TODO: It can probably be improved to be smarter than this, though giving the range
	//		of compaction as parameters allows for some flexibility.
	public static void compact(List<ITimeBlockable> allBlocks, Date start, Date end, 
			Date lastTimePlaced) {
		
		int startInd = indexOfFitLocn(allBlocks, start);
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
			long delta = block.getEnd().getTime() - block.getStart().getTime();
			Date newEnd = new Date(timeToPushTo.getTime() + delta);
			
			//Reset the value in time that the "lastTimePlaced" reference points
			//to if it is encountered
			if(block.getStart().getTime() == lastTimePlaced.getTime()) {
				lastTimePlaced.setTime(timeToPushTo.getTime());
			}
			
			block.setStart(timeToPushTo);
			block.setEnd(newEnd);
			
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
		
		ITimeBlockable pred = null;
		ITimeBlockable succ = null;
		
		long timeUnavailableMillis = 0;
		long freeTimeBankMillis = 0;
		long avgFreeTimeMillis = 0;
		List<ITimeBlockable> asgnBlocks = new ArrayList<ITimeBlockable>();
		Date timeToStartFrom = end;

		
		//TODO: THIS ALGORITHM CURRENTLY FAILS FOR CASES WHERE THERE IS NO FREE TIME AVAILABLE
		//		AT THE END, BUT RATHER, WHERE IT IS ALL AVAILABLE AT THE BEGINNING.
		//		--Poss solution: get measures of density of blocks over certain ranges of the list
		//		then determine whether it is better to start iterating from the end of the list,
		//		or from the beginning.
		
		//1. Iterate over the allBlocks set and count the number of hours unavailable in
		//	 the [start, end] range.
		for(int i = indexOfFitLocn(allBlocks, start); i < allBlocks.size(); ++i) {
			ITimeBlockable block = allBlocks.get(i);
			
			if(!block.isMovable()) {
				//Only count unavailable hours in the time range in-question
				if(block.getEnd().getTime() > end.getTime()) {
					timeUnavailableMillis += block.getEnd().getTime() - end.getTime();
					break;
				}
				else {
					timeUnavailableMillis += block.getEnd().getTime() - block.getStart().getTime();
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
		
		avgFreeTimeMillis = freeTimeBankMillis / asgnBlocks.size();
		
		//3. Iterate over the block list in reverse order, trying to place as much free time between
		//	 AssignmentBlocks as possible		
		for(int i = asgnBlocks.size() - 1; i >= 0; --i) {
			//4. Try to put the average amount of free time between the previously placed block
			//	 and the block currently being placed.  If there is a conflict with an unmovable block
			//	 that resides there, find the end of the unmovable zone (WATCH for multiple unmovables)
			//	 then try to place the block there.
			//EDGE CASE: What if a block cannot be moved?
			ITimeBlockable block = asgnBlocks.get(i);
			long newStart = timeToStartFrom.getTime() - avgFreeTimeMillis;
						
			newStart = getBlockInsertLocation(block, allBlocks, newStart);
			
			long delta = block.getEnd().getTime() - block.getStart().getTime();
			long newEnd = newStart + delta;
			
			
			//Place the block in its new location, decrement from the time bank, and
			//reset the time for where to start on the next iteration
			block.getStart().setTime(newStart);
			block.getEnd().setTime(newEnd);
			freeTimeBankMillis -= (newEnd - newStart);
			timeToStartFrom = block.getStart();
			
			
			//TODO: WILL THIS CASE OCCUR?
			//5. Track the amount of free time in the free bank - if the bank is too low on time, restart
			//	 the iteration from the beginning and remove a small amount of free time from between 
			//	 all succeeding elements of blocks in order to ensure that there is enough free time
			//	 to place the originally-requested block.
		}
		
		
		
		//When iterating, look at the next and previous assignments, and make sure that there
		//are no long successions of the same assignment (consecutive HOUR-wise, not necessarily
		//just block-wise)
		
		//Finally, take the new order of the block list, and add a uniform amount of space between
		//all blocks in the [start,end] range
		//TODO: tweak how blocks are spaced out -- uniformly is likely not the best way to go
		
		
	}
	
	
	private static long getBlockInsertLocation(ITimeBlockable block, List<ITimeBlockable> allBlocks, 
			long recommendedStart) {
		ITimeBlockable pred = null;
		ITimeBlockable succ = null;
		
		long newStart = recommendedStart;
		long delta = block.getEnd().getTime() - block.getStart().getTime();
		long newEnd = newStart + delta;
		

		while(true) {
			//TODO: I don't like how inefficient a linear traversal here is at every step...
			int indFit = indexOfFitLocn(allBlocks, new Date(newStart));
			pred = (indFit == 0 ? null : allBlocks.get(indFit - 1));
			succ = (indFit >= allBlocks.size() - 1 ? null : allBlocks.get(indFit + 1));
			
			//Ensure no overlap with successor
			if(succ != null && succ.getStart().getTime() < newEnd) {
				newStart = succ.getEnd().getTime();
				newEnd = newStart + delta;
				continue;
				//succ = allBlocks.get(temp);
				//++temp;
			}
		
			//Ensure no overlap with predecessor
			if(pred != null && pred.getEnd().getTime() > newStart) {
				newStart = pred.getEnd().getTime();
				newEnd = newStart + delta;
				continue;
				//pred = allBlocks.get(indFit);
				//++indFit;
			}
			
			//If no overlap occurs with either the predecessor or the successor, break this loop
			break;
		}
		
		return newStart;
	}
	
	
	//This function is copied from TimeAllocator
	//TODO: REMOVE THE DUPLICATE HERE
	//TODO: REMOVE THE DUPLICATE HERE
	//TODO: REMOVE THE DUPLICATE HERE
	//TODO: REMOVE THE DUPLICATE HERE
	//Returns the index in the list where the current time should appear
	//This function uses START dates in its comparisons
	private static int indexOfFitLocn(List<ITimeBlockable> timeList, Date curr) {
		int ind;
		int size = timeList.size();

		if(timeList.size() == 0)
			return 0;


		//TODO: Use the *TimeBlock*s' compareTo() function instead of directly
		//comparing *Date*s here

		//Compare to the first element and last element in the list
		if(curr.compareTo(timeList.get(0).getStart()) <= 0)
			return 0;
		else if(curr.compareTo(timeList.get(size - 1).getStart()) > 0)
			return size;

		//Compare to all surrounding pairs in the middle
		for(ind = 1; ind < size; ++ind) {
			if(curr.compareTo(timeList.get(ind - 1).getStart()) > 0 &&
					curr.compareTo(timeList.get(ind).getStart()) < 0)
				return ind;
		}

		return ind;
	}
	
	
}
