package frontend;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

/**
 * Constants class to package up strings used in multiple classes
 * 
 * @author dgattey
 */
public abstract class Utilities {
	
	public static final String	APP_NAME			= "Carly";
	public static final String	GUI					= "gui";
	public static final String	DEBUG				= "debug";
	public static final String	USAGE				= "Usage: carly [--gui] [--debug]";
	
	// GUI Constants
	public static final Color	COLOR_BACKGROUND	= Color.DARK_GRAY;
	public static final Color	COLOR_COMPONENT_FG	= Color.WHITE;
	public static final Color	COLOR_COMPONENT_BG	= Color.ORANGE;
	public static final String	APP_FONT_NAME		= "Arial";
	
	/**
	 * Sets background and foreground color of this panel
	 * 
	 * @param panel the panel to apply it to
	 */
	public static void themeComponent(final JComponent panel) {
		panel.setForeground(COLOR_COMPONENT_FG);
		panel.setBackground(COLOR_COMPONENT_BG);
	}
	
	/**
	 * Sets the padding of this panel to be x on either side and y on top and bottom
	 * 
	 * @param panel the panel
	 * @param x the horizontal padding
	 * @param y the vertical padding
	 */
	public static void padComponent(final JComponent panel, final int x, final int y) {
		panel.setBorder(new EmptyBorder(y, x, y, x));
	}
	
	/**
	 * Sets the padding of this panel to the given numbers
	 * 
	 * @param panel the panel
	 * @param top top padding
	 * @param left left padding
	 * @param bottom bottom padding
	 * @param right right padding
	 */
	public static void padComponent(final JComponent panel, final int top, final int left, final int bottom,
			final int right) {
		panel.setBorder(new EmptyBorder(top, left, bottom, right));
	}
	
	/**
	 * Calculates minimum of many ints
	 * 
	 * @param ints a list of ints to find min of
	 * @return the smallest of a variable number of ints
	 */
	public static int min(final int... ints) {
		int curr = Integer.MAX_VALUE;
		for (final int i : ints) {
			curr = Math.min(i, curr);
		}
		return curr;
	}
	
	/**
	 * Calculates maximum of many ints
	 * 
	 * @param ints a list of ints to find min of
	 * @return the smallest of a variable number of ints
	 */
	public static int max(final int... ints) {
		int curr = Integer.MIN_VALUE;
		for (final int i : ints) {
			curr = Math.max(i, curr);
		}
		return curr;
	}
	
	/**
	 * Prints an error to syserr
	 * 
	 * @param msg the details
	 */
	public static void printError(final String msg) {
		System.err.println("ERROR: " + msg);
	}
}
