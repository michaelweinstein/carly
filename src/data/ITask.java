package data;

/**
 * Represents a possible task to complete within an assignment
 * 
 * @author dgattey
 */
public interface ITask {
	
	/**
	 * Gives the name of this task.
	 * Task names should be in the format
	 * AssignmentName:StepName
	 * 
	 * @return a String representing task name (AssignmentName:StepName)
	 */
	public String getName();
	
	/**
	 * Gives the UID of the parent assignment
	 * 
	 * @return the assignment ID
	 */
	public String getAssignmentID();
	
	/**
	 * Sets var to UID of parent assignment.
	 * Must have mutator, so var can be set 
	 * when ITask added to IAssignment.
	 * 
	 * @param UID of parent assignment
	 */
	public void setAssignmentId(String id);
	
	/**
	 * Gives the UID of this task
	 * 
	 * @return the task ID
	 */
	public String getTaskID();
	
	/**
	 * Gives the percent of total assignment this task represents (scale from 0 to 1 representing 0% to 1%)
	 * 
	 * @return a double representing the percent of total
	 */
	public double getPercentOfTotal();
	
	
	//TODO Should we store actual length instead of percent length, which is already in TemplateStep?
	/**
	 * Returns number of hours expected
	 * to finish this Task.
	 * 
	 * @return double for expected hours
	 */
//	public double getExpectedHours();
//	public void setExpectedHours();
	
	/**
	 * Gives the percent the user has completed (scale from 0 to 1 representing 0% to 1%) of this task
	 * 
	 * @return a double representing the percent completed
	 */
	public double getPercentComplete();
	
	/**
	 * Sets/updates how much of ITask has been
	 * completed by user thus far.
	 * 
	 * @param percent completed by user
	 */
	public void setPercentComplete(double percent);
	
	/**
	 * Gives the preferred time of day to work on this task
	 * 
	 * @return an enum representing the time preferred
	 */
	public TimeOfDay getPreferredTimeOfDay();
	
	/**
	 * Sets optimal TimeOfDay to schedule this Task.
	 * Mutator is necessary so that it can be updated
	 * by learning algorithms.
	 * 
	 * @param time of day user should work on this task
	 */
	public void setPreferredTimeOfDay(TimeOfDay tod);
	
	/**
	 * Gives the suggested length of time to work on this task
	 * 
	 * @return a double representing suggested length
	 */
	public double getSuggestedBlockLength();
	
	/**
	 * Sets the optimal length of each contiguous block
	 * of work scheduled to complete this task. Mutator
	 * is necessary so that var can be updated by
	 * learning algorithms.
	 * 
	 * @param length of optimal block of work for this Taks
	 */
	public void setSuggestedBlockLength(double length);
	
	
	/**
	 * Returns a detailed string representation of 
	 * the Task
	 * 
	 * @return a String representation of the Task
	 */
	public String fullString();
}
