package frontend.view;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import data.ITask;
import data.ITimeBlockable;
import frontend.Utils;

/**
 * Just some constants and static methods for both canvases
 * 
 * @author dgattey
 */
public class CanvasUtils {
	
	public static final double			DAYS		= 7;
	public static final double			HRS			= 24;
	public static final int				X_OFFSET	= 35;
	public static final double			Y_PAD		= 10;
	public static Map<Integer, Color>	colors;
	
	/**
	 * Makes a random color from the hash code of the task corresponding to a time block
	 * 
	 * @param t the time blockable to use
	 * @return a new Color for that item
	 */
	public static Color getColor(final ITimeBlockable t) {
		if (t.isMovable()) {
			return getColor(t.getTask());
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
		final int max = 10000;
		if (colors == null) {
			colors = new HashMap<>();
			for (int i = 0; i <= max; i++) {
				final Random r = new Random();
				final int x = r.nextInt(180) + 20;
				final int y = r.nextInt(180) + 20;
				final int z = r.nextInt(180) + 20;
				final Color c = new Color(x, y, z, 215);
				colors.put(i, c);
			}
		}
		final Random r = new Random((t.getTaskID() + t.getAssignmentID()).hashCode());
		return colors.get(r.nextInt(max));
	}
	
	/**
	 * Returns if the mouse is currently at the top point of the block t
	 * 
	 * @param e a mouse event
	 * @param t the rect in question
	 * @param min the min value
	 * @return true if the point is at the top edge
	 */
	public static boolean atTopEdge(final MouseEvent e, final Rectangle2D.Double t, final double min) {
		return t != null && t.getMinY() + 10 >= e.getY() && xWithinRect(e, t) && e.getY() > min + 10;
	}
	
	/**
	 * Returns if the mouse is currently at the bottom point of the block t
	 * 
	 * @param e a mouse event
	 * @param t the rect in question
	 * @param max the max value
	 * @return true if the point is at the bottom edge
	 */
	public static boolean atBottomEdge(final MouseEvent e, final Rectangle2D.Double t, final double max) {
		return t != null && t.getMaxY() - 10 <= e.getY() && xWithinRect(e, t) && e.getY() < max - 10;
	}
	
	/**
	 * Checks a point for inclusion within a rect
	 * 
	 * @param e a mouse event
	 * @param t the rect in question
	 * @return true if the point is within the x bounds of the rect
	 */
	public static boolean xWithinRect(final MouseEvent e, final Rectangle2D.Double t) {
		return e.getX() >= t.getMinX() && e.getX() <= t.getMaxX();
	}
}
