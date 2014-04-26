package backend.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.database.StorageService;

import data.AssignmentBlock;
import data.IAssignment;
import data.ITask;
import data.ITemplate;
import data.ITemplateStep;
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
	private List<ITimeBlockable> m_localChangesToBlocks;

	public TimeAllocator(IAssignment asgn) {
		this.m_asgn = asgn;
		this.m_localChangesToBlocks = new ArrayList<ITimeBlockable>();
	}



	public void insertAsgn(Date start, Date end) {
		double numHoursPerBlock;
		int numBlocksLeft; //the number of blocks left to place

		m_localChangesToBlocks.clear();

		//Get the current set of blocks that have been marked by the user as either unavailable
		//or currently occupied by another assignment
		//Note that these lists are in sorted order.
		//Date start = new Date();
		//Date end = m_asgn.getDueDate();
		List<UnavailableBlock> unavailable = StorageService.getAllUnavailableBlocksWithinRange(start, end);
		List<AssignmentBlock> curr_asgns = StorageService.getAllAssignmentBlocksWithinRange(start, end);
		List<ITimeBlockable> allBlocks = zipTimeBlockLists(unavailable, curr_asgns);

		//If there are not enough free hours in the range specified by the new Assignment,
		//exit this function
		if(!TimeUtilities.existsPossibleFit(allBlocks, m_asgn, start))
			return;
		
		//Get the number of subtasks for this assignment, determine how many chunks to break into
		//per subtask, and how long per subtask
		ITemplate template = m_asgn.getTemplate();		
		numHoursPerBlock = (template == null ? DEFAULT_HRS_PER_BLOCK : template.getPreferredConsecutiveHours());
		
		//DEBUG added by Eric 
		System.out.println("MIDDLE: TimeAllocator: getting contents of allBlocks");
		for (ITimeBlockable block : allBlocks) {
			System.out.println("\t" + block.toString());
		}
		System.out.println("");
		//DEBUG
		
		List<ITemplateStep> tempSteps = template.getAllSteps();
		Date lastTimePlaced = start;
		boolean success = false;
		for(int i = 0; i < tempSteps.size(); ++i) {
			//Get the number of blocks to place for this current step
			ITemplateStep step = tempSteps.get(i);
			double numHoursInStep = m_asgn.getExpectedHours() * step.getPercentOfTotal();
			numBlocksLeft = (int) Math.ceil(numHoursInStep / numHoursPerBlock);

			//TODO: Handle cases where the number of hours of a user-submitted Assignment
			//		is exceedingly low
			
			success = tryUniformInsertion(allBlocks, start, end, lastTimePlaced, step,
					numBlocksLeft, numHoursPerBlock);
		
			//TODO: Attempt alternate insertion policy here
			if(!success) {}
		}

		//TODO: Then, decompact all AssignmentBlocks so that a user may have a break
		//		from his/her work time.  This decompact() function will consider several
		//		heuristics including (1) putting assignments in their preferred time-of-day
		//		(2) spacing them out to have breaks, (3) variety between different types of
		//		assignments if there are several AssignmentBlocks in a row
		
		System.out.println("[Start, End] : [" + start + ", " + end + "]");
		System.out.println("Debug, allBlocks list before decompaction");
		for(int i = 0; i < allBlocks.size(); ++i) {
			ITimeBlockable itb = allBlocks.get(i);
			System.out.println("Start: " + itb.getStart() + " || End: " + itb.getEnd());
		}
		
		//TODO: CAREFUL WITH THIS CALL!!! I don't want to decompact blocks from *other* assignments
		//		past their due dates - so I'm going to have to do a check for each block's due date
		TimeCompactor.decompact(allBlocks, start, end);
			
		System.out.println("DEBUG - printing out the time ranges of all blocks");	
		for(int i = 0; i < allBlocks.size(); ++i) {
			ITimeBlockable itb = allBlocks.get(i);
			System.out.println("Start: " + itb.getStart() + " || End: " + itb.getEnd() + itb.isMovable());
		}
		
		//Assign the value of this field so it may be accessed by the "getter"
		//function in this class
		m_localChangesToBlocks = allBlocks;
		
		//DEBUG added by Eric 
		System.out.println("END: TimeAllocator: getting contents of allBlocks");
		for (ITimeBlockable block : allBlocks) {
			System.out.println("\t" + block.toString());
		}
		System.out.println("");
		//DEBUG

	}
	
	
	private boolean tryUniformInsertion(List<ITimeBlockable> allBlocks, Date start, Date end, Date lastTimePlaced,
			ITemplateStep step, int numBlocksLeft, double numHoursPerBlock) {
		
		boolean hasCompactedOnce = false;
		
		while(numBlocksLeft > 0) {
			//1. Use find fit function for the next block (Best-Fit search policy)
			AssignmentBlock block = findFit(allBlocks, numHoursPerBlock, 
					(Date) lastTimePlaced.clone(), (Date) end.clone(), step);

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
					//TODO: Remove this println
					System.err.println("Could not insert block, even after compacting -- TODO:"
						+ " Try to move blocks contained by other assignments outside of the range\n"
						+ " OR use more sophisticated compaction around unmovable blocks\n"
						+ " OR return FAIL message to the user\n"
						+ " OR try breaking the remaining blocks into half-size pieces\n");
					
					return false;
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
		
		return true;
	}
	
	//Return a newly-initialized AssignmentBlock containing the relevant start/end
	//dates for the current chunk.
	private AssignmentBlock findFit(List<ITimeBlockable> blockList, double blockLength,
			Date start, Date end, ITemplateStep step) {
		Date bestStart = null;
		Date bestEnd = null;		
		long minTimeLeftover = Long.MAX_VALUE;
		long blockLenInMillis= convertHoursToMillis(blockLength);
		long delta = 0;

		if(blockList.size() == 0) {
			bestStart = start;
			bestEnd = new Date(bestStart.getTime() + convertHoursToMillis(blockLength));

			//Get the corresponding task from the Assignment member variable
			ITask task = m_asgn.getTasks().get(step.getStepNumber());
			return new AssignmentBlock(bestStart, bestEnd, task);
		}


		//Get free time between start time given and first block in list
		if(blockLenInMillis <= (delta = blockList.get(0).getStart().getTime() - start.getTime())) {
			bestStart = start;
			minTimeLeftover = delta - blockLenInMillis;
			bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
		}

		//TODO: BIG PROBLEM: items of different tasks under the same assignment need to be in 
		//		the calendar in a sequential order...
		//		--SOL'N 1: use a "lastTimePlaced" block as a starting point for each new task
		//		--SOL'N 2: ??
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


		//Create the task to give in the AssignmentBlock constructor
		ITask task = m_asgn.getTasks().get(step.getStepNumber());
		return new AssignmentBlock(bestStart, bestEnd, task);
	}


	private List<ITimeBlockable> zipTimeBlockLists(List<UnavailableBlock> unavailable,
			List<AssignmentBlock> curr_asgns) {
		List<ITimeBlockable> zippedList = new ArrayList<ITimeBlockable>(unavailable.size() + curr_asgns.size());

		int unavailInd = 0;
		int asgnInd = 0;

		//DEBUG added by Eric
		System.out.println("\nTimeAllocator: zipTimeBlockLists: printing out assignment blocks");
		for (AssignmentBlock block : curr_asgns) {
			System.out.println("\t" + block.toString());
		}
		System.out.println("");
		//DEBUG
		
		
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

	public List<ITimeBlockable> getEntireBlockSet() {
		return new ArrayList<ITimeBlockable>(m_localChangesToBlocks);
	}

}