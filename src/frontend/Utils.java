package frontend;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import data.Tuple;

/**
 * Constants class to package up strings used in multiple classes
 * 
 * @author dgattey
 */
public abstract class Utils {
	
	public static final String			APP_NAME			= "Carly";
	public static final String			DEBUG				= "debug";
	public static final String			USAGE				= "Usage: carly [--debug]";
	
	// GUI Constants
	public static final Color			COLOR_BACKGROUND	= Color.DARK_GRAY.darker();
	public static final Color			COLOR_FOREGROUND	= Color.WHITE;
	public static final Color			COLOR_ALTERNATE		= Color.DARK_GRAY;
	public static final Color			COLOR_LIGHT_BG		= Color.DARK_GRAY.brighter();
	public static final Color			COLOR_ACCENT		= Color.ORANGE;
	
	// Fonts
	private static Map<String, Font>	fonts;
	public static final String			FONT_NAME			= "Cabin";
	public static final String			FONT_NAME_ALT		= "Arial";
	
	/**
	 * Returns font of correct style and size
	 * 
	 * @param style the style (Font.BOLD, Font.PLAIN, Font.ITALIC or any combo thereof)
	 * @param size the size of the new font
	 * @return a Font object with the given properties
	 */
	public static Font getFont(final int style, final int size) {
		Font font = null;
		try {
			if (fonts == null) {
				fonts = new HashMap<>();
			}
			final String fontName = FONT_NAME.replace(" ", "") + style;
			
			// Load from a cache map, if exists
			if ((font = fonts.get(fontName + "|" + size)) != null) {
				return font;
			}
			final File fontFile = new File("fonts/" + fontName + ".ttf");
			
			// Make new font of right size
			font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(style, size);
			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(font);
			fonts.put(fontName + "|" + size, font);
		} catch (FontFormatException | IOException e) {
			font = new Font(FONT_NAME_ALT, style, size);
		}
		
		return font;
	}
	
	/**
	 * Adds a full border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderFull(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(2, 2, 2, 2, COLOR_ACCENT)));
	}
	
	/**
	 * Adds a left border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderLeft(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(0, 2, 0, 0, COLOR_ACCENT)));
	}
	
	/**
	 * Adds a right border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderRight(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(0, 0, 0, 2, COLOR_ACCENT)));
	}
	
	/**
	 * Adds a top border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderTop(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(2, 0, 0, 0, COLOR_ACCENT)));
	}
	
	/**
	 * Adds a bottom border to the panel
	 * 
	 * @param panel the panel that needs the border
	 */
	public static void addBorderBottom(final JComponent panel) {
		panel.setBorder(new CompoundBorder(panel.getBorder(), new MatteBorder(0, 0, 2, 0, COLOR_ACCENT)));
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
	public static void themeComponentAlt(final Component panel) {
		panel.setForeground(COLOR_FOREGROUND);
		panel.setBackground(COLOR_ALTERNATE);
	}
	
	/**
	 * Sets background and foreground color of this panel inverse of normal
	 * 
	 * @param panel the panel to apply it to
	 */
	public static void themeComponentLight(final Component panel) {
		panel.setForeground(COLOR_FOREGROUND);
		panel.setBackground(COLOR_LIGHT_BG);
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
	 * Returns a good contrasting color for c based on perceived brightness of a color
	 * 
	 * @param c a given color
	 * @return a new Color representing a contrasting color
	 */
	public static Color contrastingColor(final Color c) {
		final double r = c.getRed() * c.getRed() * .241;
		final double g = c.getGreen() * c.getGreen() * .691;
		final double b = c.getBlue() * c.getBlue() * .068;
		final double bright = Math.sqrt(r + g + b);
		return (bright < 130) ? Utils.COLOR_FOREGROUND : Utils.COLOR_BACKGROUND.darker().darker();
	}
	
	/**
	 * Gives a transparent color
	 * 
	 * @param c the color to use as base
	 * @param percent how transparent it should be (1 is opaque, 0 transparent)
	 * @return a new color with that range
	 */
	public static Color transparentColor(final Color c, final double percent) {
		if (percent < 0 || percent > 1) {
			throw new IllegalArgumentException("Color can't be that kind of transparent");
		}
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (percent * 255));
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
	 * Checks for dates overlapping
	 * 
	 * @param d1 a tuple of date ranges
	 * @param d2 another tuple of date ranges
	 * @return if d1 and d2 overlap
	 */
	public static boolean dateRangesOverlap(final Tuple<Date, Date> d1, final Tuple<Date, Date> d2) {
		final boolean d1StartInD2 = (d1.first.before(d2.second)) && (d1.first.after(d2.first));
		final boolean d1EndInD2 = (d1.second.before(d2.second)) && (d1.second.after(d2.first));
		final boolean d1EncompassesD2 = (d1.first.before(d2.first)) && (d1.second.after(d2.second));
		return d1StartInD2 || d1EndInD2 || d1EncompassesD2;
	}
	
	/**
	 * Prints an error to syserr
	 * 
	 * @param msg the details
	 */
	public static void printError(final String msg) {
		System.err.println("ERROR: " + msg);
	}
	
	/**
	 * Sets the font of the label to be bold of given size
	 * 
	 * @param label the label
	 * @param size the size
	 */
	public static void setFont(final JLabel label, final int size) {
		label.setFont(getFont(Font.BOLD, size));
	}
	
}
