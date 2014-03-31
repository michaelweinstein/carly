package frontend;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

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
	public static final Color	COLOR_FOREGROUND	= Color.WHITE;
	public static final Color	COLOR_BORDER		= Color.ORANGE;
	public static final String	APP_FONT_NAME		= "Arial";
	
	/**
	 * Adds a full border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderFull(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(2, 2, 2, 2, COLOR_BORDER)));
	}
	
	/**
	 * Adds a left border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderLeft(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(0, 2, 0, 0, COLOR_BORDER)));
	}
	
	/**
	 * Adds a right border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderRight(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(0, 0, 0, 2, COLOR_BORDER)));
	}
	
	/**
	 * Adds a top border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderTop(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(2, 0, 0, 0, COLOR_BORDER)));
	}
	
	/**
	 * Adds a bottom border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderBottom(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(0, 0, 2, 0, COLOR_BORDER)));
	}
	
	/**
	 * Sets background and foreground color of this panel
	 * 
	 * @param panel the panel to apply it to
	 */
	public static void themeComponent(final Component panel) {
		panel.setForeground(COLOR_FOREGROUND);
		panel.setBackground(COLOR_BACKGROUND);
	}
	
	/**
	 * Sets background and foreground color of this panel inverse of normal
	 * 
	 * @param panel the panel to apply it to
	 */
	public static void themeComponentInverse(final Component panel) {
		panel.setForeground(Color.WHITE);
		panel.setBackground(new Color(140, 140, 140));
	}
	
	/**
	 * Sets the font of a label to be bold of the given size
	 * 
	 * @param label the label
	 * @param fontSize the new font size to use
	 */
	public static void setFont(final JLabel label, final int fontSize) {
		label.setFont(new Font(APP_FONT_NAME, Font.BOLD, fontSize));
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
	 * Sets the padding of this panel to be x on either side and y on top and bottom
	 * 
	 * @param panel the panel
	 * @param x the horizontal padding
	 * @param y the vertical padding
	 */
	public static void padComponentWithBorder(final JComponent panel, final int x, final int y) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new EmptyBorder(y, x, y, x)));
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
	 * Sets the padding of this panel to the given numbers
	 * 
	 * @param panel the panel
	 * @param top top padding
	 * @param left left padding
	 * @param bottom bottom padding
	 * @param right right padding
	 */
	public static void padComponentWithBorder(final JComponent panel, final int top, final int left, final int bottom,
			final int right) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new EmptyBorder(top, left, bottom, right)));
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
