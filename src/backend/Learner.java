package backend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import data.ITask;
import data.ITimeBlockable;

public class Learner {
	
	public static ArrayList<ITask> optimizeTasks(List<ITask> taskList) {
		return new ArrayList<ITask>(); 
	}
	
	public static void considerBlockUpdate(final ITimeBlockable oldBlock, final Date newStart, final Date newEnd) {
		
	}
	
	public static void considerTaskUpdate(final ITask oldTask, final double newCompletion) {
		
	}
}
