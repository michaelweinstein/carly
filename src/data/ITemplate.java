package data;

import java.util.List;

/**
 * Represents a template object for use in the backend
 * 
 * @author dgattey
 */
public interface ITemplate {
	
	/**
	 * Adds a new step to the template
	 * 
	 * @param stepToAdd the new step
	 * @param stepBefore the step that was before
	 */
	public void addStep(ITemplateStep stepToAdd, ITemplateStep stepBefore);
	
	/**
	 * Removes a step from the template
	 * 
	 * @param stepToRemove a step to take out
	 * @return the step that was removed
	 */
	public ITemplateStep removeStep(ITemplateStep stepToRemove);
	
	/**
	 * Gives a list of all steps represented in the template
	 * 
	 * @return a List of all steps
	 */
	public List<ITemplateStep> getAllSteps();
	
	/**
	 * Searches for a step by name
	 * 
	 * @param stepName the possible name of the step
	 * @return a template step if found or null if not
	 */
	public ITemplateStep getStepByName(String stepName);
	
}
