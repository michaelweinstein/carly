package frontend.view.calendar;

import java.awt.Color;
import java.util.Random;

import data.ITimeBlockable;
import frontend.Utils;

/**
 * Just some constants and static methods for both canvases
 * 
 * @author dgattey
 */
public class CanvasConstants {
	
	static final double	DAYS		= 7;
	static final double	HRS			= 24;
	static final int	X_OFFSET	= 35;
	static final double	Y_PAD		= 10;
	
	/**
	 * Makes a random color from the hash code of a time block
	 * 
	 * @param t the time blockable to use
	 * @return a new Color for that item
	 */
	public static Color getColor(final ITimeBlockable t) {
		if (t.isMovable()) {
			final Random r = new Random(t.hashCode());
			return new Color(r.nextInt(160), r.nextInt(160), r.nextInt(160), 190);
		}
		return Utils.COLOR_ALTERNATE;
	}
}
