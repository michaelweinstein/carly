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
	private static final double		MIN_BLOCK_LENGTH_HRS 	= 0.25;
	
	private final IAssignment		m_asgn;
	private List<ITimeBlockable>	m_localChangesToBlocks;
	private Date					m_lastTimePlaced;
	
	public TimeAllocator(final IAssignment asgn) {
		m_asgn = asgn;
		m_localChangesToBlocks = new ArrayList<ITimeBlockable>();
		m_lastTimePlaced = null;
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

		
		final List<ITemplateStep> tempSteps = template.getAllSteps();
		m_lastTimePlaced = new Date(start.getTime());
		boolean success = false;
		for (int i = 0; i < tempSteps.size(); ++i) {
			// Get the number of blocks to place for this current step
			final ITemplateStep step = tempSteps.get(i);
			final double numHoursInStep = m_asgn.getExpectedHours() * step.getPercentOfTotal();
			
			//Insert as many uniform blocks as possible that are of a consistent length
			final double exactNumBlocks = numHoursInStep / numHoursPerBlock;
			numBlocksLeft = (int) Math.floor(exactNumBlocks);
			success = tryUniformInsertion(allBlocks, start, end, step, numBlocksLeft, numHoursPerBlock);
			
			//For the purposes of extensibility, we could try another insertion policy here.
			//Currently, we choose to throw an exception instead to indicate failure.
			if (!success) {
				throw new NotEnoughTimeException("Uniform insertion policy failed");
			}
			
			//Now, insert the remaining time as a smaller block
			double numHrsLeftover = numHoursInStep - (numBlocksLeft * numHoursPerBlock);
			numBlocksLeft = 1;
			if(numHrsLeftover >= MIN_BLOCK_LENGTH_HRS)
				success = tryUniformInsertion(allBlocks, start, end, step, numBlocksLeft, numHrsLeftover);
			
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
			final ITemplateStep step, int numBlocksLeft, final double numHoursPerBlock) {
		
		boolean hasCompactedOnce = false;
		
		while (numBlocksLeft > 0) {
			// 1. Use find fit function for the next block (Best-Fit search policy)
			final AssignmentBlock block = findFit(allBlocks, numHoursPerBlock, (Date) m_lastTimePlaced.clone(),
					(Date) end.clone(), step);
			
			// 2. If no fit can be found, try compaction OR break the loop and move on to
			// the next type of insertion policy
			if (block == null) {
				
				if (!hasCompactedOnce) {
					// Compact existing blocks so that they fit better, and reset the lastTimePlaced
					// reference so that it is still accurate
					m_lastTimePlaced = TimeCompactor.compact(allBlocks, start, end, m_lastTimePlaced);
					hasCompactedOnce = true;
					continue;					
				} 
				else {
					return false;
				}
			}
			
			// 3. If a fit is found, insert the block into the list, decrement the counter
			// and continue.
			TimeUtilities.insertIntoSortedList(allBlocks, block);
			--numBlocksLeft;
			
			// 4. Reset the place that the last block was placed for future searches
			m_lastTimePlaced = block.getStart();
		}
		
		return true;
	}
	
	// Return a newly-initialized AssignmentBlock containing the relevant start/end
	// dates for the current chunk.
	private AssignmentBlock findFit(final List<ITimeBlockable> blockList, final double blockLength, final Date asgnStart,
			final Date asgnEnd, final ITemplateStep step) {
		Date fitStart = null;
		Date fitEnd = null;
		final long blockLenInMillis = convertHoursToMillis(blockLength);
		final ITask task = m_asgn.getTasks().get(step.getStepNumber());
		
		if (blockList.size() == 0) {
			fitStart = asgnStart;
			fitEnd = new Date(fitStart.getTime() + convertHoursToMillis(blockLength));
			
			// Get the corresponding task from the Assignment member variable
			return new AssignmentBlock(fitStart, fitEnd, task);
		}
		
		// Get free time between start time given and first block in list
		if (blockLenInMillis <= (blockList.get(0).getStart().getTime() - asgnStart.getTime())) {
			fitStart = asgnStart;
			fitEnd = new Date(fitStart.getTime() + blockLenInMillis);
			return new AssignmentBlock(fitStart, fitEnd, task);
		}
		
		//Iterate so that items of different tasks under the same assignment are
		//placed in the calendar in correct chronological order
		for (int i = TimeUtilities.indexOfFitLocn(blockList, asgnStart); i < blockList.size(); ++i) {
			// Ignore this edge case
			if (i == 0) {
				continue;
			}
			// Get free time between two blocks in the list
			if (blockLenInMillis <= (blockList.get(i).getStart().getTime()
				- blockList.get(i - 1).getEnd().getTime())) {
				
				fitStart = (Date) blockList.get(i - 1).getEnd().clone();
				fitEnd = new Date(fitStart.getTime() + blockLenInMillis);
				
				//Added this to make policy FIRST-FIT rather than best fit
				return new AssignmentBlock(fitStart, fitEnd, task);
			}
		}
		
		// Get free time between last block in list and end time given
		if (blockLenInMillis <= (asgnEnd.getTime() - blockList.get(blockList.size() - 1).getEnd().getTime())) {
			fitStart = (Date) blockList.get(blockList.size() - 1).getEnd().clone();
			fitEnd = new Date(fitStart.getTime() + blockLenInMillis);
			
			//Added this to make policy FIRST-FIT rather than best fit
			return new AssignmentBlock(fitStart, fitEnd, task);
		}
		
		//In this case, no fit was found anywhere in the time stream,
		//so return null to indicate failure
		return null;
	}
	
	private long convertHoursToMillis(final double hrs) {
		return (long) (hrs * 60 * 60 * 1000);
	}
	
	public List<ITimeBlockable> getEntireBlockSet() {
		return new ArrayList<ITimeBlockable>(m_localChangesToBlocks);
	}
	
}