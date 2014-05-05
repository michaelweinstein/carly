package backend.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import data.AssignmentBlock;
import data.IAssignment;
import data.ITask;
import data.ITimeBlockable;
import data.UnavailableBlock;

public class TimeUtilities {
	
	/**
	 * Inserts the parameter ITimeBlockable into the parameter List.  This list is sorted in order
	 * by the start Date in each ITimeBlockable.
	 * @param allBlocks a List of ITimeBlockables
	 * @param block a block that will be inserted into the parameter List upon exiting this function.
	 */
	public static void insertIntoSortedList(final List<ITimeBlockable> allBlocks, final ITimeBlockable block) {
		int ind;
		final int size = allBlocks.size();
		
		if (allBlocks.size() == 0) {
			allBlocks.add(block);
			return;
		}
		
		// Compare to the first element and last element in the list
		if (block.getStart().compareTo(allBlocks.get(0).getStart()) <= 0) {
			allBlocks.add(0, block);
			return;
		} else if (block.getStart().compareTo(allBlocks.get(size - 1).getStart()) > 0) {
			allBlocks.add(allBlocks.size(), block);
			return;
		}
		
		// Compare to all surrounding pairs in the middle
		for (ind = 1; ind < size; ++ind) {
			if (block.getStart().compareTo(allBlocks.get(ind - 1).getEnd()) >= 0
				&& block.getStart().compareTo(allBlocks.get(ind).getStart()) <= 0) {
				allBlocks.add(ind, block);
				return;
			}
		}
	}
	
	/**
	 * Returns the index in the list where the parameter "curr" would appear if it were to be
	 * inserted into the list of ITimeBlockables.  Note that the list is sorted by start dates.
	 * @param timeList a List of ITimeBlockable
	 * @param curr a Date object.
	 * @return the index of the location where the parameter date would fit if it were inserted
	 *         into the list.
	 */
	public static int indexOfFitLocn(final List<ITimeBlockable> timeList, final Date curr) {
		int ind = -1;
		final int size = timeList.size();
		
		if (timeList.size() == 0) {
			return 0;
		}
		
		// Compare to the first element and last element in the list
		if (curr.compareTo(timeList.get(0).getStart()) <= 0) {
			return 0;
		} else if (curr.compareTo(timeList.get(size - 1).getStart()) > 0) {
			return size;
		}
		
		// Compare to all surrounding pairs in the middle
		for (ind = 1; ind < size; ++ind) {
			if (curr.compareTo(timeList.get(ind - 1).getStart()) > 0
				&& curr.compareTo(timeList.get(ind).getStart()) <= 0) {
				return ind;
			}
		}
		
		return ind;
	}
	
	/**
	 * Counts the number of free hours in between each pair of blocks in the parameter List
	 * of ITimeBlockables, then determines if the parameter IAssignment may be able to fit in
	 * the range [start, asgn.getDueDate()].  Note that this function may return true, yet a 
	 * fit may still not exist based on the movability of blocks in the parameter List.
	 * @param allBlocks A List of ITimeBlockables sorted by start Date
	 * @param asgn An IAssignment with an expected number of hours
	 * @param start The time to begin counting free hours in the parameter List
	 * @return Returns true if the number of hours in between blocks in the parameter List is
	 * 		   greater than or equal to the expected number of hours in "asgn"; false otherwise.
	 */
	public static boolean existsPossibleFit(final List<ITimeBlockable> allBlocks, final IAssignment asgn,
			final Date start) {
		long amtFreeTime = 0;
		
		// If the list has no blocks in it, there is a fit as long as the range is big enough
		if (allBlocks.size() == 0) {
			final long hours = Math.round(asgn.getExpectedHours());
			final long min = Math.round((asgn.getExpectedHours() - hours) * 60);
			return TimeUnit.MILLISECONDS.convert(hours * 60 + min, TimeUnit.MINUTES) <= asgn.getDueDate().getTime()
				- start.getTime();
		}
		
		// Add time between start and first block
		if (allBlocks.size() > 0) {
			//Use the max function to avoid adding a negative amount of time
			amtFreeTime += Math.max(allBlocks.get(0).getStart().getTime() - start.getTime(), 0);
		}
		
		// Add time between blocks
		for (int i = 0; i < allBlocks.size() - 1; ++i) {
			final ITimeBlockable b1 = allBlocks.get(i);
			final ITimeBlockable b2 = allBlocks.get(i + 1);
			amtFreeTime += b2.getStart().getTime() - b1.getEnd().getTime();
		}
		
		// Add time between last block and end
		if (allBlocks.size() > 0) {
			//Use the max function to avoid adding a negative amount of time
			amtFreeTime += Math.max(0, asgn.getDueDate().getTime() - 
					allBlocks.get(allBlocks.size() - 1).getEnd().getTime());
		}
		
		final double numFreeHours = TimeUnit.HOURS.convert(amtFreeTime, TimeUnit.MILLISECONDS);
		System.out.println("Trying to find possible fit: num free hours = " + numFreeHours);
		return (numFreeHours >= asgn.getExpectedHours());
	}
	
