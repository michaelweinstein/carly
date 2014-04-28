package backend.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import data.AssignmentBlock;
import data.ITask;
import data.ITimeBlockable;
import data.UnavailableBlock;

public class TimeModifier {
	
	public static boolean updateBlock(final ITimeBlockable block, final Date newStart, final Date newEnd) {
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		// TODO: Figure out with Eric -- what is the acceptable range of blocks here? Maybe should I
		// get the entire block set from the db...
		final Date tempStart = new Date(newStart.getTime() - (86400000 * 4));
		final Date tempEnd = new Date(newEnd.getTime() + (86400000 * 4));
		final List<AssignmentBlock> asgnBlocks = StorageService.getAllAssignmentBlocksWithinRange(tempStart, tempEnd);
		final List<UnavailableBlock> unavBlocks = StorageService.getAllUnavailableBlocksWithinRange(tempStart, tempEnd);
		final List<ITimeBlockable> allBlocks = TimeUtilities.zipTimeBlockLists(unavBlocks, asgnBlocks);
		final Date now = new Date();
		
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
				
				//This is the case where a block is being dragged over itself slightly
				if(prev.equals(block) && (curr == null || curr.getStart().getTime() >= newEnd.getTime())) {
					block.setStart(newStart);
					block.setEnd(newEnd);
					
					try{
						StorageService.updateTimeBlock(block);
						return true;
					}
					catch(StorageServiceException sse) {
						System.err.println("OH NOES");
						return false;
					}
				}
				
				//Otherwise, try to push other blocks backwards to make a fit
				if (!pushBlocksBack(allBlocks, block, now, newStart)) {
					return false;
				}
			}
			if (curr != null && curr.getStart().getTime() < newEnd.getTime()) {
				
				//This is the case where a block is being dragged over itself slightly
				if(curr.equals(block) && (prev == null || prev.getEnd().getTime() <= newStart.getTime())){
					block.setStart(newStart);
					block.setEnd(newEnd);
					
					try{
						StorageService.updateTimeBlock(block);
						return true;
					}
					catch(StorageServiceException sse) {
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
	
	private static boolean pushBlocksBack(final List<ITimeBlockable> allBlocks, final ITimeBlockable block,
			final Date now, final Date newStart) {
		final int ind = TimeUtilities.indexOfFitLocn(allBlocks, newStart);
		
		final ITimeBlockable prev = (ind > 0 ? allBlocks.get(ind - 1) : null);
		final ITimeBlockable curr = (ind < allBlocks.size() ? allBlocks.get(ind) : null);
		
		//TODO: FOR NOW, IF PREV IS NULL, RETURN FOR SAFETY CONCERNS
		//		--If I end up getting the entire block set when calling this function, then there is no concern here
		if(prev == null || curr == null)
			return false;
		
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
	
	private static boolean pushBlocksForward(final List<ITimeBlockable> allBlocks, final ITimeBlockable block,
			final Date now, final Date newStart, final Date newEnd) {
		final int ind = TimeUtilities.indexOfFitLocn(allBlocks, newStart);
		
		final ITimeBlockable curr = (ind < allBlocks.size() ? allBlocks.get(ind) : null);
		final ITimeBlockable next = (ind < allBlocks.size() - 1 ? allBlocks.get(ind + 1) : null);
		
		//TODO: FOR NOW, IF NEXT IS NULL, RETURN FOR SAFETY CONCERNS
		//		--If I end up getting the entire block set when calling this function, then there is no concern here
		if(next == null || curr == null)
			return false;
		
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
	
	// This function is called when a user pulls on a slider to convey the message that
	// they are changing how much progress they have made on completing a particular Task.
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
		final Date tempEnd = new Date(209769820398203L);
		final List<AssignmentBlock> asgnBlocks = StorageService.getAllAssignmentBlocksWithinRange(tempStart, tempEnd);
		final List<UnavailableBlock> unavBlocks = StorageService.getAllUnavailableBlocksWithinRange(tempStart, tempEnd);
		final List<ITimeBlockable> allBlocks = TimeUtilities.zipTimeBlockLists(unavBlocks, asgnBlocks);
		
		
		final Date now = new Date(); // this Date captures where the user is and how much work they've done
		final Date due = StorageService.getAssignment(task.getAssignmentID()).getDueDate();
		double currProgress = 0.0;
		double pctToAdjust = 0.0;
		//This list contains all blocks that represent the current task, in order sorted by start date
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
			final long blockLengthInMillis = (long)(blockLengthInHours * 60 * 60 * 1000);
			
			// Add as many full-size blocks as possible, then add on extra time to other blocks
			while (currInd < allBlocks.size() && totalMillisToAdd > 0) {
				//Ignore this edge case
				if(currInd == 0)
					continue;
				
				ITimeBlockable b1 = allBlocks.get(currInd - 1);
				ITimeBlockable b2 = allBlocks.get(currInd);
				
				//Exit the loop early if past the Assignment's due date
				if(!b2.getStart().before(due))
					break;
				
				//TODO: Exit the loop early if either b1 or b2 is on a Task that occurs
				//after the current Task chronologically
				//--PROBLEM: Tasks have no way of knowing what their "TemplateStep" number is
				//if(b1.getTask().getAssignmentID().equals(task.getAssignmentID()) && task.get(???))
				
				
				//Determine if there is space for a block in between b1 and b2
				if(b2.getStart().getTime() - b1.getEnd().getTime() >= blockLengthInMillis) {
					//Insert a block, starting at b1's end
					AssignmentBlock x = new AssignmentBlock((Date) b1.getEnd().clone(),
							(Date) b2.getStart().clone(), task);
					TimeUtilities.insertIntoSortedList(allBlocks, x);
					TimeUtilities.insertIntoSortedList(taskBlocks, x);
					
					totalMillisToAdd -= blockLengthInMillis;
					currInd = startInd;
				}
				else
					++currInd;
			}
			
			//Add whatever leftover time there is to-add to the currently existing blocks
			for(int i = 1; i < allBlocks.size() - 1 && totalMillisToAdd > 0; ++i) {
				
				ITimeBlockable b1 = allBlocks.get(i);
				ITimeBlockable b2 = allBlocks.get(i + 1);
				
				if(b1.getTaskId().equals(task.getTaskID())) {
					long between = getMillisBetween(b1, b2);
					
					//Add whatever time is possible
					if(between > 0) {
						long amtToAdd = Math.min(between, totalMillisToAdd);
						b1.setEnd(new Date(b1.getEnd().getTime() + amtToAdd));
						totalMillisToAdd -= amtToAdd;
						
						try {
							StorageService.updateTimeBlock(b1);
						} catch (StorageServiceException e) {
							e.printStackTrace();
						}
					}	
				}
			}
			
			//TODO: At this point, return to the user (regardless of how many millis
			//		were successfully added).  This is a pretty extreme edge case that
			//		the schedule would be so tight that no blocks could be added anyway...
			if(totalMillisToAdd > 0) {
				System.err.println("Total Millis To Add > 0 - sleeping for 5 seconds as punishment :(");
				try{
					Thread.sleep(5000);
				}
				catch(InterruptedException ie) {}
			}
		}
		// 5b. The user is ahead, so remove a bit of time from each block
		else if (pctToAdjust > 0) {
			long totalMillisToRemove = (long) (pctToAdjust * taskLengthInMillis);
			
			final int startInd = TimeUtilities.indexOfFitLocn(allBlocks, now);
			int numFutureBlocks = taskBlocks.size() - startInd;
			int currInd = taskBlocks.size() - 1;
			
			// Remove as many blocks as possible, then remove a fixed amount from one block
			while (currInd >= startInd && totalMillisToRemove > 0) {
				final ITimeBlockable itb = allBlocks.get(currInd);
				final long blockLen = itb.getLength();
				if (blockLen <= totalMillisToRemove) {
					// Remove the block from both the local list and the StorageService
					allBlocks.remove(currInd);
					StorageService.removeTimeBlock(itb);
					
					// Subtract the number of millis removed and the number of future blocks
					--numFutureBlocks;
					totalMillisToRemove -= blockLen;
					
					// Restart the loop from the end of the list
					currInd = allBlocks.size() - 1;
				}
			}
			
			final long avgTimeToRemove = totalMillisToRemove / numFutureBlocks;
			// If there is still some time left to remove, remove a bit of time from each block
			// TODO: Alternate policy = remove all of it from one block
			if (totalMillisToRemove > 0) {
				for (int i = allBlocks.size() - 1; i >= startInd; ++i) {
					final ITimeBlockable block = allBlocks.get(i);
					block.setEnd(new Date(block.getEnd().getTime() - avgTimeToRemove));
					
					try {
						StorageService.updateTimeBlock(block);
					} catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
				}
			}
		}
		
		// If the percent to-adjust-to is the same as the current amount done, there's 
		// nothing left to do
	}
	
	//Return the number of milliseconds between the end of "b1" and the start of "b2"
	private static long getMillisBetween(ITimeBlockable b1, ITimeBlockable b2) {
		return b2.getStart().getTime() - b1.getEnd().getTime();
	}
	
}
