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


		//TODO: Use the *TimeBlock*s' compareTo() function instead of directly
		//comparing *Date*s here

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


		//TODO: Use the *TimeBlock*s' compareTo() function instead of directly
		//comparing *Date*s here

		//Compare to the first element and last element in the list
		if(curr.compareTo(timeList.get(0).getStart()) <= 0)
			return 0;
		else if(curr.compareTo(timeList.get(size - 1).getStart()) > 0)
			return size;

		//Compare to all surrounding pairs in the middle
		for(ind = 1; ind < size; ++ind) {
			if(curr.compareTo(timeList.get(ind - 1).getStart()) > 0 &&
					curr.compareTo(timeList.get(ind).getStart()) < 0)
				return ind;
		}

		return ind;
	}
	
	
	public static boolean existsPossibleFit(List<ITimeBlockable> allBlocks, IAssignment asgn, Date start) {
		long amtFreeTime = 0;
		
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
			amtFreeTime += asgn.getDueDate().getTime()
					- allBlocks.get(allBlocks.size() - 1).getEnd().getTime();
		}
		
		double numFreeHours = TimeUnit.HOURS.convert(amtFreeTime, TimeUnit.MILLISECONDS);
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
		IAssignment srcAsgn = StorageService.getAssignment(source.getTask().getAssignmentID());
		IAssignment destAsgn = StorageService.getAssignment(dest.getTask().getAssignmentID());
		
		long srcLen = source.getLength();
		long destLen = dest.getLength();
		
		//Check to see if same length (switch always possible)
		if(srcLen == destLen) {
			
			//Ensure that neither block is switched past its Assignment's due date
			if(source.getStart().before(dest.getStart()) && 
					dest.getEnd().after(srcAsgn.getDueDate())) {
				return false;
			}
			else if(dest.getStart().before(source.getStart()) && 
					source.getEnd().after(destAsgn.getDueDate())) {
				return false;
			}
			
			//Switch the blocks
			ITask t1 = source.getTask();
			ITask t2 = dest.getTask();
			source.setTask(t2);
			dest.setTask(t1);
			
			//Update the blocks in the database
			try {
				StorageService.updateTimeBlock(source);
				StorageService.updateTimeBlock(dest);
				return true;
			}
			catch(StorageServiceException sse) {
				sse.printStackTrace();
			}
			
			//ONLY return false here if the database updates/merges failed
			return false;
		}
		else if(srcLen > destLen) {
			int destInd = allBlocks.indexOf(dest); 
			
			//First try to place source's start at dest's start
			if(destInd == allBlocks.size() - 1) {
				//TODO: For safety reasons, don't perform a switch -- in order to be the most careful
				//		with this, I would have to request more of the schedule from the database so
				//		that I can be sure I don't overlap something that I currently don't see in
				//		my local segment of the calendar
				return false;
			}
			else {
				ITimeBlockable afterDest = allBlocks.get(destInd + 1);
				
				//TODO: REORGANIZE THIS FUNCTION!
				//	Optimally, I wouldn't have to handle concerns with both chronology, and the swapping
				//	locations at the same time.
				
				//--Try to push at front	
				//Ensure that the switch can be made without interfering with due dates and without
				//pushing the "afterDest"-block
				if(source.getStart().before(dest.getStart())
						&& !dest.getEnd().after(srcAsgn.getDueDate())
						&& afterDest.getStart().after(new Date(dest.getStart().getTime() + source.getLength()))) {
					
					ITask t1 = source.getTask();
					ITask t2 = dest.getTask();
					
					//Update the timeblocks
					source.setEnd(new Date(source.getStart().getTime() + destLen));
					source.setTask(t2);
					dest.setEnd(new Date(dest.getStart().getTime() + srcLen));
					dest.setTask(t1);
					
					//Update the blocks in the database
					try {
						StorageService.updateTimeBlock(source);
						StorageService.updateTimeBlock(dest);
						return true;
					}
					catch(StorageServiceException sse) {
						sse.printStackTrace();
					}
					
					//Only return false if the update failed
					return false;
				}
				
				//--Then try to place source's end at dest's end

				
			}
			
						
		}
		else {
			
		}
		
		//TODO : check to see if blocks are of varying lengths - look at its immediate
		//		neighbors in list (maybe possible)
		
		
		//Return true if the insertion was successful
		return true;
	}
	
	
}
