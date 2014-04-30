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
 * A class that will examine a user's current calendar of events and plot the optimal schedule, given some recent change
 * or new assignment to add to the calendar.
 * 
 * @author evanfuller
 */
public class TimeAllocator {
	
	private static final double		DEFAULT_HRS_PER_BLOCK	= 3.0;
	
	private final IAssignment		m_asgn;
	private List<ITimeBlockable>	m_localChangesToBlocks;
	
	public TimeAllocator(final IAssignment asgn) {
		m_asgn = asgn;
		m_localChangesToBlocks = new ArrayList<ITimeBlockable>();
	}
	
	public void insertAsgn(final Date start, final Date end) throws NotEnoughTimeException{
		double numHoursPerBlock;
		int numBlocksLeft; // the number of blocks left to place
		
		m_localChangesToBlocks.clear();
		
		// Get the current set of blocks that have been marked by the user as either unavailable
		// or currently occupied by another assignment
		// Note that these lists are in sorted order.
		// Date start = new Date();
		// Date end = m_asgn.getDueDate();
		final List<UnavailableBlock> unavailable = StorageService.getAllUnavailableBlocksWithinRange(start, end);
		final List<AssignmentBlock> curr_asgns = StorageService.getAllAssignmentBlocksWithinRange(start, end);
		final List<ITimeBlockable> allBlocks = TimeUtilities.zipTimeBlockLists(unavailable, curr_asgns);
		
		// If there are not enough free hours in the range specified by the new Assignment,
		// exit this function
		if (!TimeUtilities.existsPossibleFit(allBlocks, m_asgn, start)) {
			throw new NotEnoughTimeException("Not enough free time available by the specified due date");
		}
		
		// Get the number of subtasks for this assignment, determine how many chunks to break into
		// per subtask, and how long per subtask
		final ITemplate template = m_asgn.getTemplate();
		numHoursPerBlock = (template == null ? DEFAULT_HRS_PER_BLOCK : template.getPreferredConsecutiveHours());
		
		// DEBUG added by Eric
		System.out.println("MIDDLE: TimeAllocator: getting contents of allBlocks");
		for (final ITimeBlockable block : allBlocks) {
			System.out.println("\t" + block.toString());
		}
		System.out.println("");
		// DEBUG
		
		final List<ITemplateStep> tempSteps = template.getAllSteps();
		final Date lastTimePlaced = start;
		boolean success = false;
		for (int i = 0; i < tempSteps.size(); ++i) {
			// Get the number of blocks to place for this current step
			final ITemplateStep step = tempSteps.get(i);
			final double numHoursInStep = m_asgn.getExpectedHours() * step.getPercentOfTotal();
			numBlocksLeft = (int) Math.ceil(numHoursInStep / numHoursPerBlock);
			
			// TODO: Handle cases where the number of hours of a user-submitted Assignment
			// is exceedingly low
			
			success = tryUniformInsertion(allBlocks, start, end, lastTimePlaced, step, numBlocksLeft, numHoursPerBlock);
			
			//For the purposes of extensibility, we could try another insertion policy here.
			//Currently, we choose to throw an exception instead to indicate failure.
			if (!success) {
				throw new NotEnoughTimeException("Uniform insertion policy failed");
			}
		}
		
		// TODO: Then, decompact all AssignmentBlocks so that a user may have a break
		// from his/her work time. This decompact() function will consider several
		// heuristics including (1) putting assignments in their preferred time-of-day
		// (2) spacing them out to have breaks, (3) variety between different types of
		// assignments if there are several AssignmentBlocks in a row
		
		System.out.println("[Start, End] : [" + start + ", " + end + "]");
		System.out.println("Debug, allBlocks list before decompaction");
		for (int i = 0; i < allBlocks.size(); ++i) {
			final ITimeBlockable itb = allBlocks.get(i);
			System.out.println("Start: " + itb.getStart() + " || End: " + itb.getEnd());
		}
		
		TimeCompactor.decompact(allBlocks, start, end);
		
		System.out.println("DEBUG - printing out the time ranges of all blocks");
		for (int i = 0; i < allBlocks.size(); ++i) {
			final ITimeBlockable itb = allBlocks.get(i);
			System.out.println("Start: " + itb.getStart() + " || End: " + itb.getEnd() + itb.isMovable());
		}
		
		// Assign the value of this field so it may be accessed by the "getter"
		// function in this class
		m_localChangesToBlocks = allBlocks;
		
		// DEBUG added by Eric
		System.out.println("END: TimeAllocator: getting contents of allBlocks");
		for (final ITimeBlockable block : allBlocks) {
			System.out.println("\t" + block.toString());
		}
		System.out.println("");
		// DEBUG
		
	}
	
