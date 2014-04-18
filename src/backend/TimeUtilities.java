package backend;

import java.util.Date;
import java.util.List;

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
	
}
