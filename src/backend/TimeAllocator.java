package backend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
		//CAREFUL - certain blocks are unmovable... therefore, I'm not sure that I
		//can ever guarantee that an assignment can fit.  HOWEVER, I will be able to
		//declare that an assignment *can't* fit.

		//Clear the old sets of blocks 
		m_toDelete.clear();
		m_toAdd.clear();
		m_localChangesToBlocks.clear();

		//Get the current set of blocks that have been marked by the user as either unavailable
		//or currently occupied by another assignment
		//TODO: note that these lists are in sorted order.
		Date start = new Date();
		Date end = m_asgn.getDueDate();
		List<UnavailableBlock> unavailable = new ArrayList<>();
				//StorageService.getAllUnavailableBlocksWithinRange(start, end);
		
		//TODO: AS A TEMPORARY TEST - inserting some stuff into the unavailable list
		/************************************************************/
		UnavailableBlock ub1 = new UnavailableBlock((Date) start.clone(), new Date(start.getTime() + 14400000), null, false);
		UnavailableBlock ub2 = new UnavailableBlock(new Date(start.getTime() + 86400000),
				new Date(start.getTime() + 104400000), null, false);
		unavailable.add(ub1);
		unavailable.add(ub2);
		/************************************************************/

		List<AssignmentBlock> curr_asgns = new ArrayList<>();
				//StorageService.getAllAssignmentBlocksWithinRange(start, end);
		List<ITimeBlockable> allBlocks = zipTimeBlockLists(unavailable, curr_asgns);

		//Get the number of subtasks for this assignment, determine how many chunks to break into
		//per subtask, and how long per subtask
		taskList = m_asgn.getTasks();
		numSubtasks = taskList.size();
		numHoursPerBlock = DEFAULT_HRS_PER_BLOCK;

		//TODO: Will the null case ever happen?
		ITemplate template = m_asgn.getTemplate();
		if(template != null)
			numHoursPerBlock = template.getPreferredConsecutiveHours();


		//Try the best case assumption - that blocks are able to be split uniformly across the days
		//that a user is working on an assignment
		numBlocksLeft = (int) Math.ceil(m_asgn.getExpectedHours() / numHoursPerBlock);
		

		//TODO: BIG CHANGE!! Items of different tasks must go in sequential order.  Therefore,
		//		this loop should change so that is a two-layered loop: one layer goes over the
		//		set of tasks to insert, and the inner layer goes over the number of blocks for
		//		that task.
		//		--After a given Task is completely inserted, then the "start" date should be
		//		reset to the end of the last time block of that Task type to ensure
		//		chronological correctness.
		
		boolean hasCompactedOnce = false;
		Date lastTimePlaced = start;
		
		while(numBlocksLeft > 0) {
			//1. Use find fit function for the next block (BEST-fit search, NOT FIRST FIT)
			AssignmentBlock block = findFit(allBlocks, numHoursPerBlock, 
					(Date) lastTimePlaced.clone(), (Date) end.clone());

			//2. If no fit can be found, try compaction OR break the loop and move on to 
			//the next type of insertion policy
			if(block == null) {
				
				if(!hasCompactedOnce){
					//Compact existing blocks so that they fit better, and reset the lastTimePlaced
					//reference so that it is still accurate
					TimeCompactor.compact(allBlocks, start, end, lastTimePlaced);
					continue;
					//TODO: currently I am compacting all blocks... is a different range better?
					
				}
				else {
					System.err.println("Could not insert block, even after compacting -- TODO:"
						+ " Try to move blocks contained by other assignments outside of the range\n"
						+ " OR use more sophisticated compaction around unmovable blocks\n"
						+ " OR return FAIL message to the user\n"
						+ " OR try breaking the remaining blocks into half-size pieces\n");
					
					break;
				}
			}

			//3. If a fit is found, insert the block into the list, decrement the counter
			//	 and continue.
			int ind = TimeUtilities.indexOfFitLocn(allBlocks, block.getStart());
			allBlocks.add(ind, block);
			--numBlocksLeft;
			
			//4. Reset the place that the last block was placed for future searches
			lastTimePlaced = block.getStart();
		}


		//TODO: Attempt alternate insertion policy here


		//TODO: Then, decompact all AssignmentBlocks so that a user may have a break
		//		from his/her work time.  This decompact() function will consider several
		//		heuristics including (1) putting assignments in their preferred time-of-day
		//		(2) spacing them out to have breaks, (3) variety between different types of
		//		assignments if there are several AssignmentBlocks in a row
		
		System.out.println("Debug, allBlocks list before decompaction");
		for(int i = 0; i < allBlocks.size(); ++i) {
			ITimeBlockable itb = allBlocks.get(i);
			System.out.println("Start: " + itb.getStart() + " || End: " + itb.getEnd());
		}
		
		//if(hasCompactedOnce) {
			TimeCompactor.decompact(allBlocks, start, end);
		//}
			
		System.out.println("DEBUG - printing out the time ranges of all blocks");	
		for(int i = 0; i < allBlocks.size(); ++i) {
			ITimeBlockable itb = allBlocks.get(i);
			System.out.println("Start: " + itb.getStart() + " || End: " + itb.getEnd() + itb.isMovable());
		}
		
		//Assign the value of this field so it may be accessed by the "getter"
		//function in this class
		m_localChangesToBlocks = allBlocks;

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
				bestStart = (Date) blockList.get(i).getEnd().clone();
				minTimeLeftover = delta - blockLenInMillis;
				bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
			}	
		}

		//Get free time between last block in list and end time given
		if(blockLenInMillis <= (delta = end.getTime() - blockList.get(blockList.size() - 1).getEnd().getTime())
				&& delta - blockLenInMillis < minTimeLeftover) {
			bestStart = (Date) blockList.get(blockList.size() - 1).getEnd().clone();
			minTimeLeftover = delta - blockLenInMillis;
			bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
		}


		//TODO: give an actual task here instead of null
		return new AssignmentBlock(bestStart, bestEnd, null, true);
	}


	private List<ITimeBlockable> zipTimeBlockLists(List<UnavailableBlock> unavailable,
			List<AssignmentBlock> curr_asgns) {
		List<ITimeBlockable> zippedList = new ArrayList<ITimeBlockable>(unavailable.size() + curr_asgns.size());

		int unavailInd = 0;
		int asgnInd = 0;

		//Iterate over the contents of these two arrays, then return a zipped list containing
		//the contents of both lists, in sorted order
		UnavailableBlock[] unavail = new UnavailableBlock[unavailable.size()];
		AssignmentBlock[] asgn = new AssignmentBlock[curr_asgns.size()];
		unavail = unavailable.toArray(unavail);
		asgn = curr_asgns.toArray(asgn);

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