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
	public static final TimeOfDay DEFAULT_TIME_OF_DAY = TimeOfDay.EVENING;
	// Template, Task: Default value for Task block length, Template consecutive hours
	public static final double DEFAULT_CONSECUTIVE_HOURS = 3;
	// Assignment: Default value of expected hours to complete in Assignment
	public static int DEFAULT_ASSIGNMENT_EXPECTED_HOURS = 3;
	
	
	/* util methods */
	
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
