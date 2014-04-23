package backend;

import java.util.Date;
import java.util.List;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import data.ITimeBlockable;


public class TimeModifier {
	
	
	public static void updateBlock(List<ITimeBlockable> allBlocks, ITimeBlockable block, 
			Date newStart, Date newEnd) {
		
		Date currStart = block.getStart();
		Date currEnd = block.getEnd();
		
		//Shortening a block from the top
		if(currStart.compareTo(newStart) < 0 && currEnd.compareTo(newEnd) == 0) {
			block.setStart(newStart);
			try {
				StorageService.updateTimeBlock(block);
			} catch (StorageServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//TODO: re-optimize schedule to take advantage of this new empty space?
		}
		//Shortening a block from the bottom
		else if(currStart.compareTo(newStart) == 0 && currEnd.compareTo(newEnd) > 0) {
			block.setEnd(newEnd);
			try {
				StorageService.updateTimeBlock(block);
			} catch (StorageServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				//TODO: push prev back if possible, or push next forward
				//		(i.e. make the necessary accommodations)
			}
			else {
				block.setStart(newStart);
				try {
					StorageService.updateTimeBlock(block);
				} catch (StorageServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
		//Lengthening a block from the bottom
		else if(currStart.compareTo(newStart) == 0 && currEnd.compareTo(newEnd) < 0) {
			
		}
		//Otherwise the block has been dragged
		else {
			//TODO: make sure it is not within the bounds of both the prev and next
		}
	}
	
	public static void deleteBlock(ITimeBlockable block) {
		StorageService.removeTimeBlock(block);
		
		//TODO: Re-optimize calendar post-deletion??
	}
	
}
