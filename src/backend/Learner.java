package backend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import backend.database.AssignmentTaskStorage;
import backend.database.StorageService;
import backend.database.Utilities;
import data.IAssignment;
import data.ITask;
import data.ITemplate;
import data.ITemplateStep;
import data.ITimeBlockable;
import data.TimeOfDay;

public class Learner {
	//How much to increment the ToD weight if the user directly adjusts the block in considerBlockUpdate
	private static final double DIRECT_TOD_INCREMENT = 3.0;
	private static final double INDIRECT_TOD_FACTOR = 1.0; 
	private static boolean _isEnabled = true;
	
	public static void optimizeTasks(IAssignment assignment) {
		//Don't do any optimizing if the Learner is disabled
		if (_isEnabled == false) {
			return; 
		}
		AssignmentTaskStorage.sortTasks(assignment);
		ITemplate template = StorageService.getTemplate(assignment.getTemplate().getID()); 
		
		HashMap<Integer, ITemplateStep> stepNumberToStep = new HashMap<>(); 
		for (ITemplateStep step : template.getAllSteps()) {
			stepNumberToStep.put(step.getStepNumber(), step); 
		}
		System.out.println("assignment: " + assignment);
		System.out.println("template: " + template);
		System.out.println("stepNumberToStep: " + stepNumberToStep);
		
		for (ITask task : assignment.getTasks()) {
			ITemplateStep step = stepNumberToStep.get(task.getTaskNumber()); 
			task.setSuggestedBlockLength(template.getPreferredConsecutiveHours());
			task.setPreferredTimeOfDay(step.getBestTimeToWork());
		}
	}
	
	public static void considerBlockUpdate(final ITimeBlockable newBlock, final Date oldStart, 
			final Date oldEnd) {
		//Don't do any learning if the Learner is disabled
		if (_isEnabled == false) {
			return; 
		}
		
		System.out.println("Learner: considerBlockUpdate");
		final Date start = newBlock.getStart(); 
		final Date end = newBlock.getEnd();
		final Date earlier = (oldStart.compareTo(oldEnd) < 0) ? oldStart : oldEnd;
		final Date later = (oldEnd.compareTo(oldStart) > 0) ? oldEnd : oldStart;
		
		System.out.println("\tstart: " + start);
		System.out.println("\tend: " + end);
		System.out.println("\tearlier: " + earlier);
		System.out.println("\tlater: " + later);
		
		//if the block length isn't exactly the same length
		if (Learner.getHoursBetween(start, end) != Learner.getHoursBetween(earlier, later)) {
			StorageService.learnTemplateConsecutiveHours(newBlock.getTask(), Learner.getHoursBetween(earlier, later));
		}
		StorageService.learnTemplateStepTimeOfDay(newBlock.getTask(), Learner.extrapolateTimeOfDay(earlier), 
				Learner.DIRECT_TOD_INCREMENT);
	}
	
	public static void considerTaskUpdate(final ITask oldTask, Date adjustmentTime, double magnitudeChange) {
		//Don't do any learning if the Learner is disabled
		if (_isEnabled == false) {
			return; 
		}
		System.out.println("Learner: considerTaskUpdate");
		StorageService.learnTemplateStepTimeOfDay(oldTask, Learner.extrapolateTimeOfDay(adjustmentTime), 
				magnitudeChange * Learner.INDIRECT_TOD_FACTOR);
	}
	
	/**
	 * Sets whether or not Learner is enabled.
	 * 
	 * @param enable true if Learner should run
	 */
	public static void setEnabled(boolean enable) {
		_isEnabled = enable;
	}
	
	/*
	 * Helper methods
	 */
	
	private static double getHoursBetween(final Date earlier, final Date later) { 
		//Decided against using TimeUnit to convert because it rounds results down
		//We need to find the exact length even if the hours between is less than 1
		return (double)(later.getTime() - earlier.getTime()) / (1000 * 60 * 60); 
	}
	
	private static String extrapolateTimeOfDay(final Date earlier) {
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(earlier);
		int hourOfDay = cal.get(Calendar.HOUR_OF_DAY); 
		double startTime = (double) hourOfDay + ((double) cal.get(Calendar.MINUTE)) / 60.0; 
		
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
}