	/**
	 * 
	 * @param unavailable A List of UnavailableBlocks sorted by start Date
	 * @param curr_asgns A List of AssignmentBlocks sorted by start Date
	 * @return A List containing all elements of the two parameter lists, zipped together
	 * 			in order sorted by start Date.
	 */
	public static List<ITimeBlockable> zipTimeBlockLists(final List<UnavailableBlock> unavailable,
			final List<AssignmentBlock> curr_asgns) {
		final List<ITimeBlockable> zippedList = new ArrayList<ITimeBlockable>(unavailable.size() + curr_asgns.size());
		
		int unavailInd = 0;
		int asgnInd = 0;
		
		// DEBUG added by Eric
		System.out.println("\nTimeAllocator: zipTimeBlockLists: printing out assignment blocks");
		for (final AssignmentBlock block : curr_asgns) {
			System.out.println("\t" + block.toString());
		}
		System.out.println("");
		// DEBUG
		
		// Iterate over the contents of these two arrays, then return a zipped list containing
		// the contents of both lists, in sorted order
		UnavailableBlock[] unavail = new UnavailableBlock[unavailable.size()];
		AssignmentBlock[] asgn = new AssignmentBlock[curr_asgns.size()];
		unavail = unavailable.toArray(unavail);
		asgn = curr_asgns.toArray(asgn);
		
		while (unavailInd < unavail.length || asgnInd < asgn.length) {
			
			if (unavailInd == unavail.length) {
				zippedList.add(asgn[asgnInd]);
				++asgnInd;
				continue;
			}
			if (asgnInd == asgn.length) {
				zippedList.add(unavail[unavailInd]);
				++unavailInd;
				continue;
			}
			
			final int comp = unavail[unavailInd].compareTo(asgn[asgnInd]);
			
			if (comp < 0) {
				zippedList.add(unavail[unavailInd]);
				++unavailInd;
			} else if (comp > 0) {
				zippedList.add(asgn[asgnInd]);
				++asgnInd;
			}
		}
		
		return zippedList;
	}
	
