package backend;

import java.util.ArrayList;
import java.util.List;

import data.ITask;
import data.ITimeBlockable;

public class Learner {
	
	public ArrayList<ITask> optimizeTasks(List<ITask> taskList) {
		return new ArrayList<ITask>(); 
	}
	
	public void considerUserAdjustment(ITimeBlockable block, double newLength, String newTimeOfDay) {
		
	}
	
	public void considerCompletionSurvey(ITimeBlockable block, String timeOfDay, double completionDecimal) {
		
	}
}
