package data;

import java.util.UUID;

/**
 * Utilities methods for data structures
 * like Assignment, Task, Template, TemplateStep.
 * Also stores constants for default initialized
 * values not specified by user.
 * 
 * @author miweinst
 *
 */

public class DataUtil {
	
	/* constants */
	
	// Task: Default value for preferred Time of Day in Task
	public static final TimeOfDay 	DEFAULT_TIME_OF_DAY = TimeOfDay.EVENING;
	// Template, Task: Default value for Task block length, Template consecutive hours
	public static final double 		DEFAULT_CONSECUTIVE_HOURS = 3;
	
	// Assignment: Default value of expected hours to complete in Assignment
	public static final int 		DEFAULT_ASSIGNMENT_EXPECTED_HOURS = 3;
	
	// TemplateStep: Val for how many days this step takes
	public static final int 		DEFAULT_STEP_DAYS = 1;
	// TemplateStep: Val for how many hours a day step is worked on
	public static final double 		DEFAULT_HOURS_PER_DAY = 3;
	
	
	/* util methods */
	
	/**
	 * Checks representation of percentOfTotal
	 * representation. 
	 * Must be between 0 and 1 as decimal ratio.
	 * 
	 * @param percent of total
	 * @return true if rep ok
	 */
	public static boolean percentRepOK(double per) {
		return (per <= 1 && per > 0);
	}
	
	/**
	 * Creates and stores unique identifier
	 * for caller using java.util.UUID. It is
	 * often called in data constructors.
	 * Generates unique ID every time it's called.
	 * 
	 * @return UUID as String
	 */
	public static String generateID() {
		return UUID.randomUUID().toString();
	}
	
}
