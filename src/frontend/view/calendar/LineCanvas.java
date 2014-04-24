package frontend.view.calendar;

import static frontend.view.calendar.CanvasConstants.DAYS;
import static frontend.view.calendar.CanvasConstants.HRS;
import static frontend.view.calendar.CanvasConstants.X_OFFSET;
import static frontend.view.calendar.CanvasConstants.Y_PAD;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.List;

import javax.swing.JPanel;

import data.ITimeBlockable;
import frontend.Utils;

/**
 * Represents a continuous line of calendar data
 * 
 * @author dgattey
 */
public class LineCanvas extends JPanel {
	
	private final CalendarView	cv;
	private int					y;
	private static final long	serialVersionUID	= 8788849553807412908L;
	
	/**
	 * Creates a canvas object
	 * 
	 * @param cv the calendar view object
	 */
	public LineCanvas(final CalendarView cv) {
		this.cv = cv;
		Utils.themeComponent(this);
	}
	
	/**
	 * Paints the current week on left
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		
		final Graphics2D brush = (Graphics2D) g;
		brush.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		brush.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// Week box
		brush.setColor(Utils.COLOR_ACCENT);
		brush.fill(new Rectangle2D.Double(0, 0, CanvasConstants.X_OFFSET, getHeight()));
		brush.setColor(Utils.COLOR_BACKGROUND);
		brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 13));
		brush.drawString("AT A", 8, 20);
		brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 11));
		brush.drawString("GLANCE", 8, 32);
		brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 15));
		brush.drawString("WEEK", 8, 56);
		brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 26));
		brush.drawString(String.valueOf(cv.getWeek()), 8, 80);
		
		// Do the vertical lines
		brush.setColor(Utils.COLOR_LIGHT_BG);
		for (int i = 1; i < DAYS; i++) {
			final double x = (i / DAYS) * (getWidth() - X_OFFSET - cv.getScrollWidth()) + X_OFFSET;
			brush.draw(new Line2D.Double(x, 0, x, getHeight()));
		}
		
		// Draws all lines for the assignment
		final List<ITimeBlockable> timeBlocks = cv.getTimeBlocks();
		y = (int) (Y_PAD / 2.0);
		final int height = (int) ((getHeight() - Y_PAD) / (timeBlocks.size() + 1));
		final int space = (int) ((getHeight() - Y_PAD) / timeBlocks.size());
		for (final ITimeBlockable t : timeBlocks) {
			placeAndDrawLine(brush, t, height);
			y += space;
		}
	}
	
	/**
	 * Simply calculates an x position from a day and hours/min
	 * 
	 * @param time an hour/min time
	 * @param day the day of the week
	 * @return a value to set for X
	 */
	private int getXPos(final double time, final int day) {
		double xStart = ((day / DAYS) * (getWidth() - X_OFFSET - cv.getScrollWidth())) + X_OFFSET;
		xStart += (time / HRS) * ((getWidth() - X_OFFSET) / DAYS);
		return (int) xStart;
	}
	
	/**
	 * Draws all lines from blocks in the calendar view
	 * 
	 * @param brush the graphics object
	 * @param t the block itself
	 * @param height the height of the block itself
	 */
	private void placeAndDrawLine(final Graphics2D brush, final ITimeBlockable t, final int height) {
		final Calendar c = CalendarView.getCalendarInstance();
		c.setTime(t.getStart());
		final double startTime = c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0);
		final int startDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		c.setTime(t.getEnd());
		final double endTime = c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0);
		final int endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		
		// Shared measurements
		final int x1 = getXPos(startTime, startDay);
		int x2 = getXPos(endTime, endDay);
		
		// If the end day is after the end of the given week, just make it max width
		if (endDay < startDay || (endDay == startDay && endTime < startTime)) {
			x2 = getWidth();
		}
		
		final int width = Math.max(x2 - x1, 5);
		final Rectangle2D.Double rect = new Rectangle2D.Double(x1, y, width, height);
		final Color currColor = CanvasConstants.getColor(t);
		
		// Draw block
		brush.setColor(currColor);
		brush.fill(rect);
		brush.setColor(Utils.COLOR_LIGHT_BG);
		brush.draw(rect);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(300, 100);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(300, 100);
	}
	
}
