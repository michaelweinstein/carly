package data;

/**
 * Represents a step in the template
 * 
 * @author dgattey
 */
public interface ITemplateStep {
	
	/**
	 * Gets name of this TemplateStep.
	 * Constraint: Steps within a Template
	 * MUST have unique names
	 * 
	 * @return unique name as String
	 */
	public String getName();
	
	/**
	 * Returns the index of this TemplateStep
	 * referring to the order steps are completed.
	 * 
	 * @return int index
	 */
	public int getStepNumber();
	
	/**
	 * Sets the order in which this 
	 * TemplateStep should be executed.
	 * 
	 * @param index of step execution
	 */
	public void setStepNumber(int index);
	
	/**
	 * Gives how long a template step will take to complete
	 * relative to the entire ITemplate
	 * 
	 * @return percent of Template
	 */
	public double getPercentOfTotal();
	
	/**
	 * Gives the best time of day to do this step
	 * 
	 * @return the best time of the day as an enum
	 */
	public TimeOfDay getBestTimeToWork();
	
	/**
	 * Number of days this step lasts
	 * 
	 * @return number of days
	 */
	public int getNumberOfDays();
	
	/**
	 * Gives the hours that the step of the template
	 * should be worked on per day
	 * 
	 * @return number of hours per day
	 */
	public double getHoursPerDay();
	
	/**
	 * Full String representation of this class
	 * 
	 * @return String representing all aspects of this class 
	 */
	public String fullString(); 
}
