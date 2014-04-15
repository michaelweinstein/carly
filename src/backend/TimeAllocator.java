package backend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.DateTimeAtCompleted;

import data.AssignmentBlock;
import data.IAssignment;
import data.ITask;
import data.ITemplate;
import data.ITimeBlockable;
import data.UnavailableBlock;


/**
 * A class that will examine a user's current calendar of events and plot the optimal schedule,
 * given some recent change or new assignment to add to the calendar.
 * @author evanfuller
 *
 */
public class TimeAllocator {
	
	private static final double DEFAULT_HRS_PER_BLOCK = 3.0;
	
	private IAssignment m_asgn;
	private List<ITimeBlockable> m_toDelete;
	private List<ITimeBlockable> m_toAdd;
	
	//TODO: Replace the two lists above with the below - I will return to Dylan
	//		two lists representing all the unavailable time blocks and all the assigned
	//		time blocks that have been added or modified from the original calendar
	//		(i.e. I give him 2 lists containing everything within the ranges that 
	//		I have been messing with)
	private List<ITimeBlockable> m_localChangesToBlocks;
	
	public TimeAllocator(IAssignment asgn) {
		this.m_asgn = asgn;
		this.m_toDelete = new ArrayList<ITimeBlockable>();
		this.m_toAdd = new ArrayList<ITimeBlockable>();
		this.m_localChangesToBlocks = new ArrayList<ITimeBlockable>();
	}
	
	
	
	public void insertAsgn() {
		double numHoursPerBlock;
		List<ITask> taskList;
		int numSubtasks;
		int numBlocksLeft; //the number of blocks left to place
		
		//TODO: Do a check right away where I iterate over all available time to see if
		//		I can even insert an assignment with the requested number of hours.
		
		
		//Clear the old sets of blocks 
		m_toDelete.clear();
		m_toAdd.clear();
		
		//Get the current set of blocks that have been marked by the user as either unavailable
		//or currently occupied by another assignment
		//TODO: note that these lists are in sorted order.
		Date start = new Date();
		Date end = m_asgn.getDueDate();
		List<UnavailableBlock> unavailable = 
				StorageService.getAllUnavailableBlocksWithinRange(start, end);
		List<AssignmentBlock> curr_asgns =
				StorageService.getAllAssignmentBlocksWithinRange(start, end);
		List<ITimeBlockable> allBlocks = zipTimeBlockLists(unavailable, curr_asgns);
		
		//Get the number of subtasks for this assignment, determine how many chunks to break into
		//per subtask, and how long per subtask
		taskList = m_asgn.getTasks();
		numSubtasks = taskList.size();
		numHoursPerBlock = DEFAULT_HRS_PER_BLOCK;
		
		//TODO: does getTemplate() return null or something else if an arbitrary assignment doesn't
		//have a template associated to it?
		ITemplate template = m_asgn.getTemplate();
		if(template != null)
			numHoursPerBlock = template.getPreferredConsecutiveHours();
		
		
		//TODO: Pass the necessary parameters and uncomment this function call
		//insertBlocksUniformly();
		
		//Try the best case assumption - that blocks are able to be split uniformly across the days
		//that a user is working on an assignment
		numBlocksLeft = (int) Math.ceil(m_asgn.getExpectedHours() / numHoursPerBlock);
		
		
		
		while(numBlocksLeft > 0) {
			//1. Use find fit function for the next block (BEST-fit search, NOT FIRST FIT)
			AssignmentBlock block = findFit(allBlocks, numHoursPerBlock, start, end);
			
			//2. If no fit can be found, break this loop, and move on to the next type
			//	 of insertion policy
			if(block == null) {
				break;
			}
			
			//3. If a fit is found, insert the block into the list, decrement the counter
			//	 and continue.
			int ind = indexOfFitLocn(allBlocks, block.getStart());
			allBlocks.add(ind, block);
			--numBlocksLeft;
		}
		
		
		m_localChangesToBlocks = allBlocks;
		
	}
	
	
	private List<ITimeBlockable> zipTimeBlockLists(List<UnavailableBlock> unavailable,
			List<AssignmentBlock> curr_asgns) {
		List<ITimeBlockable> zippedList = new ArrayList<ITimeBlockable>(unavailable.size() + curr_asgns.size());
		
		int unavailInd = 0;
		int asgnInd = 0;
		
		//Iterate over the contents of these two arrays, then return a zipped list containing
		//the contents of both lists, in sorted order
		UnavailableBlock[] unavail = (UnavailableBlock[]) unavailable.toArray();
		AssignmentBlock[] asgn = (AssignmentBlock[]) curr_asgns.toArray();
		
		while (unavailInd < unavail.length || asgnInd < asgn.length) {
			
			if(unavailInd == unavail.length) {
				zippedList.add(asgn[asgnInd]);
				++asgnInd;
				continue;
			}
			if(asgnInd == asgn.length){
				zippedList.add(unavail[unavailInd]);
				++unavailInd;
				continue;
			}
			
			int comp = unavail[unavailInd].compareTo(asgn[asgnInd]);
			
			if(comp < 0) {
				zippedList.add(unavail[unavailInd]);
				++unavailInd;
			}
			else if(comp > 0) {
				zippedList.add(asgn[asgnInd]);
				++asgnInd;
			}
			else; //TODO: ???? there should never be two blocks that are equal
			
		}		
		
		return zippedList;
	}
	
