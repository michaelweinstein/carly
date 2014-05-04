package backend.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import data.Assignment;
import data.AssignmentBlock;
import data.ITask;
import data.ITimeBlockable;
import data.UnavailableBlock;

public class TimeModifier {
	
	private static final long ETERNITY = 209769820398203L;
	
	/**
	 * Takes in a block, and a new start/end time and updates the block in the database. The types of operations that
	 * merit the use of this function are (1) lengthening a block, (2) shortening a block, and (3) dragging a block.
	 * 
	 * @param block The block to be updated in the database
	 * @param newStart The new start time for the parameter block
	 * @param newEnd The new end time for the parameter block
	 * @return a boolean indicating whether or not the operation was successful
	 */
	public static boolean updateBlock(final ITimeBlockable block, final Date newStart, final Date newEnd) {
		final Date now = new Date();
		final Assignment asgn = StorageService.getAssignment(block.getTask().getAssignmentID());
		
		// Ensure that a user cannot lengthen, shorten, or drag a block outside of the valid time range
		if (!newStart.after(now) || !newEnd.before(asgn.getDueDate())) {
			return false;
		}
		
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		final Date tempStart = new Date(0);
		final Date tempEnd = new Date(ETERNITY);
		final List<AssignmentBlock> asgnBlocks = StorageService.getAllAssignmentBlocksWithinRange(tempStart, tempEnd);
		final List<UnavailableBlock> unavBlocks = StorageService.getAllUnavailableBlocksWithinRange(tempStart, tempEnd);
		final List<ITimeBlockable> allBlocks = TimeUtilities.zipTimeBlockLists(unavBlocks, asgnBlocks);
		
		final Date currStart = block.getStart();
		final Date currEnd = block.getEnd();
		
		// Shortening a block from the top
		if (currStart.compareTo(newStart) < 0 && currEnd.compareTo(newEnd) == 0) {
			block.setStart(newStart);
			try {
				StorageService.updateTimeBlock(block);
				return true;
			} catch (final StorageServiceException e) {
				e.printStackTrace();
			}
		}
		// Shortening a block from the bottom
		else if (currStart.compareTo(newStart) == 0 && currEnd.compareTo(newEnd) > 0) {
			block.setEnd(newEnd);
			try {
				StorageService.updateTimeBlock(block);
				return true;
			} catch (final StorageServiceException e) {
				e.printStackTrace();
			}
		}
		// Lengthening a block from the top
		else if (currStart.compareTo(newStart) > 0 && currEnd.compareTo(newEnd) == 0) {
			return pushBlocksBack(allBlocks, block, now, newStart);
		}
		// Lengthening a block from the bottom
		else if (currStart.compareTo(newStart) == 0 && currEnd.compareTo(newEnd) < 0) {
			return pushBlocksForward(allBlocks, block, now, newStart, newEnd);
		}
		// Otherwise the block has been dragged
		else {
			final int ind = TimeUtilities.indexOfFitLocn(allBlocks, newStart);
			
			final ITimeBlockable prev = (ind > 0 ? allBlocks.get(ind - 1) : null);
			final ITimeBlockable curr = (ind <= allBlocks.size() - 1 ? allBlocks.get(ind) : null);
			
			// Try a switch operation if the starts/ends line up and no due date violations occur
			if (curr != null && curr.getStart().equals(newStart) && curr.getEnd().equals(newEnd)) {
				return TimeUtilities.switchTimeBlocks(allBlocks, block, curr);
			}
			
			// Make sure the new block is not overlapping the bounds of "prev" and "curr" -- if so,
			// push the others backwards/forwards, respectively, to make room.
			if (prev != null && prev.getEnd().getTime() > newStart.getTime()) {
				
				// This is the case where a block is being dragged over itself slightly
				if (prev.equals(block) && (curr == null || curr.getStart().getTime() >= newEnd.getTime())) {
					block.setStart(newStart);
					block.setEnd(newEnd);
					
					try {
						StorageService.updateTimeBlock(block);
						return true;
					} catch (final StorageServiceException sse) {
						System.err.println("OH NOES");
						return false;
					}
				}
				
				// Otherwise, try to push other blocks backwards to make a fit
				if (!pushBlocksBack(allBlocks, block, now, newStart)) {
					return false;
				}
			}
			if (curr != null && curr.getStart().getTime() < newEnd.getTime()) {
				
				// This is the case where a block is being dragged over itself slightly
				if (curr.equals(block) && (prev == null || prev.getEnd().getTime() <= newStart.getTime())) {
					block.setStart(newStart);
					block.setEnd(newEnd);
					
					try {
						StorageService.updateTimeBlock(block);
						return true;
					} catch (final StorageServiceException sse) {
						System.err.println("OH NOES");
						return false;
					}
				}
				
				if (!pushBlocksForward(allBlocks, block, now, newStart, newEnd)) {
					return false;
				}
			}
			
			// Once any overlapping conflicts are resolved, update the db
			block.setStart(newStart);
			block.setEnd(newEnd);
			
			try {
				StorageService.updateTimeBlock(block);
				return true;
			} catch (final StorageServiceException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * Given the parameter list of all blocks, attempts to compact blocks that are before the parameter "block" so that
	 * it can fit at the time "newStart"
	 * 
	 * @param allBlocks a list of all blocks (unavailable and assignment) for access
	 * @param block The block to start from
	 * @param now The time that the function the called this function began
	 * @param newStart The new start time for the parameter block
	 * @return a boolean indicating whether or not the operation was successful
	 */
	private static boolean pushBlocksBack(final List<ITimeBlockable> allBlocks, final ITimeBlockable block,
			final Date now, final Date newStart) {
		final int ind = TimeUtilities.indexOfFitLocn(allBlocks, newStart);
		
		final ITimeBlockable prev = (ind > 0 ? allBlocks.get(ind - 1) : null);
		final ITimeBlockable curr = (ind < allBlocks.size() ? allBlocks.get(ind) : null);
		
		// TODO: FOR NOW, IF PREV IS NULL, RETURN FOR SAFETY CONCERNS
		// --If I end up getting the entire block set when calling this function, then there is no concern here
		if (prev == null || curr == null) {
			return false;
		}
		
		// In this case, check to see if newStart overlaps prev's end
		if (prev != null && prev.getEnd().getTime() > newStart.getTime()) {
			final long timeDiff = prev.getEnd().getTime() - newStart.getTime();
			
			// No block in front of prev -- use the "now" block at the top of this function for comparison
			if (ind - 1 == 0) {
				if (prev.getStart().getTime() - now.getTime() >= timeDiff) {
					// If there is space to push prev back, update its time ranges and reset curr's range
					final Date newPrevStart = new Date(prev.getStart().getTime() - timeDiff);
					prev.setStart(newPrevStart);
					prev.setEnd(new Date(newPrevStart.getTime() + prev.getLength()));
					curr.setStart(newStart);
					
					// Update the blocks in the db
					try {
						StorageService.updateTimeBlock(prev);
						StorageService.updateTimeBlock(curr);
					} catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
					return true;
				}
				// Not enough space between "now" and "prev" to be able to push "prev" back
				return false;
			}
			// Otherwise, use the block in front of prev for comparison
			final ITimeBlockable pp = allBlocks.get(ind - 2);
			
			// TEMP: for now, I just check to see if there is space to push prev back.
			if (prev.getStart().getTime() - pp.getEnd().getTime() >= timeDiff) {
				// If there is space to push prev back, update its time ranges and reset curr's range
				final Date newPrevStart = new Date(prev.getStart().getTime() - timeDiff);
				prev.setStart(newPrevStart);
				prev.setEnd(new Date(newPrevStart.getTime() + prev.getLength()));
				curr.setStart(newStart);
				
				// Update the blocks in the db
				try {
					StorageService.updateTimeBlock(prev);
					StorageService.updateTimeBlock(curr);
				} catch (final StorageServiceException sse) {
					sse.printStackTrace();
				}
				return true;
			}
			// Not enough space between "now" and "prev" to be able to push "prev" back
			return false;
		}
		// No overlap occurs, so just update the block in the db
		block.setStart(newStart);
		
		try {
			StorageService.updateTimeBlock(block);
			return true;
		} catch (final StorageServiceException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Given the parameter list of all blocks, attempts to compact blocks that are after the parameter "block" so that
	 * it can fit at the time "newEnd"
	 * 
	 * @param allBlocks a list of all blocks (unavailable and assignment) for access
	 * @param block The block to start from
	 * @param now The time that the function the called this function began
	 * @param newStart The new start time for the parameter block
	 * @param newEnd The new end time for the parameter block
	 * @return a boolean indicating whether or not the operation was successful
	 */
	private static boolean pushBlocksForward(final List<ITimeBlockable> allBlocks, final ITimeBlockable block,
			final Date now, final Date newStart, final Date newEnd) {
		final int ind = TimeUtilities.indexOfFitLocn(allBlocks, newStart);
		
		final ITimeBlockable curr = (ind < allBlocks.size() ? allBlocks.get(ind) : null);
		final ITimeBlockable next = (ind < allBlocks.size() - 1 ? allBlocks.get(ind + 1) : null);
		
		// TODO: FOR NOW, IF NEXT IS NULL, RETURN FOR SAFETY CONCERNS
		// --If I end up getting the entire block set when calling this function, then there is no concern here
		if (next == null || curr == null) {
			return false;
		}
		
		// In this case, check to see if newEnd overlaps next's start
		if (next != null && next.getStart().getTime() < newEnd.getTime()) {
			final long timeDiff = newEnd.getTime() - next.getStart().getTime();
			
			// No block after "next" -- use the due date for comparison
			if (ind + 1 == 0) {
				final Date due = StorageService.getAssignment(curr.getTask().getAssignmentID()).getDueDate();
				
				if (due.getTime() - next.getEnd().getTime() >= timeDiff) {
					// If there is space to push "next" forward, update its time ranges and reset curr's range
					final Date newNextStart = new Date(next.getStart().getTime() + timeDiff);
					next.setStart(newNextStart);
					next.setEnd(new Date(newNextStart.getTime() + next.getLength()));
					curr.setEnd(newEnd);
					
					// Update the blocks in the db
					try {
						StorageService.updateTimeBlock(curr);
						StorageService.updateTimeBlock(next);
					} catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
					return true;
				}
				// Not enough space between "now" and "prev" to be able to push "prev" back
				else {
					return false;
				}
			}
			// Otherwise, use the block after "next" for comparison
			else {
				final ITimeBlockable nn = allBlocks.get(ind + 2);
				
				// TEMP: for now, I just check to see if there is space to push next forward.
				if (nn.getStart().getTime() - next.getEnd().getTime() >= timeDiff) {
					// If there is space to push prev back, update its time ranges and reset curr's range
					final Date newNextStart = new Date(next.getStart().getTime() + timeDiff);
					next.setStart(newNextStart);
					next.setEnd(new Date(newNextStart.getTime() + next.getLength()));
					curr.setEnd(newEnd);
					
					// Update the blocks in the db
					try {
						StorageService.updateTimeBlock(curr);
						StorageService.updateTimeBlock(next);
					} catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
					return true;
				}
				
				// Not enough space between "now" and "next" to be able to push "next" forward
				return false;
			}
			
		}
		// No overlap occurs, so just update the block in the db
		else {
			block.setEnd(newEnd);
			
			try {
				StorageService.updateTimeBlock(block);
				return true;
			} catch (final StorageServiceException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	/**
	 * This function is called when a user pulls on a slider to convey the message that they are changing how much
	 * progress they have made on completing a particular Task. This function will modify blocks in the same task as the
	 * parameter "task" so that extra time is added or removed to reflect the user change in percent-complete.
	 * 
	 * @param task The task whose blocks will be updated.
	 * @param newPct The new percent-complete to-be-set in the parameter task.
	 */
	public static void updateBlocksInTask(final ITask task, final double newPct) {
		
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		final Date tempStart = new Date(0);
		final Date tempEnd = new Date(ETERNITY);
		final List<AssignmentBlock> asgnBlocks = StorageService.getAllAssignmentBlocksWithinRange(tempStart, tempEnd);
		final List<UnavailableBlock> unavBlocks = StorageService.getAllUnavailableBlocksWithinRange(tempStart, tempEnd);
		final List<ITimeBlockable> allBlocks = TimeUtilities.zipTimeBlockLists(unavBlocks, asgnBlocks);
		
		final long minLengthInMillis = (long) (TimeAllocator.MIN_BLOCK_LENGTH_HRS * 60 * 60 * 1000);
		
		final Date now = new Date(); // this Date captures where the user is and how much work they've done
		final Date due = StorageService.getAssignment(task.getAssignmentID()).getDueDate();
		double currProgress = 0.0;
		double pctToAdjust = 0.0;
		// This list contains all blocks that represent the current task, in order sorted by start date
		final List<ITimeBlockable> taskBlocks = new ArrayList<ITimeBlockable>();
		long taskLengthInMillis = 0;
		
		// 1. Get all Blocks that are part of the current Task, "task"
		for (int i = 0; i < allBlocks.size(); ++i) {
			final ITimeBlockable block = allBlocks.get(i);
			if (block.getTaskId().equals(task.getTaskID())) {
				taskBlocks.add(block);
				taskLengthInMillis += block.getLength();
			}
		}
		
		// 2. Using the time in "now", figure out how much the user should have completed
		// (in units of percentage) of that Task.
		for (final ITimeBlockable bl : taskBlocks) {
			// If a block is entirely in the past
			if (bl.getStart().getTime() < now.getTime() && bl.getEnd().getTime() <= now.getTime()) {
				currProgress += (double) bl.getLength() / taskLengthInMillis;
			}
			// If a block is currently being worked on
			else if (bl.getStart().getTime() < now.getTime() && bl.getEnd().getTime() > now.getTime()) {
				currProgress += (double) (now.getTime() - bl.getStart().getTime()) / taskLengthInMillis;
			}
		}
		
		// 3. Examine the actual percent complete (the parameter "newPct")
		pctToAdjust = newPct - currProgress;
		
		// 4. Determine whether this is an addition or subtraction operation based on the sign
		// of the difference between step (2) and step (3).
		
		// 5a. The user is behind, so add a bit of time to each block if possible, or insert new blocks
		// if necessary.
		if (pctToAdjust < 0) {
			long totalMillisToAdd = (long) (Math.abs(pctToAdjust) * taskLengthInMillis);
			
			final int startInd = TimeUtilities.indexOfFitLocn(allBlocks, now);
			int currInd = startInd;
			final double blockLengthInHours = task.getSuggestedBlockLength();
			final long blockLengthInMillis = (long) (blockLengthInHours * 60 * 60 * 1000);
			
			// Get the step number of the current Task in this assignment
			final Assignment asgn = StorageService.getAssignment(task.getAssignmentID());
			final int taskIndex = asgn.getTasks().indexOf(task);
			
			// Add as many full-size blocks as possible, then add on extra time to other blocks
			while (currInd < allBlocks.size() && totalMillisToAdd > minLengthInMillis) {
				// Ignore this edge case
				if (currInd == 0) {
					continue;
				}
				
				final ITimeBlockable b1 = allBlocks.get(currInd - 1);
				final ITimeBlockable b2 = allBlocks.get(currInd);
				
				// Exit the loop early if past the Assignment's due date
				if (!b2.getStart().before(due)) {
					break;
				}
				
				// Exit the loop early if either b1 or b2 is on a Task that occurs
				// after the current Task chronologically
				final Assignment b1asgn = StorageService.getAssignment(b1.getTask().getAssignmentID());
				if (b1asgn.getID().equals(task.getAssignmentID()) && b1asgn.getTasks().indexOf(task) > taskIndex) {
					break;
				}
				
				final long insLen = Math.min(blockLengthInMillis, totalMillisToAdd);
				
				// Determine if there is space for a block in between b1 and b2
				if (b2.getStart().getTime() - b1.getEnd().getTime() >= insLen) {
					// Insert a block, starting at b1's end
					
					// Get the start/end time of the new block
					final Date insEnd = new Date(b2.getStart().getTime());
					final Date insStart = new Date(insEnd.getTime() - insLen);
					
					// Insert the block into the local copies of the lists
					final AssignmentBlock x = new AssignmentBlock(insStart, insEnd, task);
					TimeUtilities.insertIntoSortedList(allBlocks, x);
					TimeUtilities.insertIntoSortedList(taskBlocks, x);
					
					// Insert the block into the database
					try {
						StorageService.addTimeBlock(x);
					} catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
					
					// Decrement the number of millis to add and restart the loop
					totalMillisToAdd -= insLen;
					currInd = startInd;
				} else {
					++currInd;
				}
			}
			
			// Add whatever leftover time there is to-add to the currently existing blocks
			for (int i = 1; i < allBlocks.size() - 1 && totalMillisToAdd > minLengthInMillis; ++i) {
				
				final ITimeBlockable b1 = allBlocks.get(i);
				final ITimeBlockable b2 = allBlocks.get(i + 1);
				
				if (b1.getTaskId().equals(task.getTaskID())) {
					final long between = getMillisBetween(b1, b2);
					
					// Add whatever time is possible
					if (between > 0) {
						final long amtToAdd = Math.min(between, totalMillisToAdd);
						b1.setEnd(new Date(b1.getEnd().getTime() + amtToAdd));
						totalMillisToAdd -= amtToAdd;
						
						try {
							StorageService.updateTimeBlock(b1);
						} catch (final StorageServiceException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			// TODO: At this point, return to the user (regardless of how many millis
			// were successfully added). This is a pretty extreme edge case that
			// the schedule would be so tight that no blocks could be added anyway...
			if (totalMillisToAdd > minLengthInMillis) {
				System.err.println("Total Millis To Add > 0 - sleeping for 5 seconds as punishment :(");
				try {
					Thread.sleep(5000);
				} catch (final InterruptedException ie) {}
			}
			
			// Blocks were just inserted, so decompact them
			TimeCompactor.decompact(allBlocks, now, tempEnd);
			
		}
		// 5b. The user is ahead, so remove a bit of time from each block
		else if (pctToAdjust > 0) {
			long totalMillisToRemove = (long) (pctToAdjust * taskLengthInMillis);
			
			final int startInd = TimeUtilities.indexOfFitLocn(taskBlocks, now);
			int numFutureBlocks = taskBlocks.size() - startInd;
			int currInd = taskBlocks.size() - 1;
			
			// Remove as many blocks as possible, then remove a fixed amount from one block
			while (currInd >= startInd && totalMillisToRemove > 0) {
				
				final ITimeBlockable itb = taskBlocks.get(currInd);
				final long blockLen = itb.getLength();
				if (blockLen <= totalMillisToRemove) {
					// Remove the block from both the local list and the StorageService
					allBlocks.remove(itb);
					taskBlocks.remove(itb);
					StorageService.removeTimeBlock(itb);
					
					// Subtract the number of millis removed and the number of future blocks
					--numFutureBlocks;
					totalMillisToRemove -= blockLen;
					
					// Restart the loop from the end of the list
					currInd = taskBlocks.size() - 1;
				} else {
					--currInd;
				}
			}
			
			// Removed as much as possible, regardless of what the user has input
			if (numFutureBlocks == 0) {
				return;
			}
			long avgTimeToRemove = totalMillisToRemove / numFutureBlocks;
			
			// If there is still some time left to remove, remove a bit of time from each block
			if (totalMillisToRemove > 0) {
				for (int i = taskBlocks.size() - 1; i >= taskBlocks.size() - numFutureBlocks; --i) {
					final ITimeBlockable block = taskBlocks.get(i);
					
					// If removing time from a block makes it shorter than the minimum block length,
					// don't remove any time, then reset the average time to remove from each block
					if (block.getLength() - avgTimeToRemove < TimeAllocator.MIN_BLOCK_LENGTH_HRS * 60 * 60 * 1000) {
						--numFutureBlocks;
						if (numFutureBlocks == 0) {
							break;
						} else {
							avgTimeToRemove = totalMillisToRemove / numFutureBlocks;
						}
					} else {
						block.setEnd(new Date(block.getEnd().getTime() - avgTimeToRemove));
						
						// Update any block that is changed
						try {
							StorageService.updateTimeBlock(block);
						} catch (final StorageServiceException sse) {
							sse.printStackTrace();
						}
					}
				}
			}
		}
		
		//Save the new percent-complete of the Task that was just updated and 
		//push to the StorageService
		task.setPercentComplete(newPct);
		StorageService.updateTask(task);
	}
	
	/**
	 * Return the number of milliseconds between the end of "b1" and the start of "b2"
	 * 
	 * @param b1 An ITimeBlockable that ends before b2 starts
	 * @param b2 An ITimeBlockable that starts after b1 ends
	 * @return the number of milliseconds between the two parameter blocks.
	 */
	private static long getMillisBetween(final ITimeBlockable b1, final ITimeBlockable b2) {
		return b2.getStart().getTime() - b1.getEnd().getTime();
	}
	
}
