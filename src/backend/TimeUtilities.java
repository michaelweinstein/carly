package backend;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import data.IAssignment;
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
	
	
	public static boolean existsPossibleFit(List<ITimeBlockable> allBlocks, IAssignment asgn) {
		long amtFreeTime = 0;
		
		for(int i = 0; i < allBlocks.size() - 1; ++i) {
			ITimeBlockable b1 = allBlocks.get(i);
			ITimeBlockable b2 = allBlocks.get(i + 1);
			amtFreeTime += b2.getStart().getTime() - b1.getEnd().getTime();
		}
		
		double numFreeHours = TimeUnit.HOURS.convert(amtFreeTime, TimeUnit.MILLISECONDS);
		return (numFreeHours <= (double) asgn.getExpectedHours());
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
	public static boolean switchTimeBlocks(List<ITimeBlockable> allBlocks, ITimeBlockable b1, ITimeBlockable b2) {
		
		//TODO : check to see if same length (switch always possible)
		//TODO : check to see if one longer than other - look at its immediate neighbors in list (maybe possible)
		
		//Return true if the insertion was successful
		return true;
	}
	
	
}