	/**
	 * 
	 * @param allBlocks A List of ITimeBlockables.
	 * @return a String representing the contents of the parameter List of ITimeBlockables.
	 */
	public static String printSchedule(final List<ITimeBlockable> allBlocks) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < allBlocks.size(); ++i) {
			final ITimeBlockable itb = allBlocks.get(i);
			builder.append("Start: ");
			builder.append(itb.getStart());
			builder.append(" || End: ");
			builder.append(itb.getEnd());
			builder.append("\n");
		}
		
		return builder.toString();
	}
	
	/**
	 * Attempts to switch the two parameter ITimeBlockables.  In order for ITimeBlockables to be switched,
	 * there must not be conflicts with the due Date of either Assignment upon switching, and there must
	 * be sufficient free space surrounding the two ITimeBlockables so that there is not an overlap
	 * after conducting a switch.  If the switch is successful, the two ITimeBlockables are updated in
	 * the StorageService.
	 * @param allBlocks A List of ITimeBlockables sorted by start Date
	 * @param source an ITimeBlockable to-be-switched with dest
	 * @param dest an ITimeBlockable to-be-switched with source
	 * @return Returns true if the operation was successful; false otherwise.
	 */
	public static boolean switchTimeBlocks(final List<ITimeBlockable> allBlocks, final ITimeBlockable source,
			final ITimeBlockable dest) {
		
		ITimeBlockable beforeBlock, afterBlock;
		// Determine which block is first, chronologically
		if (source.getStart().before(dest.getStart())) {
			beforeBlock = source;
			afterBlock = dest;
		} else {
			beforeBlock = dest;
			afterBlock = source;
		}
		
		// Get the lengths and the Assignment object corresponding to each block
		final long beforeLen = beforeBlock.getLength();
		final long afterLen = afterBlock.getLength();
		final IAssignment befAsgn = StorageService.getAssignment(beforeBlock.getTask().getAssignmentID());
		
		// Check to see if same length (switch always possible)
		if (beforeLen == afterLen) {
			
			// Ensure that neither block is switched past its Assignment's due date
			if (afterBlock.getEnd().after(befAsgn.getDueDate())) {
				return false;
			}
			
			// Switch the blocks
			final ITask t1 = beforeBlock.getTask();
			final ITask t2 = afterBlock.getTask();
			beforeBlock.setTask(t2);
			afterBlock.setTask(t1);
			
			// Update the blocks in the database
			try {
				StorageService.updateTimeBlock(beforeBlock);
				StorageService.updateTimeBlock(afterBlock);
				return true;
			} catch (final StorageServiceException sse) {
				sse.printStackTrace();
			}
			
			// ONLY return false here if the database updates/merges failed
			return false;
		} 
		else if (beforeLen > afterLen) {
			final int afterInd = allBlocks.indexOf(afterBlock);
			
			if (afterInd == allBlocks.size() - 1) {
				// For safety reasons, don't perform a switch -- in order to be the most careful
				// with this, I would have to request more of the schedule from the database so
				// that I can be sure I don't overlap something that I currently don't see in
				// my local segment of the calendar
				return false;
			}
			else {
				final ITimeBlockable postAfter = allBlocks.get(afterInd + 1);
				final ITimeBlockable preAfter = allBlocks.get(afterInd - 1);
				
				// --Try to push at front
				// Ensure that the switch can be made without interfering with due dates and without
				// pushing the "afterDest"-block
				if (!afterBlock.getEnd().after(befAsgn.getDueDate())
					&& postAfter.getStart().after(new Date(afterBlock.getStart().getTime() + beforeLen))) {
					
					final ITask t1 = beforeBlock.getTask();
					final ITask t2 = afterBlock.getTask();
					
					// Update the timeblocks
					beforeBlock.setEnd(new Date(beforeBlock.getStart().getTime() + afterLen));
					beforeBlock.setTask(t2);
					afterBlock.setEnd(new Date(afterBlock.getStart().getTime() + beforeLen));
					afterBlock.setTask(t1);
					
					// Update the blocks in the database
					try {
						StorageService.updateTimeBlock(beforeBlock);
						StorageService.updateTimeBlock(afterBlock);
						return true;
					} catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
					
					// Only return false if the update failed
					return false;
				}
				// --Then try to place before's end at after's end
				else if (!afterBlock.getEnd().after(befAsgn.getDueDate())
					&& !preAfter.getEnd().after(new Date(afterBlock.getEnd().getTime() - beforeLen))) {
					
					final ITask t1 = beforeBlock.getTask();
					final ITask t2 = afterBlock.getTask();
					
					// Update the time blocks
					beforeBlock.setEnd(new Date(beforeBlock.getStart().getTime() + afterLen));
					beforeBlock.setTask(t2);
					afterBlock.setStart(new Date(afterBlock.getStart().getTime() - beforeLen));
					afterBlock.setTask(t1);
					
					// Update the blocks in the database
					try {
						StorageService.updateTimeBlock(beforeBlock);
						StorageService.updateTimeBlock(afterBlock);
						return true;
					} catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
					
					// Only return false if the update failed
					return false;
				}
			}
		} 
		else { // beforeLen < afterLen
			final int beforeInd = allBlocks.indexOf(beforeBlock);
			
			if (beforeInd == 0) {
				// For safety reasons, don't perform a switch -- in order to be the most careful
				// with this, I would have to request more of the schedule from the database so
				// that I can be sure I don't overlap something that I currently don't see in
				// my local segment of the calendar
				return false;
			} 
			else {
				final ITimeBlockable postBefore = allBlocks.get(beforeInd + 1);
				final ITimeBlockable preBefore = allBlocks.get(beforeInd - 1);
				
				// --Try to push at front
				// Ensure that the switch can be made without interfering with due dates and without
				// pushing the "afterDest"-block
				if (!afterBlock.getEnd().after(befAsgn.getDueDate())
					&& postBefore.getStart().after(new Date(afterBlock.getStart().getTime() + beforeLen))) {
					
					final ITask t1 = beforeBlock.getTask();
					final ITask t2 = afterBlock.getTask();
					
					// Update the timeblocks
					beforeBlock.setEnd(new Date(beforeBlock.getStart().getTime() + afterLen));
					beforeBlock.setTask(t2);
					afterBlock.setEnd(new Date(afterBlock.getStart().getTime() + beforeLen));
					afterBlock.setTask(t1);
					
					// Update the blocks in the database
					try {
						StorageService.updateTimeBlock(beforeBlock);
						StorageService.updateTimeBlock(afterBlock);
						return true;
					} 
					catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
					
					// Only return false if the update failed
					return false;
				}
				// --Then try to place before's end at after's end
				else if (!afterBlock.getEnd().after(befAsgn.getDueDate())
					&& !preBefore.getEnd().after(new Date(afterBlock.getEnd().getTime() - beforeLen))) {
					
					final ITask t1 = beforeBlock.getTask();
					final ITask t2 = afterBlock.getTask();
					
					// Update the time blocks
					beforeBlock.setEnd(new Date(beforeBlock.getStart().getTime() + afterLen));
					beforeBlock.setTask(t2);
					afterBlock.setStart(new Date(afterBlock.getStart().getTime() - beforeLen));
					afterBlock.setTask(t1);
					
					// Update the blocks in the database
					try {
						StorageService.updateTimeBlock(beforeBlock);
						StorageService.updateTimeBlock(afterBlock);
						return true;
					} catch (final StorageServiceException sse) {
						sse.printStackTrace();
					}
					
					// Only return false if the update failed
					return false;
				}
			}	
		}
		
		// If this line is reached, all types of switches failed, so return false
		return false;
	}
	
	
	public static Date getLastDueDate() {
		List<IAssignment> list = StorageService.getAllAssignments();
		
		Date lastDue = new Date();
		
		for(int i = 0; i < list.size(); ++i) {
			IAssignment asgn = list.get(i);
			
			if(asgn.getDueDate().after(lastDue))
				lastDue = new Date(asgn.getDueDate().getTime());
		}
		
		return lastDue;
	}
	
}
