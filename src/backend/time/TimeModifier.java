package backend.time;

import java.util.Date;
import java.util.List;

import backend.database.StorageService;
import data.ITask;
import data.ITimeBlockable;


public class TimeModifier {
	
	
	public static void updateBlock(List<ITimeBlockable> allBlocks, ITimeBlockable block, 
			Date newStart, Date newEnd) {
		
		Date currStart = block.getStart();
		Date currEnd = block.getEnd();
		
		//Shortening a block from the top
		if(currStart.compareTo(newStart) < 0 && currEnd.compareTo(newEnd) == 0) {
			block.setStart(newStart);
			StorageService.updateTimeBlock(block);
			//TODO: re-optimize schedule to take advantage of this new empty space?
		}
		//Shortening a block from the bottom
		else if(currStart.compareTo(newStart) == 0 && currEnd.compareTo(newEnd) > 0) {
			block.setEnd(newEnd);
			StorageService.updateTimeBlock(block);
			//TODO: re-optimize schedule to take advantage of this new empty space?
		}
		//Lengthening a block from the top
		else if(currStart.compareTo(newStart) > 0 && currEnd.compareTo(newEnd) == 0) {
			
			int ind = TimeUtilities.indexOfFitLocn(allBlocks, newStart);
			
			ITimeBlockable prev = (ind > 0 ? allBlocks.get(ind - 1) : null);
			ITimeBlockable curr = allBlocks.get(ind);
			ITimeBlockable next = (ind < allBlocks.size() - 1 ? allBlocks.get(ind + 1) : null);
			
			//In this case, check to see if newStart overlaps prev's end
			if(prev.getEnd().getTime() > newStart.getTime()) {
				//TODO: push "prev" back if possible, or push "next" forward
				//		(i.e. make the necessary accommodations)
			}
			else {
				block.setStart(newStart);
				StorageService.updateTimeBlock(block);
			}
			
			
		}
		//Lengthening a block from the bottom
		else if(currStart.compareTo(newStart) == 0 && currEnd.compareTo(newEnd) < 0) {
			
		}
		//Otherwise the block has been dragged
		else {
			int ind = TimeUtilities.indexOfFitLocn(allBlocks, newStart);
			
			ITimeBlockable prev = (ind > 0 ? allBlocks.get(ind - 1) : null);
			ITimeBlockable curr = allBlocks.get(ind);
			ITimeBlockable next = (ind < allBlocks.size() - 1 ? allBlocks.get(ind + 1) : null);
			
			
			//TODO: make sure it is not within the bounds of both the prev and next
			if(prev.getEnd().getTime() > newStart.getTime()) {
				//TODO: push "prev" back if possible
			}
			else if(next.getStart().getTime() < newEnd.getTime()) {
				//TODO: push "next" forward if possible
			}
			else {
				block.setStart(newStart);
				block.setEnd(newEnd);
				StorageService.updateTimeBlock(block);
			}
			
		}
	}
	
	public static void deleteBlock(ITimeBlockable block) {
		
		StorageService.removeTimeBlock(block);
		
		//TODO: Re-optimize calendar post-deletion??
	}
	
	
	//This function is called when a user pulls on a slider to convey the message that
	//they are changing how much progress they have made on completing a particular Task.
	public static void updateBlocksInTask(ITask task, double newPct) {
		
		Date now = new Date(); //this Date captures where the user is and how much work they've done

		//1. Get all Dates that are part of the current Task, "task"
		
		//2. Using the time in "now", figure out how much the user should have completed
		//	 (in units of percentage) of that Task.
		
		//3. Examine the actual percent complete (the parameter "newPct")
		
		//4. Determine whether this is an addition or subtraction operation based on the sign
		//	 of the difference between step (2) and step (3).
		
		//5a. If user is ahead - take a bit of time away from each block, then done
		//5b. If user is behind - try to add a bit to each block -- follow the same patterns
		//		as I use above for re-optimizing the schedule when a block is lengthened
		//		*** (perhaps even call the updateBlock(.) function from here) ***
		
		
	}
	
}
