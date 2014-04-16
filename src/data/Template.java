package data;

import java.util.ArrayList;
import java.util.List;


public class Template implements ITemplate {
	
	private String _uid;
	private String _name;
	// Using ArrayList so we can add elements at specific index
	private ArrayList<ITemplateStep> _steps;
	private double _preferredConsecutiveHours;
	
	/**
	 * Constructor with template name. _preferredConsecutiveHours
	 * set to DataUtil.DEFAULT, _steps initialized to empty TreeSet
	 */
	public Template(String name) {
		_name = name;
		_uid = DataUtil.generateID();
		_preferredConsecutiveHours = DataUtil.DEFAULT_CONSECUTIVE_HOURS;
		_steps = new ArrayList<ITemplateStep>();
	}
	
	/* ITemplate step manipulation */

	/**
	 * Add steps in list after specified stepBefore. Gets index
	 * of stepBefore, then adds specified stepToAdd to _steps List 
	 * at index+1. All elements inclusive following index+1 are shifted right.
	 */
	@Override
	public void addStep(ITemplateStep stepToAdd, ITemplateStep stepBefore) {
		int indexBefore = _steps.indexOf(stepBefore);
		_steps.add(indexBefore+1, stepToAdd);
	}

	/**
	 * Removes specified stepToRemove from _steps List.
	 * If removal successful, returns the element.
	 * If _steps does not contain stepToRemove, returns null
	 * and prints message.
	 */
	@Override
	public ITemplateStep removeStep(ITemplateStep stepToRemove) {
		if (_steps.contains(stepToRemove)) {
			int index = _steps.indexOf(stepToRemove);
			return _steps.remove(index);
		}
		else {
			System.out.println("ERROR: _steps does not contain " + 			//print line
					"stepToRemove; returning null (Template.removeStep)");
			return null;
		}
	}

	@Override
	public List<ITemplateStep> getAllSteps() {
		return _steps;
	}

	/**
	 * Returns the ITemplateStep stored in List _steps
	 * that has the name of specified String stepName.
	 * If no match found, returns null.
	 * Runtime: worst case linear time, O(n)
	 */
	@Override
	public ITemplateStep getStepByName(String stepName) {
		for (ITemplateStep step: _steps) 
			if (step.getName().equals(stepName)) 
				return step;
		// Executes if no match found
		return null;
	}

	/* ITemplate accessors (Commented in interface)*/
	
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
}
