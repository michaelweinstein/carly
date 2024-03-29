package data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import frontend.Utils;

public class Template implements ITemplate {
	
	private final String				_uid;
	private String						_name;
	// Using ArrayList so we can add elements at specific index
	private final List<ITemplateStep>	_steps;
	private final double				_preferredConsecutiveHours;
	
	/**
	 * Constructor with template name. _preferredConsecutiveHours set to DataUtil.DEFAULT, _steps initialized to empty
	 * TreeSet
	 */
	public Template(final String name) {
		_name = name;
		_uid = DataUtil.generateID() + _name.hashCode();
		_preferredConsecutiveHours = DataUtil.DEFAULT_CONSECUTIVE_HOURS;
		_steps = new ArrayList<>();
	}
	
	/**
	 * Constructor with preferred consecutive hours
	 */
	public Template(final String name, final double hours) {
		_name = name;
		_preferredConsecutiveHours = hours;
		_uid = DataUtil.generateID() + _name.hashCode();
		_steps = new ArrayList<>();
	}
	
	/**
	 * Constructor takes in a list of Steps already formed.
	 */
	public Template(final String name, final List<ITemplateStep> steps) {
		_name = name;
		_uid = DataUtil.generateID() + _name.hashCode();
		_preferredConsecutiveHours = DataUtil.DEFAULT_CONSECUTIVE_HOURS;
		_steps = new ArrayList<>();
		addAllSteps(steps);
	}
	
	/**
	 * Constructor with both name and preferred consecutive hours, and also an initial List of TemplateSteps to populate
	 * _steps.
	 */
	public Template(final String name, final double hours, final List<ITemplateStep> steps) {
		_name = name;
		_preferredConsecutiveHours = hours;
		_steps = new ArrayList<>();
		addAllSteps(steps);
		_uid = DataUtil.generateID() + +_name.hashCode();
	}
	
	/**
	 * Constructor used by StorageService to reconstruct the template object
	 */
	public Template(final String id, final String name, final List<ITemplateStep> steps, final double consecutiveHours) {
		_uid = id;
		_name = name;
		_steps = new ArrayList<>();
		addAllSteps(steps);
		_preferredConsecutiveHours = consecutiveHours;
	}
	
	/* Private methods */
	
	/**
	 * Adds steps individually (instead of setting _steps = steps) because addStep() checks that names are unique.
	 * 
	 * @param steps, list of steps to add
	 */
	private void addAllSteps(final List<ITemplateStep> steps) {
		// Add steps individually to ensure the names are unique
		for (final ITemplateStep s : steps) {
			addStep(s);
		}
	}
	
	/* ITemplate step manipulation */
	
	/**
	 * Add step after specified stepBefore in List. Gets index of stepBefore, then adds specified stepToAdd to _steps
	 * List at index+1. All elements inclusive following index+1 are shifted right. Has to check getStepByName because
	 * Step names MUST be unique within a Template. Returns false if step with that name already exists. If stepBefore
	 * is null, adds step to end of _steps Runtime: O(n) [because of getStepByName]
	 */
	@Override
	public boolean addStep(final ITemplateStep stepToAdd, final ITemplateStep stepBefore) {
		// Make sure a Step of that name does not already exist
		final ITemplateStep mustBeNull = getStepByName(stepToAdd.getName());
		if (mustBeNull == null) {
			// Add after stepBefore
			if (stepBefore != null) {
				final int indexBefore = _steps.indexOf(stepBefore);
				_steps.add(indexBefore + 1, stepToAdd);
			}
			// If stepBefore is null, add to end of list
			else {
				_steps.add(stepToAdd);
			}
			return true;
		}
		Utils.printError("A TemplateStep of that name already exists in Template " + _name + "  (Template.addStep)");
		return false;
	}
	
	/**
	 * Adds Step to the end of List of Steps; does not specify index or stepBefore.
	 * 
	 * @param stepToAdd to end of list
	 * @return true if successfully added
	 */
	@Override
	public boolean addStep(final ITemplateStep stepToAdd) {
		return addStep(stepToAdd, null);
	}
	
	/**
	 * Removes specified stepToRemove from _steps List. If removal successful, returns the element. If _steps does not
	 * contain stepToRemove, returns null and prints message.
	 */
	@Override
	public ITemplateStep removeStep(final ITemplateStep stepToRemove) {
		if (_steps.contains(stepToRemove)) {
			final int index = _steps.indexOf(stepToRemove);
			return _steps.remove(index);
		}
		Utils.printError("_steps does not contain" + stepToRemove + " : returning null (Template.removeStep)");
		return null;
	}
	
	@Override
	public List<ITemplateStep> getAllSteps() {
		return _steps;
	}
	
	/**
	 * Returns the ITemplateStep stored in List _steps that has the name of specified String stepName. If no match
	 * found, returns null. Runtime: worst case linear time, O(n)
	 */
	@Override
	public ITemplateStep getStepByName(final String stepName) {
		for (final ITemplateStep step : _steps) {
			if (step.getName().equals(stepName)) {
				return step;
			}
		}
		// Executes if no match found
		return null;
	}
	
	/**
	 * Clears all TemplateSteps from _steps list, and returns old _steps. Call this and then addAllSteps to simulate a
	 * replaceSteps method.
	 * 
	 * @return old List of TemplateSteps
	 */
	@Override
	public List<ITemplateStep> clearSteps() {
		final List<ITemplateStep> oldSteps = _steps;
		_steps.clear();
		return oldSteps;
	}
	
	/* ITemplate accessors (Commented in interface) */
	
	@Override
	public String getID() {
		return _uid;
	}
	
	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public double getPreferredConsecutiveHours() {
		return _preferredConsecutiveHours;
	}
	
	/* Holy Trinity (+ fullString()) */
	
	@Override
	public String toString() {
		return _name;
	}
	
	@Override
	public String fullString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("[uid: ");
		builder.append(_uid);
		builder.append(", name: ");
		builder.append(_name);
		builder.append(", preferredConsecutiveHours: ");
		builder.append(_preferredConsecutiveHours);
		builder.append(", steps: {");
		
		for (final ITemplateStep step : _steps) {
			builder.append(step.fullString() + ", ");
		}
		builder.replace(builder.length() - 2, builder.length() - 1, "}]");
		return builder.toString().trim();
	}
	
	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Template)) {
			return false;
		}
		try {
			final Template comp = (Template) other;
			return (comp.fullString().contains(fullString())) ? true : false;
		} catch (final ClassCastException x) {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fullString());
	}
	
	@Override
	public void setTitle(final String titleTemplate) {
		_name = titleTemplate;
	}
}
