package data;

import java.util.List;

/**
 * Represents a template object for use in the backend
 * 
 * @author dgattey
 */
public interface ITemplate {
	
	/**
	 * Returns UID of this ITemplate
	 * 
	 * @return UID as String
	 */
	public String getID();
	
	/**
	 * Returns name of Template, visible to user
	 * 
	 * @return name as String
	 */
	public String getName();
	
	/**
	 * Returns optimal contiguous hours user
	 * should work on steps in a template
	 * 
	 * @return preferred consecutive hours as double
	 */
	public double getPreferredConsecutiveHours();
	
	/**
	 * Adds a new step to the template. Names
	 * of Steps in template must be unique
	 * to be successfully added. 
	 * 
	 * @param stepToAdd the new step
	 * @param stepBefore the step that was before
	 * @return true if step successfully added
	 */
	public boolean addStep(ITemplateStep stepToAdd, ITemplateStep stepBefore);
	
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
