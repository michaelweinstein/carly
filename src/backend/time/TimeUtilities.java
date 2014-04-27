package backend.time;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import data.IAssignment;
import data.ITask;
import data.ITimeBlockable;


public class TimeUtilities {
	
	
	public static void insertIntoSortedList(List<ITimeBlockable> allBlocks, ITimeBlockable block) {
		int ind;
		int size = allBlocks.size();

		if(allBlocks.size() == 0) {
			allBlocks.add(block);
			return;
		}
		
		//Compare to the first element and last element in the list
		if(block.getStart().compareTo(allBlocks.get(0).getStart()) <= 0) {
			allBlocks.add(0, block);
			return;
		}
		else if(block.getStart().compareTo(allBlocks.get(size - 1).getStart()) > 0) {
			allBlocks.add(allBlocks.size(), block);
			return;
		}

		//Compare to all surrounding pairs in the middle
		for(ind = 1; ind < size; ++ind) {
			if(block.getStart().compareTo(allBlocks.get(ind - 1).getStart()) > 0 &&
					block.getStart().compareTo(allBlocks.get(ind).getStart()) < 0) {
				allBlocks.add(ind, block);
				return;
			}
		}
	}


	//Returns the index in the list where the current time should appear
	//This function uses START dates in its comparisons	
	public static int indexOfFitLocn(List<ITimeBlockable> timeList, Date curr) {
		int ind;
		int size = timeList.size();

		if(timeList.size() == 0)
			return 0;

		//Compare to the first element and last element in the list
		if(curr.compareTo(timeList.get(0).getStart()) <= 0)
			return 0;
		else if(curr.compareTo(timeList.get(size - 1).getStart()) > 0)
			return size;

		//Compare to all surrounding pairs in the middle
		for(ind = 1; ind < size; ++ind) {
			//TODO: I just changed the second "compareTo" call to be "<=" instead of "<"... repercussions?
			if(curr.compareTo(timeList.get(ind - 1).getStart()) > 0 &&
					curr.compareTo(timeList.get(ind).getStart()) <= 0)
				return ind;
		}

		return ind;
	}
	
	
	public static boolean existsPossibleFit(List<ITimeBlockable> allBlocks, IAssignment asgn, Date start) {
		long amtFreeTime = 0;
		
		//If the list has no blocks in it, there is a fit as long as the range is big enough
		if(allBlocks.size() == 0)
			return TimeUnit.MILLISECONDS.convert(asgn.getExpectedHours(), TimeUnit.HOURS)
					<= asgn.getDueDate().getTime() - start.getTime();
		
		//Add time between start and first block
		if(allBlocks.size() > 0)
			amtFreeTime += allBlocks.get(0).getStart().getTime() - start.getTime();
		
		//Add time between blocks
		for(int i = 0; i < allBlocks.size() - 1; ++i) {
			ITimeBlockable b1 = allBlocks.get(i);
			ITimeBlockable b2 = allBlocks.get(i + 1);
			amtFreeTime += b2.getStart().getTime() - b1.getEnd().getTime();
		}
		
		//Add time between last block and end
		if(allBlocks.size() > 0) {
			amtFreeTime += (asgn.getDueDate().getTime()
					- allBlocks.get(allBlocks.size() - 1).getEnd().getTime());
		}
		
		double numFreeHours = TimeUnit.HOURS.convert(amtFreeTime, TimeUnit.MILLISECONDS);
		System.out.println("Trying to find possible fit: num free hours = " + numFreeHours);
		return (numFreeHours >= (double) asgn.getExpectedHours());
	}
	
	
	public static String printSchedule(List<ITimeBlockable> allBlocks) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < allBlocks.size(); ++i) {
			ITimeBlockable itb = allBlocks.get(i);
			builder.append("Start: ");
			builder.append(itb.getStart());
			builder.append(" || End: ");
			builder.append(itb.getEnd());
			builder.append("\n");
		}
		
		return builder.toString();
	}
	
	
	
	//Return false and do not modify the list if an insertion is not possible
	public static boolean switchTimeBlocks(List<ITimeBlockable> allBlocks, ITimeBlockable source, ITimeBlockable dest) {
		
		ITimeBlockable beforeBlock, afterBlock;
		//Determine which block is first, chronologically
		if(source.getStart().before(dest.getStart())) {
			beforeBlock = source;
			afterBlock = dest;
		}
		else {
			beforeBlock = dest;
			afterBlock = source;
		}
		
		//Get the lengths and the Assignment object corresponding to each block
		long beforeLen = beforeBlock.getLength();
		long afterLen = afterBlock.getLength();
		IAssignment befAsgn = StorageService.getAssignment(beforeBlock.getTask().getAssignmentID());
		IAssignment aftAsgn = StorageService.getAssignment(afterBlock.getTask().getAssignmentID());

		
		//Check to see if same length (switch always possible)
		if(beforeLen == afterLen) {
			
			//Ensure that neither block is switched past its Assignment's due date
			if(afterBlock.getEnd().after(befAsgn.getDueDate())) {
				return false;
			}
			
			
			//Switch the blocks
			ITask t1 = beforeBlock.getTask();
			ITask t2 = afterBlock.getTask();
			beforeBlock.setTask(t2);
			afterBlock.setTask(t1);
			
			//Update the blocks in the database
			try {
				StorageService.updateTimeBlock(beforeBlock);
				StorageService.updateTimeBlock(afterBlock);
				return true;
			}
			catch(StorageServiceException sse) {
				sse.printStackTrace();
			}
			
			//ONLY return false here if the database updates/merges failed
			return false;
		}
		else if(beforeLen > afterLen) {
			int afterInd = allBlocks.indexOf(dest); 
			
			//First try to place before's start at after's start
			if(afterInd == allBlocks.size() - 1) {
				//For safety reasons, don't perform a switch -- in order to be the most careful
				//with this, I would have to request more of the schedule from the database so
				//that I can be sure I don't overlap something that I currently don't see in
				//my local segment of the calendar
				return false;
			}
			else {
				ITimeBlockable postAfter = allBlocks.get(afterInd + 1);
				ITimeBlockable preAfter = allBlocks.get(afterInd - 1);

				//--Try to push at front	
				//Ensure that the switch can be made without interfering with due dates and without
				//pushing the "afterDest"-block
				if(!afterBlock.getEnd().after(befAsgn.getDueDate()) && 
						postAfter.getStart().after(new Date(afterBlock.getStart().getTime() + beforeLen))) {
					
					ITask t1 = beforeBlock.getTask();
					ITask t2 = afterBlock.getTask();
					
					//Update the timeblocks
					beforeBlock.setEnd(new Date(beforeBlock.getStart().getTime() + afterLen));
					beforeBlock.setTask(t2);
					afterBlock.setEnd(new Date(afterBlock.getStart().getTime() + beforeLen));
					afterBlock.setTask(t1);
					
					//Update the blocks in the database
					try {
						StorageService.updateTimeBlock(beforeBlock);
						StorageService.updateTimeBlock(afterBlock);
						return true;
					}
					catch(StorageServiceException sse) {
						sse.printStackTrace();
					}
					
					//Only return false if the update failed
					return false;
				}
				//--Then try to place before's end at after's end
				else if(!afterBlock.getEnd().after(befAsgn.getDueDate()) && 
						!preAfter.getEnd().after(new Date(afterBlock.getEnd().getTime() - beforeLen))) {
					
					ITask t1 = beforeBlock.getTask();
					ITask t2 = afterBlock.getTask();
					
					//Update the time blocks
					beforeBlock.setEnd(new Date(beforeBlock.getStart().getTime() + afterLen));
					beforeBlock.setTask(t2);
					afterBlock.setStart(new Date(afterBlock.getStart().getTime() - beforeLen));
					afterBlock.setTask(t1);
					
					//Update the blocks in the database
					try {
						StorageService.updateTimeBlock(beforeBlock);
						StorageService.updateTimeBlock(afterBlock);
						return true;
					}
					catch(StorageServiceException sse) {
						sse.printStackTrace();
					}
					
					//Only return false if the update failed
					return false;
				}
			}			
		}
		else { //beforeLen < afterLen
			//TODO: Do the same things as I do in the "else if" case above, except using 
			//		boundary-checking conditions on "beforeBlock" instead of "afterBlock"
		}
		
		//If this line is reached, all types of switches failed, so return false
		return false;
	}
}
