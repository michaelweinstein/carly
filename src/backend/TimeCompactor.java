package backend;

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
		
		//ITimeBlockable pred = null;
		//ITimeBlockable succ = null;
		
		long timeUnavailableMillis = 0;
		
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
		}
		
		for(int i = allBlocks.size() - 1; i >= 0; ++i) {
			ITimeBlockable block = allBlocks.get(i);
			
			if(!block.isMovable())
				continue;
			
			
			
		}
		
		
		
		//When iterating, look at the next and previous assignments, and make sure that there
		//are no long successions of the same assignment (consecutive HOUR-wise, not necessarily
		//just block-wise)
		
		//Finally, take the new order of the block list, and add a uniform amount of space between
		//all blocks in the [start,end] range
		//TODO: tweak how blocks are spaced out -- uniformly is likely not the best way to go
		
		
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
		if(curr.compareTo(timeList.get(0).getStart()) < 0)
			return 0;
		else if(curr.compareTo(timeList.get(size - 1).getStart()) > 0)
			return size - 1;

		//Compare to all surrounding pairs in the middle
		for(ind = 1; ind < size; ++ind) {
			if(curr.compareTo(timeList.get(ind - 1).getStart()) > 0 &&
					curr.compareTo(timeList.get(ind).getStart()) < 0)
				return ind;
		}

		return ind;
	}
	
	
}
