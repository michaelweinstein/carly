package data;

import java.util.Objects;

public class Task implements ITask {
	
	/* global vars */
	
	private String			_uniqueId;
	// AssignmentName:StepName format
	private final String	_name;
	private double			_percentOfTotal;
	private String			_assignmentId;
	private double			_percentComplete;
	// Optimal TimeOfDay to schedule Task
	private TimeOfDay		_timeOfDay;
	// Optimal length of blocks to complete Task
	private double			_suggestedBlockLength;
	
	/**
	 * Constructor without Assignment UID; _assignmentId is null
	 */
	public Task(final String name, final double percentTotal) {
		_name = name;
		handlePercent(percentTotal);
		/* _assignmentId is not set until Task is added to assignment */
		_assignmentId = null;
		// Set UID of this Task
		_uniqueId = DataUtil.generateID() + _name.hashCode();
		/* Sets _percentComplete, _timeOfDay, _suggestedBlockLength */
		setInitialValues();
	}
	
	/**
	 * Constructor with parent Assignment UID
	 */
	public Task(final String name, final double percentTotal, final String assignmentUID) {
		_name = name;
		handlePercent(percentTotal);
		_assignmentId = assignmentUID;
		_uniqueId = assignmentUID + _name.hashCode();
		setInitialValues();
	}
	
	/**
	 * Constructor used by StorageService to recreate the task object
	 */
	public Task(final String id, final String name, final double percentTotal, final String asgnId,
			final double percentComplete, final TimeOfDay timeOfDay, final double suggestedBlockLength) {
		_uniqueId = id;
		_name = name;
		_percentOfTotal = percentTotal;
		_assignmentId = asgnId;
		_percentComplete = percentComplete;
		_timeOfDay = timeOfDay;
		_suggestedBlockLength = suggestedBlockLength;
	}
	
	/* Private methods */
	
	/**
	 * Method called in constructors to initial values of vars not set in constructor. Percent complete obviously set to
	 * 0%, and other vars set to DataUtil default value constants. _percentComplete, _timeOfDay, _suggestedBlockLength
	 */
	private void setInitialValues() {
		_percentComplete = 0;
		_timeOfDay = DataUtil.DEFAULT_TIME_OF_DAY;
		_suggestedBlockLength = DataUtil.DEFAULT_CONSECUTIVE_HOURS;
	}
	
	/**
	 * Sets _percentOfTotal to percent passed into constructor, IF the representation is valid. Else _percentOfTotal = 0
	 * and message is printed.
	 * 
	 * @param 0 < percent <= 1
	 */
	private void handlePercent(final double percent) {
		if (DataUtil.percentRepOK(percent)) {
			_percentOfTotal = percent;
		} else {
			System.out.println("ERROR: Percent representation invalid " + percent + ". "
				+ "Expecting format: 0 < percent <= 1  (Task.handlePercent)");
		}
	}
	
	/* ITask Mutators (Some comments in interface ITask) */
	
	@Override
	public void setAssignmentId(final String id) {
		_assignmentId = id;
		
		// Also updates the block id
		_uniqueId = id + _name.hashCode();
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
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Task)) {
			return false;
		}
		final Task x = (Task) obj;
		return _assignmentId.equals(x._assignmentId) && _name.equals(x._name) && _percentComplete == x._percentComplete
			&& _percentOfTotal == x._percentOfTotal && _suggestedBlockLength == x._suggestedBlockLength
			&& _timeOfDay.equals(x._timeOfDay) && _uniqueId.equals(x._uniqueId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(_name, _timeOfDay, _suggestedBlockLength, _assignmentId);
	}
	
	@Override
	public String fullString() {
		return String.format("[Task: id: %s; name: %s; percentOfTotal: %s; assignmentId: %s; "
			+ "percentComplete: %s; timeOfDay: %s; suggestedBlockLength: %s]", _uniqueId, _name, _percentOfTotal,
				_assignmentId, _percentComplete, _timeOfDay.name(), _suggestedBlockLength);
	}
}
