package data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Assignment implements IAssignment {
	
	private final String	_uniqueId;
	private final String	_name;
	private final Date		_deadline;
	private ITemplate		_template;
	private double			_expectedHours;
	private List<ITask>		_tasks;
	
	/**
	 * An Assignment must at minimum have a name, due date and template Constructor without expectedHours, set to
	 * DEFAULT_EXPECTED_HOURS constant
	 */
	public Assignment(final String name, final Date dueDate, final ITemplate template) {
		_name = name;
		_deadline = dueDate;
		_template = template;
		_expectedHours = DataUtil.DEFAULT_ASSIGNMENT_EXPECTED_HOURS;
		_uniqueId = DataUtil.generateID() + _name.hashCode();
		
		_tasks = createTasksFromTemplate(template);
	}
	
	/**
	 * Gives it an ID in addition to the normal constructor
	 */
	public Assignment(final String id, final String name, final Date dueDate, final ITemplate template) {
		_name = name;
		_deadline = dueDate;
		_template = template;
		_expectedHours = DataUtil.DEFAULT_ASSIGNMENT_EXPECTED_HOURS;
		_uniqueId = id;
		
		_tasks = createTasksFromTemplate(template);
	}
	
	/**
	 * Constructor with expectedHours
	 */
	public Assignment(final String name, final Date dueDate, final ITemplate template, final double exHours) {
		_name = name;
		_deadline = dueDate;
		_template = template;
		_expectedHours = exHours;
		_uniqueId = DataUtil.generateID() + _name.hashCode();
		
		_tasks = createTasksFromTemplate(template);
	}
	
	/**
	 * Constructor with expectedHours and id
	 */
	public Assignment(final String id, final String name, final Date dueDate, final ITemplate template,
			final double exHours) {
		_name = name;
		_deadline = dueDate;
		_template = template;
		_expectedHours = exHours;
		_uniqueId = id;
		
		_tasks = createTasksFromTemplate(template);
	}
	
	/**
	 * Constructor used by StorageService to rebuild an Assignment Template and list of tasks are set later.
	 */
	public Assignment(final String id, final String name, final Date dueDate, final double d, final List<ITask> taskList) {
		_uniqueId = id;
		_name = name;
		_deadline = dueDate;
		_expectedHours = d;
		_tasks = taskList;
		_template = null;
	}
	
	/* Private methods */
	
	/**
	 * Creates a new Task in _tasks for each TemplateStep in _template. For each Task: sets percentOfTotal (and
	 * suggestedBlockLength)
	 * 
	 * @param template stored in _template
	 * @return a list of tasks created
	 */
	private List<ITask> createTasksFromTemplate(final ITemplate template) {
		if (template != null) {
			final List<ITemplateStep> steps = template.getAllSteps();
			final List<ITask> tasks = new ArrayList<>(steps.size());
			
			// Loop through all steps and create tasks from them
			for (int i = 0; i < steps.size(); i++) {
				final ITemplateStep step = steps.get(i);
				final String taskName = step.getName();
				
				// Create a task from the given information
				final ITask task = new Task(taskName, step.getPercentOfTotal(), i, _uniqueId);
				task.setPreferredTimeOfDay(step.getBestTimeToWork());
				task.setSuggestedBlockLength(template.getPreferredConsecutiveHours());
				tasks.add(task);
			}
			return tasks;
		}
		return new ArrayList<>();
	}
	
	/* Editing _tasks */
	
	/**
	 * Adds Task to list of all Tasks in Assignment. Sets Task's reference to Assignment id to this _uniqueId
	 * 
	 * @param task to add
	 */
	public void addTask(final ITask task) {
		task.setAssignmentId(_uniqueId);
		_tasks.add(task);
	}
	
	/**
	 * Removes the specified Task from the list of all Tasks in assignment. Returns true if successfully removed
	 * 
	 * @param task to remove
	 * @return true if removal successful
	 */
	public boolean removeTask(final ITask task) {
		return _tasks.remove(task);
	}
	
	/**
	 * Overrides current _tasks List and sets the specified List as _tasks. Stores all Tasks in parameter list in
	 * _tasks, and saves no Tasks from old _tasks list. Sets parent UID of each Task added to this _uniqueId.
	 * 
	 * @param tasks List to be stored
	 */
	public void setAllTasks(final List<ITask> tasks) {
		// Set Task parent UID in O(n)
		for (final ITask t : tasks) {
			t.setAssignmentId(_uniqueId);
		}
		// Override entire _tasks var
		_tasks = tasks;
	}
	
	/* Mutators */
	
	/**
	 * Mutator for number of hours a user expects this Assignment to take.
	 * 
	 * @param h, int approximate hours
	 */
	@Override
	public void setExpectedHours(final int h) {
		_expectedHours = h;
	}
	
	/**
	 * Used by Storage Service to reconstruct the Assignment
	 * 
	 * @param template
	 */
	public void setTemplate(final ITemplate template) {
		_template = template;
	}
	
	/* IAssignment Accessors (Comments in interface IAssignment) */
	
	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public double getExpectedHours() {
		return _expectedHours;
	}
	
	@Override
	public List<ITask> getTasks() {
		return _tasks;
	}
	
	@Override
	public Date getDueDate() {
		return _deadline;
	}
	
	@Override
	public String getID() {
		return _uniqueId;
	}
	
	@Override
	public ITemplate getTemplate() {
		return _template;
	}
	
	@Override
	public double getPercentComplete() {
		double complete = 0;
		if (_tasks == null) {
			return 0;
		}
		for (final ITask t : _tasks) {
			complete += t.getPercentComplete() * t.getPercentOfTotal();
		}
		return complete;
	}
	
	/* Holy trinity */
	
	/**
	 * Returns true if the Assignment's UIDs are equal, or instances are identical in memory. Returns false if Object is
	 * not an instance of Assignment at all.
	 * 
	 * @param o to check if equal to this
	 * @return true if UIDs are equal
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Assignment) {
			final Assignment a = (Assignment) o;
			return getID().equals(a.getID());
		}
		return false;
	}
	
	/**
	 * Returns the hash code representation of this Assignment's UID. Turns string back into original UUID and gets
	 * hash.
	 * 
	 * @return int hash code of UUID from _uniqueID string
	 */
	@Override
	public int hashCode() {
		final UUID uid = UUID.fromString(getID());
		return uid.hashCode();
	}
	
	/**
	 * Returns the name of this Assignment and it's unique identifier.
	 * 
	 * @return AssignmentName, UID
	 */
	@Override
	public String toString() {
		return new String(getName() + ", " + getID());
	}
	
	@Override
	public String fullString() {
		final StringBuilder taskBuilder = new StringBuilder();
		for (final ITask t : getTasks()) {
			taskBuilder.append(t.fullString());
			taskBuilder.append(", ");
		}
		taskBuilder.delete(taskBuilder.length() - 2, taskBuilder.length());
		
		return String.format("Assignment\n\tName: %s\n\tID: %s\n\tDue: %s\n\tTasks: %s\n\tTemplate: %s", getName(),
				getID(), getDueDate(), taskBuilder.toString(), getTemplate().fullString());
	}
}
