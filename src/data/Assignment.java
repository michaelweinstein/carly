package data;

	
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class Assignment implements IAssignment {
	// Constant for initialized value of _expectedHours
	private static int DEFAULT_EXPECTED_HOURS = 3;
	
	private final String _uniqueId;
	private String _name;
	private Date _deadline;
	private ITemplate _template;
	private int _expectedHours;
	private List<ITask> _tasks;
	
	/** 
	 * An Assignment must at minimum have a name, due date and template
	 * Constructor without expectedHours, set to DEFAULT_EXPECTED_HOURS constant
	 */
	public Assignment(String name, Date dueDate, ITemplate template) {
		_name = name;
		_deadline = dueDate;
		_template = template;
		_expectedHours = DEFAULT_EXPECTED_HOURS;
		_uniqueId = generateId();

		_tasks = createTasksFromTemplate(template);
	}
	/**
	 * Constructor with expectedHours
	 */
	public Assignment(String name, Date dueDate, ITemplate template, int expectedHours) {
		_name = name;
		_deadline = dueDate;
		_template = template;
		_expectedHours = expectedHours;
		_uniqueId = generateId();
		
		_tasks = createTasksFromTemplate(template);
	}
	
	/* Private methods */
	
	/**
	 * Creates a new Task in _tasks for 
	 * each TemplateStep in _template. 
	 * For each Task: sets percentOfTotal
	 * (and suggestedBlockLength ?)
	 * 
	 * @param ITemplate stored in _template
	 */
	private List<ITask> createTasksFromTemplate(final ITemplate template) {
		if (template != null) {
			List<ITemplateStep> steps = template.getAllSteps();
			// Create List to store new Tasks with same length of template steps list
			List<ITask> tasks = new ArrayList<ITask>(steps.size());
			// For each TemplateStep, create new Task
			for (ITemplateStep step: steps) {
				// Task name in the form of Assignment:Step
				String taskName = _name + ":" + step.getName();
				
				// TODO Should we store actual amount of time in Task?
				double approxLength = step.getPercentOfTotal()*_expectedHours;
				
				// Create new Task with info from TemplateStep
				ITask task = new Task(taskName, step.getPercentOfTotal(), _uniqueId);
				// Set preferred time of day in Task according to this TemplateStep
				task.setPreferredTimeOfDay(step.getBestTimeToWork());
				task.setSuggestedBlockLength(template.getPreferredConsecutiveHours());
				// Add task 
				tasks.add(task);
			}
			return tasks;
		}
		// If Template is null, return empty list of tasks
		else return new ArrayList<ITask>();
	}
	
	
	/**
	 * Creates and stores unique identifier
	 * for this object using java.util.UUID.
	 * It should be called in the constructor.
	 */
	private String generateId() {
		return UUID.randomUUID().toString();
	}
	
	/* Editing _tasks */
	
	/**
	 * Adds Task to list of all Tasks in
	 * Assignment. Sets Task's reference
	 * to Assignment id to this _uniqueId
	 * 
	 * @param task to add
	 */
	public void addTask(ITask task) {
		task.setAssignmentId(_uniqueId);
		_tasks.add(task);
	}
	
	/**
	 * Removes the specified Task from the
	 * list of all Tasks in assignment.
	 * Returns true if successfully removed
	 * 
	 * @param task to remove
	 * @return true if removal successful
	 */
	public boolean removeTask(ITask task) {
		return _tasks.remove(task);
	}
	
	/**
	 * Overrides current _tasks List and sets
	 * the specified List as _tasks. Stores all
	 * Tasks in parameter list in _tasks, and
	 * saves no Tasks from old _tasks list.
	 * Sets parent UID of each Task added
	 * to this _uniqueId.
	 * 
	 * @param tasks List to be stored
	 */
	public void setAllTasks(List<ITask> tasks) {
		// Set Task parent UID in O(n)
		for (ITask t: tasks) 
			t.setAssignmentId(_uniqueId);
		// Override entire _tasks var
		_tasks = tasks;
	}
	
	/* Mutators */
	
	/**
	 * Mutator for number of hours a user
	 * expects this Assignment to take.
	 * 
	 * @param h, int approximate hours
	 */
	@Override
	public void setExpectedHours(int h) {
		_expectedHours = h;
	}
	
	/* IAssignment Accessors (Comments in interface IAssignment) */

	@Override
	public String getName() {
		return _name;
	}
	@Override
	public int getExpectedHours() {
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
}
