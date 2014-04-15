package data;

import java.util.Date;

/**
 * Represents a block of time that can be blocked into an assignment or unavailable
 * 
 * @author dgattey
 */
public interface ITimeBlockable extends Comparable<ITimeBlockable> {
	
	/**
	 * The start date of the block
	 * 
	 * @param d the date representing the start
	 */
	public void setStart(Date d);
	
	/**
	 * The end date of the block
	 * 
	 * @param d the date representing the end
	 */
	public void setEnd(Date d);
	
	public Date getStart();
	public Date getEnd();
	
	
	/**
	 * Set the task of this time block
	 * 
	 * @param t the task represented
	 */
	public void setTask(ITask t);
	
	/**
	 * Get the task of this time block
	 * 
	 * @return the task represented
	 */
	public ITask getTask();
	
	/**
	 * Gives the task ID
	 * 
	 * @return a String representing the task ID
	 */
	public String getTaskId();
	
	/**
	 * Gives moveable status
	 * 
	 * @return if the block is moveable
	 */
	public boolean isMovable();
	
}
