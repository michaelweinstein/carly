package backend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import backend.database.StorageService;
import backend.database.Utilities;
import data.ITask;
import data.ITimeBlockable;
import data.TimeOfDay;

public class Learner {
	//How much to increment the ToD weight if the user directly adjusts the block in considerBlockUpdate
	private static final double DIRECT_TOD_INCREMENT = 3.0; 
	
	private static boolean _isEnabled = true;
	
	public static ArrayList<ITask> optimizeTasks(List<ITask> taskList) {
		return new ArrayList<ITask>(); 
	}
	
	public static void considerBlockUpdate(final ITimeBlockable newBlock, final Date oldStart, 
			final Date oldEnd) {
		final Date earlier = (oldStart.compareTo(oldEnd) < 0) ? oldStart : oldEnd;
		final Date later = (oldEnd.compareTo(oldStart) > 0) ? oldEnd : oldStart;
//		StorageService.learnTemplateConsecutiveHours(Learner.getHoursBetween(earlier, later));
		
		String todKey = Learner.extrapolateTimeOfDay(earlier); 
		if (todKey.equals("")) {
			System.err.println("Learner: considerBlockUpdate: Could not learn TimeOfDay");
		}
		else {
//			StorageService.learnTemplateStepTimeOfDay(oldBlock.getTask(), todKey, Learner.DIRECT_TOD_INCREMENT);
		}
	}
	
	public static void considerTaskUpdate(final ITask oldTask, final double newCompletion) {
		
	}
	
	private static double getHoursBetween(final Date earlier, final Date later) { 
		return TimeUnit.HOURS.convert(later.getTime() - earlier.getTime(), TimeUnit.MILLISECONDS); 
	}
	
	private static String extrapolateTimeOfDay(final Date earlier) {
		SimpleDateFormat format = new SimpleDateFormat ("k");
		
		System.out.println("Learner: extrapolateTimeOfDay: get hour format: " + format.format(earlier));
		
		double startTime; 
		try {
			startTime = Double.parseDouble(format.format(earlier));
		}
		catch (NumberFormatException x) {
			Utilities.printException("Learner: extrapolateTimeOfDay: could not parse Date's start time", x);
			return ""; 
		}
		
		//TimeOfDay enum uses 24-hour (military) time
		for (TimeOfDay tod : TimeOfDay.values()) {
			if (startTime >= tod.start && startTime <= tod.end) {
				return tod.name(); 
			}
			//need to wrap around at the twenty four hour mark
			else if (tod.start > tod.end) {
				if ((startTime >= tod.start && startTime <= 24.0) || 
					(startTime >= 0.0 && startTime <= tod.end)) {
					return tod.name();
				}
			}
		}
		
		return ""; 
	}
	
	/**
	 * Sets whether or not Learner is enabled.
	 * 
	 * @param enable true if Learner should run
	 */
	public static void setEnabled(boolean enable) {
		_isEnabled = enable;
	}
}
