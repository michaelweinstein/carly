package data;

/**
 * Represents a possible task to complete within an assignment
 * 
 * @author dgattey
 */
public interface ITask {
	
	/**
	 * Gives the name of this task
	 * 
	 * @return a String representing the name
	 */
	public String getName();
	
	/**
	 * Gives the UID of the parent assignment
	 * 
	 * @return the assignment ID
	 */
	public String getAssignmentID();
	
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
	
	/**
	 * Gives the percent the user has completed (scale from 0 to 1 representing 0% to 1%) of this task
	 * 
	 * @return a double representing the percent completed
	 */
	public double getPercentComplete();
	
	/**
	 * Gives the preferred time of day to work on this task
	 * 
	 * @return an enum representing the time preferred
	 */
	public TimeOfDay getPreferredTimeOfDay();
	
	/**
	 * Gives the suggested length of time to work on this task
	 * 
	 * @return a double representing suggested length
	 */
	public double getSuggestedBlockLength();
	
}
