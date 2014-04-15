package data;

import java.util.UUID;


public class Task implements ITask {
	
	/* constants */
	
	// Default value to which _timeOfDay is initialized
	private static final TimeOfDay DEFAULT_TIME_OF_DAY = TimeOfDay.EVENING;
	// Default value of _suggestedBlockLength
	private static final double DEFAULT_BLOCK_LENGTH = 3;
	
	/* global vars */
	
	private final String _uniqueId;
	// AssignmentName:StepName format
	private String _name;	
	private double _percentOfTotal;
	private String _assignmentId;
	private double _percentComplete;
	// Optimal TimeOfDay to schedule Task
	private TimeOfDay _timeOfDay;
	// Optimal length of blocks to complete Task
	private double _suggestedBlockLength;

	/**
	 * Constructor without Assignment UID; _assignmentId is null
	 */
	public Task(String name, double percentTotal) {
		_name = name;
		_percentOfTotal = percentTotal;
		/* _assignmentId is not set until Task is added to assignment */	
		_assignmentId = null;
		// Set UID of this Task
		_uniqueId = generateId();
		/* Sets _percentComplete, _timeOfDay, _suggestedBlockLength*/
		setInitialValues();
	}
	/**
	 * Constructor with parent Assignment UID 
	 */
	public Task(String name, double percentTotal, String assignmentUID) {
		_name = name;
		_percentOfTotal = percentTotal;
		_assignmentId = assignmentUID;
		_uniqueId = generateId();
		setInitialValues();
	}
	
	/* Private methods */
	
	/**
	 * Method called in constructors to 
	 * initial values of vars not set
	 * in constructor.
	 * _percentComplete, _timeOfDay, _suggestedBlockLength
	 */
	private void setInitialValues() {
		_percentComplete = 0;
		_timeOfDay = DEFAULT_TIME_OF_DAY;
		_suggestedBlockLength = DEFAULT_BLOCK_LENGTH;
	}
	
	/**
	 * Generates unique identifier for this Task 
	 * using java.util.UUID
	 * 
	 * @return UID String
	 */
	private String generateId() {
		return UUID.randomUUID().toString();
	}
	
	/* ITask Mutators (Some comments in interface ITask) */
	
	@Override
	public void setAssignmentId(final String id) {
		_assignmentId = id;
	}	
	@Override
	public void setPercentComplete(final double percent) {
		_percentComplete = percent;	
	}
	@Override
	public void setPreferredTimeOfDay(final TimeOfDay tod) {
		_timeOfDay = tod;	
	}
	@Override
	public void setSuggestedBlockLength(final double length) {
		_suggestedBlockLength = length;
	}
	
	/* ITask Accessors (Comments in interface) */
	
	@Override
	public String getName() {
		return _name;
	}
	@Override
	public String getAssignmentID() {
		return _assignmentId;
	}
	@Override
	public String getTaskID() {
		return _uniqueId;
	}
	@Override
	public double getPercentOfTotal() {
		return _percentOfTotal;
	}
	@Override
	public double getPercentComplete() {
		return _percentComplete;
	}
	@Override
	public TimeOfDay getPreferredTimeOfDay() {
		return _timeOfDay;
	}
	@Override
	public double getSuggestedBlockLength() {
		return _suggestedBlockLength;
	}
}
