package data;

/**
 * Represents a step in the template
 * 
 * @author dgattey
 */
public interface ITemplateStep {
	
	/**
	 * Gives how long a template step will take to complete
	 * 
	 * @return a double in hours
	 */
	public double getHourLength();
	
	/**
	 * Gives the best time of day to do this step
	 * 
	 * @return the best time of the day as an enum
	 */
	public TimeOfDay getBestTimeToWork();
	
}