	private boolean tryUniformInsertion(final List<ITimeBlockable> allBlocks, final Date start, final Date end,
			Date lastTimePlaced, final ITemplateStep step, int numBlocksLeft, final double numHoursPerBlock) {
		
		boolean hasCompactedOnce = false;
		
		while (numBlocksLeft > 0) {
			// 1. Use find fit function for the next block (Best-Fit search policy)
			final AssignmentBlock block = findFit(allBlocks, numHoursPerBlock, (Date) lastTimePlaced.clone(),
					(Date) end.clone(), step);
			
			// 2. If no fit can be found, try compaction OR break the loop and move on to
			// the next type of insertion policy
			if (block == null) {
				
				if (!hasCompactedOnce) {
					// Compact existing blocks so that they fit better, and reset the lastTimePlaced
					// reference so that it is still accurate
					TimeCompactor.compact(allBlocks, start, end, lastTimePlaced);
					hasCompactedOnce = true;
					continue;					
				} 
				else {
					// TODO: Remove this println
					System.err.println("Could not insert block, even after compacting -- TODO:"
						+ " Try to move blocks contained by other assignments outside of the range\n"
						+ " OR use more sophisticated compaction around unmovable blocks\n"
						+ " OR return FAIL message to the user\n"
						+ " OR try breaking the remaining blocks into half-size pieces\n");
					
					return false;
				}
			}
			
			// 3. If a fit is found, insert the block into the list, decrement the counter
			// and continue.
			TimeUtilities.insertIntoSortedList(allBlocks, block);
//			final int ind = TimeUtilities.indexOfFitLocn(allBlocks, block.getStart());
//			allBlocks.add(ind, block);
			--numBlocksLeft;
			
			// 4. Reset the place that the last block was placed for future searches
			lastTimePlaced = block.getStart();
		}
		
		return true;
	}
	
	// Return a newly-initialized AssignmentBlock containing the relevant start/end
	// dates for the current chunk.
	private AssignmentBlock findFit(final List<ITimeBlockable> blockList, final double blockLength, final Date start,
			final Date end, final ITemplateStep step) {
		Date bestStart = null;
		Date bestEnd = null;
		long minTimeLeftover = Long.MAX_VALUE;
		final long blockLenInMillis = convertHoursToMillis(blockLength);
		long delta = 0;
		final ITask task = m_asgn.getTasks().get(step.getStepNumber());
		
		if (blockList.size() == 0) {
			bestStart = start;
			bestEnd = new Date(bestStart.getTime() + convertHoursToMillis(blockLength));
			
			// Get the corresponding task from the Assignment member variable
			return new AssignmentBlock(bestStart, bestEnd, task);
		}
		
		// Get free time between start time given and first block in list
		if (blockLenInMillis <= (delta = blockList.get(0).getStart().getTime() - start.getTime())) {
			bestStart = start;
			minTimeLeftover = delta - blockLenInMillis;
			bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
			return new AssignmentBlock(bestStart, bestEnd, task);
		}
		
		//Iterate so that items of different tasks under the same assignment are
		//placed in the calendar in correct chronological order
		for (int i = TimeUtilities.indexOfFitLocn(blockList, start); i < blockList.size(); ++i) {
			// Ignore this edge case
			if (i == 0) {
				continue;
			}
			// Get free time between two blocks in the list
			if (blockLenInMillis <= (delta = blockList.get(i).getStart().getTime()
				- blockList.get(i - 1).getEnd().getTime())
				&& delta - blockLenInMillis < minTimeLeftover) {
				
				bestStart = (Date) blockList.get(i - 1).getEnd().clone();
				minTimeLeftover = delta - blockLenInMillis;
				bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
				
				//Added this to make policy FIRST-FIT rather than best fit
				return new AssignmentBlock(bestStart, bestEnd, task);
			}
		}
		
		// Get free time between last block in list and end time given
		if (blockLenInMillis <= (delta = end.getTime() - blockList.get(blockList.size() - 1).getEnd().getTime())
			&& delta - blockLenInMillis < minTimeLeftover) {
			bestStart = (Date) blockList.get(blockList.size() - 1).getEnd().clone();
			minTimeLeftover = delta - blockLenInMillis;
			bestEnd = new Date(bestStart.getTime() + blockLenInMillis);
			
			//Added this to make policy FIRST-FIT rather than best fit
			return new AssignmentBlock(bestStart, bestEnd, task);
		}
		
		if (bestStart == null || bestEnd == null) {
			return null;
		}
		
		//TODO: REMOVE THIS RETURN STATEMENT, and clean this function up so that it is
		//		entirely "first-fit" based instead of "best-fit" based
		return new AssignmentBlock(bestStart, bestEnd, task);
	}
	
	private long convertHoursToMillis(final double hrs) {
		return (long) (hrs * 60 * 60 * 1000);
	}
	
	public List<ITimeBlockable> getEntireBlockSet() {
		return new ArrayList<ITimeBlockable>(m_localChangesToBlocks);
	}
	
}