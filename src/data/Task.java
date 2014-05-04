package data;

import java.util.Objects;

/**
 * Task class for concrete assignments
 * 
 * @author dgattey
 */
public class Task implements ITask {
	
	private String			_uniqueId;
	private final String	_name;
	private double			_percentOfTotal;
	private String			_assignmentId;
	private double			_percentComplete;
	private TimeOfDay		_timeOfDay;
	private double			_suggestedBlockLength;
	private final int		_taskNumber;
	
	/**
	 * Constructor without Assignment UID, gets set later
	 * 
	 * @param name name of task
	 * @param percentTotal the percent of total assignment
	 * @param taskNumber the task number in order
	 */
	public Task(final String name, final double percentTotal, final int taskNumber) {
		_name = name;
		handlePercent(percentTotal);
		_assignmentId = null;
		_uniqueId = DataUtil.generateID() + _name.hashCode();
		setInitialValues();
		_taskNumber = taskNumber;
	}
	
	/**
	 * Constructor with parent Assignment UID
	 * 
	 * @param name name of task
	 * @param percentTotal the percent of total assignment
	 * @param taskNumber the task number in order
	 * @param assignmentUID the assignment ID
	 */
	public Task(final String name, final double percentTotal, final int taskNumber, final String assignmentUID) {
		_name = name;
		handlePercent(percentTotal);
		_assignmentId = assignmentUID;
		_uniqueId = assignmentUID + _name.hashCode();
		setInitialValues();
		_taskNumber = taskNumber;
	}
	
	/**
	 * Constructor used by StorageService to recreate the task object
	 * 
	 * @param id the id of the task itself
	 * @param name the name of task
	 * @param percentTotal the percent of total assignment
	 * @param asgnId the assignment ID
	 * @param percentComplete the percent complete (0 to 1)
	 * @param timeOfDay the time of day we like working on this
	 * @param suggestedBlockLength the suggested length of time to work on it
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
		// TODO Set with constructor parameter
		_taskNumber = 0;
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
	 * @param percent will always be > 0 and <= 1
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
	public int getTaskNumber() {
		return _taskNumber;
	}
	
	/* Holy trinity methods */
	
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
		return _assignmentId.equals(x._assignmentId) && _name.equals(x._name) && _uniqueId.equals(x._uniqueId);
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
