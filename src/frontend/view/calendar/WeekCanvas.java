package frontend.view.calendar;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import data.ITimeBlockable;
import frontend.Utils;

/**
 * Draws a week to canvas representation
 * 
 * @author dgattey
 */
public class WeekCanvas extends JPanel {
	
	private static final long			serialVersionUID	= 1L;
	private static final double			VERT_NUM			= 5;
	private static final double			HORZ_NUM			= 24;
	private static final int			X_OFFSET			= 56;
	private static final double			Y_PAD				= 10;
	private final List<ITimeBlockable>	timeBlocks			= new ArrayList<>();
	
	/**
	 * Draws lines for view to use
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		
		final Graphics2D brush = (Graphics2D) g;
		brush.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		brush.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// Background of labels
		brush.setColor(Utils.COLOR_LIGHT_BG);
		brush.fillRect(0, 0, X_OFFSET, getHeight());
		
		// Do the vertical lines
		for (int i = 1; i < VERT_NUM; i++) {
			final double x = (i / VERT_NUM) * getWidth() + X_OFFSET;
			brush.draw(new Line2D.Double(x, 0, x, getHeight()));
		}
		
		for (int i = 0; i < HORZ_NUM; i++) {
			final double y = (i / HORZ_NUM) * (getHeight() - Y_PAD) + Y_PAD;
			
			// Horizontal lines
			brush.setColor(Utils.COLOR_LIGHT_BG);
			if (i != 0) {
				brush.draw(new Line2D.Double(X_OFFSET, y, getWidth(), y));
			}
			
			// Text
			brush.setColor(Utils.COLOR_FOREGROUND);
			brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 11));
			brush.drawString(getHourString(i), 5, (int) y + 5);
		}
		
		// Gets all time blocks and converts them to real blocks
		for (final ITimeBlockable t : timeBlocks) {
			drawBlock(t);
		}
		
	}
	
	/**
	 * Draws a block to canvas
	 * 
	 * @param t a time blockable to draw
	 */
	private void drawBlock(final ITimeBlockable t) {
		
	}
	
	/**
	 * Given an int, gives back an hour string for that
	 * 
	 * @param i an int (0 to 24) representing the hour
	 * @return the hour as a string
	 */
	private static String getHourString(final int i) {
		int hour = i % 12;
		if (hour == 0) {
			hour = 12;
		}
		return hour + ":00 " + (i < 12 ? "am" : "pm");
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100, 2000);
	}
}