	//TODO: Also pass the Task to-be-assigned to this block... or remove Task from constructor?
	private AssignmentBlock findFit(List<ITimeBlockable> blockList, double blockLength,
			Date start, Date end) {
		//Return a newly-initialized AssignmentBlock containing the relevant start/end
		//dates for the current chunk.
		
		Date bestStart = null;
		Date bestEnd = null;		
		long minTimeLeftover = Long.MAX_VALUE;
		long blockLenInMillis= convertHoursToMillis(blockLength);
		long delta = 0;
		
		if(blockList.size() == 0) {
			bestStart = start;
			bestEnd = new Date(bestStart.getTime() + convertHoursToMillis(blockLength));
			return new AssignmentBlock(bestStart, bestEnd, null, true);
		}
		
		
		//Get free time between start time given and first block in list
		if(blockLenInMillis <= (delta = blockList.get(0).getStart().getTime() - start.getTime())) {
			bestStart = start;
			minTimeLeftover = delta - blockLenInMillis;
			bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
		}
		
		//TODO: problem = iterating over the same list n times when I call this function
		//		n different times... further problem: items of different tasks under
		//		the same assignment need to be in the calendar in a sequential order
		for(int i = 0; i < blockList.size() - 1; ++i) {
			//Get free time between two blocks in the list
			if(blockLenInMillis <= (delta = blockList.get(i + 1).getStart().getTime() - 
					blockList.get(i).getEnd().getTime()) && delta - blockLenInMillis < minTimeLeftover) {
				bestStart = blockList.get(i).getEnd();
				minTimeLeftover = delta - blockLenInMillis;
				bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
			}	
		}
			
		//Get free time between last block in list and end time given
		if(blockLenInMillis <= (delta = end.getTime() - blockList.get(blockList.size() - 1).getEnd().getTime())
				&& delta - blockLenInMillis < minTimeLeftover) {
			bestStart = blockList.get(blockList.size() - 1).getEnd();
			minTimeLeftover = delta - blockLenInMillis;
			bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
		}
		
		
		//TODO: give an actual task here instead of null
		return new AssignmentBlock(bestStart, bestEnd, null, true);
	}
		
	
	//Returns the index in the list where the current time should appear
	//This function uses START dates in its comparisons
	private int indexOfFitLocn(List<ITimeBlockable> timeList, Date curr) {
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
	
	
	//Return true upon success, return false upon failure
//	private boolean insertBlocksUniformly() {
//		
//		
//		
//		return true;
//	}
	
	
	private long convertHoursToMillis(double hrs) {
		return (long) (hrs * 60 * 60 * 1000);
	}
	
	public List<ITimeBlockable> getBlocksToDelete() {
		return new ArrayList<ITimeBlockable>(m_toDelete);
	}
	
	public List<ITimeBlockable> getBlocksToAdd() {
		return new ArrayList<ITimeBlockable>(m_toAdd);
	}
	
	public List<ITimeBlockable> getEntireBlockSet() {
		return new ArrayList<ITimeBlockable>(m_localChangesToBlocks);
	}
	
}
