package data;

import java.util.Date;
import java.util.List;

/**
 * Represents the interface for an Assignment object
 * 
 * @author dgattey
 */
public interface IAssignment {
	
	/**
	 * Gives the name of the assignment
	 * 
	 * @return a string representation of the name
	 */
	public String getName();
	
	/**
	 * Gives the expected number of hours an assignment will take
	 * 
	 * @return the total number of hours an assignment may take
	 */
	public double getExpectedHours();
	
	/**
	 * Mutator for number of hours a user expects this Assignment to take.
	 * 
	 * @param h int approximate hours
	 */
	public void setExpectedHours(int h);
	
	/**
	 * Gives the tasks associated with this assignment
	 * 
	 * @return a list of task objects
	 */
	public List<ITask> getTasks();
	
	/**
	 * Gives the due date of the assignment
	 * 
	 * @return the Date object representing the due date
	 */
	public Date getDueDate();
	
	/**
	 * Gives the UID associated with this assignment
	 * 
	 * @return a unique String ID for this
	 */
	public String getID();
	
	/**
	 * Gives the Template object associated with this assignment
	 * 
	 * @return a template object
	 */
	public ITemplate getTemplate();
	
	/**
	 * Gives the full string
	 * 
	 * @return a string containing all possible information
	 */
	public String fullString();
	
}
