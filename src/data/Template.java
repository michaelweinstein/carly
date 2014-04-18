package data;

import java.util.ArrayList;
import java.util.List;

import frontend.Utils;

public class Template implements ITemplate {
	
	private final String				_uid;
	private final String				_name;
	// Using ArrayList so we can add elements at specific index
	private final List<ITemplateStep>	_steps;
	private final double				_preferredConsecutiveHours;
	
	/**
	 * Constructor with template name. _preferredConsecutiveHours set to DataUtil.DEFAULT, _steps initialized to empty
	 * TreeSet
	 */
	public Template(final String name) {
		_name = name;
		_uid = DataUtil.generateID();
		_preferredConsecutiveHours = DataUtil.DEFAULT_CONSECUTIVE_HOURS;
		_steps = new ArrayList<>();
	}
	
	/**
	 * Constructor with preferred consecutive hours
	 */
	public Template(final String name, final double hours) {
		_name = name;
		_preferredConsecutiveHours = hours;
		_uid = DataUtil.generateID();
		_steps = new ArrayList<>();
	}
	
	/**
	 * Constructor takes in a list of Steps already formed.
	 */
	public Template(final String name, final List<ITemplateStep> steps) {
		_name = name;
		_uid = DataUtil.generateID();
		_preferredConsecutiveHours = DataUtil.DEFAULT_CONSECUTIVE_HOURS;
		_steps = steps;
	}
	
	/**
	 * Constructor used by StorageService to reconstruct the template object
	 */
	public Template(String id, String name, List<ITemplateStep> steps, double consecutiveHours) {
		_uid = id; 
		_name = name; 
		_steps = steps; 
		_preferredConsecutiveHours = consecutiveHours; 
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
		Utils.printError("A TemplateStep of that name already " + "exists in Template " + _name
			+ "  (Template.addStep)");
		return false;
	}
	
	/**
	 * Adds Step to the end of List of Steps; does not specify index or stepBefore.
	 * 
	 * @param stepToAdd to end of list
	 * @return true if successfully added
	 */
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
		Utils.printError("_steps does not contain " + // print line
			"stepToRemove; returning null (Template.removeStep)");
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
	
	@Override
	public String toString() {
		return _name;
	}
}
