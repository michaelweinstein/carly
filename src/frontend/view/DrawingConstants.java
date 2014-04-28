package frontend.view;

import java.awt.Color;
import java.util.Random;

import data.ITask;
import data.ITimeBlockable;
import frontend.Utils;

/**
 * Just some constants and static methods for both canvases
 * 
 * @author dgattey
 */
public class DrawingConstants {
	
	public static final double	DAYS		= 7;
	public static final double	HRS			= 24;
	public static final int		X_OFFSET	= 35;
	public static final double	Y_PAD		= 10;
	
	/**
	 * Makes a random color from the hash code of the task corresponding to a time block
	 * 
	 * @param t the time blockable to use
	 * @return a new Color for that item
	 */
	public static Color getColor(final ITimeBlockable t) {
		if (t.isMovable()) {
			final Random r = new Random(t.getTaskId().hashCode() + t.getTask().getAssignmentID().hashCode());
			return new Color(r.nextInt(150), r.nextInt(150), r.nextInt(150), 210);
		}
		return Utils.COLOR_ALTERNATE;
	}
	
	/**
	 * Makes a random color from the hash code of a task
	 * 
	 * @param t a task to use
	 * @return a new Color for that item
	 */
	public static Color getColor(final ITask t) {
		final Random r = new Random(t.getTaskID().hashCode() + t.getAssignmentID().hashCode());
		return new Color(r.nextInt(150), r.nextInt(150), r.nextInt(150), 210);
	}
}
