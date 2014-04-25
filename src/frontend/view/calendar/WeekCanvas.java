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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
	
	private static final long	serialVersionUID	= 1L;
	private final CalendarView	_cv;
	private Date				_weekStartDate;
	private Date				_weekEndDate;
	
	/**
	 * Constructor for a week canvas
	 * 
	 * @param cv the calendar view containing this
	 */
	public WeekCanvas(final CalendarView cv) {
		_cv = cv;
	}
	
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
		brush.setColor(Utils.COLOR_ALTERNATE);
		brush.fillRect(X_OFFSET, 0, getWidth() - X_OFFSET, (int) Y_PAD);
		brush.fillRect(X_OFFSET, (int) (getHeight() - Y_PAD), getWidth() - X_OFFSET, (int) Y_PAD);
		brush.setColor(Utils.COLOR_LIGHT_BG);
		brush.fillRect(0, 0, X_OFFSET, getHeight());
		
		// Do the vertical lines
		for (int i = 1; i < DAYS; i++) {
			final double x = (i / DAYS) * (getWidth() - X_OFFSET) + X_OFFSET;
			brush.draw(new Line2D.Double(x, Y_PAD, x, getHeight() - Y_PAD));
		}
		
		for (int i = 0; i <= HRS; i++) {
			final double y = (i / HRS) * (getHeight() - Y_PAD) + Y_PAD;
			
			// Horizontal lines
			brush.setColor(Utils.COLOR_LIGHT_BG);
			if (i != 0) {
				brush.draw(new Line2D.Double(X_OFFSET, y, getWidth(), y));
			}
			
			// Text
			brush.setColor(Utils.COLOR_FOREGROUND);
			brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 11));
			if (i == HRS) {
				// Why necessary to special case? I don't know but it doesn't draw right otherwise
				brush.drawString(getHourString(i), 5, (int) (getHeight() - Y_PAD));
			} else {
				brush.drawString(getHourString(i), 5, (int) y + 5);
			}
		}
		
		// Reloads week start and end date
		_weekStartDate = _cv.getCurrentWeekStartDate();
		_weekEndDate = _cv.getCurrentWeekEndDate();
		
		// Gets all time blocks and converts them to real blocks
		brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 12));
		for (final ITimeBlockable t : _cv.getTimeBlocks()) {
			placeAndDrawBlock(brush, t);
		}
		
	}
	
	/**
	 * Draws a block to canvas
	 * 
	 * @param brush the brush for the canvas
	 * @param t a time blockable to draw
	 */
	private void placeAndDrawBlock(final Graphics2D brush, final ITimeBlockable t) {
		// Checks bounds so we know not to place line if dates don't match up
		if (t.getStart().after(_weekEndDate) || t.getEnd().before(_weekStartDate)) {
			return;
		}
		
		// Shared measurements
		final double dayWidth = (getWidth() - X_OFFSET) / DAYS;
		final Calendar c = CalendarView.getCalendarInstance();
		c.setTime(t.getStart());
		final Date startDate = c.getTime();
		int startDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		c.setTime(t.getEnd());
		final Date endDate = c.getTime();
		int endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		
		// Sets correct start bounds
		int startX;
		int startY;
		if (startDate.before(_weekStartDate)) {
			startY = 0;
			startDay = c.getMinimum(Calendar.DAY_OF_WEEK) - 1;
			startX = getXPos(startDay);
		} else {
			c.setTime(t.getStart());
			startX = getXPos((int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS));
			startY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
		}
		
		// Sets correct end bounds
		int endX;
		int endY;
		if (endDate.after(_weekEndDate)) {
			endY = getHeight();
			endDay = c.getMaximum(Calendar.DAY_OF_WEEK) - 1;
			endX = getXPos(endDay);
		} else {
			c.setTime(t.getEnd());
			endX = getXPos((int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS));
			endY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
		}
		
		// Simple - start and end on same day!
		if (startX == endX) {
			final int height = endY - startY;
			drawBlock(brush, t, new Rectangle2D.Double(startX, startY, dayWidth, height));
			return;
		}
		
		// For events spanning at least one night
		drawBlock(brush, t, new Rectangle2D.Double(startX, startY, dayWidth, getHeight() - startY));
		for (int i = startDay + 1; i < endDay; i++) {
			// Draw full day
			drawBlock(brush, t, new Rectangle2D.Double(getXPos(i), 0, dayWidth, getHeight()));
		}
		drawBlock(brush, t, new Rectangle2D.Double(endX, 0, dayWidth, endY));
	}
	
	/**
	 * Draws an actual block
	 * 
	 * @param g the graphics object
	 * @param t the block itself
	 * @param rect where this block should be drawn
	 */
	private static void drawBlock(final Graphics2D g, final ITimeBlockable t, final Rectangle2D.Double rect) {
		final Color currColor = CanvasConstants.getColor(t);
		
		// Draw block background
		g.setColor(currColor);
		g.fill(rect);
		g.setColor(Utils.COLOR_LIGHT_BG);
		g.draw(rect);
		
		// Make title parts
		g.setColor(contrastingColor(currColor));
		final List<String> titleParts = new ArrayList<>(4);
		titleParts.add(t.getTask().getName());
		int i = 0;
		
		// Iterate while i is less than size
		while (i < titleParts.size() && i < 3) {
			String currPart = titleParts.get(i);
			while (g.getFontMetrics().getStringBounds(currPart, g).getWidth() >= rect.getWidth() - 10) {
				
				// Split the current title part into strings, putting the end on the next line
				final String part2 = currPart.substring(currPart.length() - 1);
				currPart = currPart.substring(0, currPart.length() - 1);
				titleParts.set(i, currPart);
				
				// Actually put it on the next line
				if (titleParts.size() - 1 == i) {
					titleParts.add(part2);
				} else {
					titleParts.set(i + 1, part2 + titleParts.get(i + 1));
				}
			}
			i++;
		}
		// For long titles
		if (i < titleParts.size()) {
			titleParts.set(i, "...");
		}
		
		// Finally, draw the title until it doesn't fit
		final int xPos = (int) rect.getX();
		final int space = 15;
		int yPos = 0;
		double nextY = 0;
		for (i = 0; i < titleParts.size(); i++) {
			yPos = (int) rect.getY() + (space * (i + 1));
			nextY = rect.getY() + (space * (i + 2));
			if (yPos >= rect.getMaxY()) {
				return;
			}
			g.drawString(nextY >= rect.getMaxY() ? "..." : titleParts.get(i), xPos + 5, yPos);
		}
		
	}
	
	/**
	 * Returns a good contrasting color for c based on perceived brightness of a color
	 * 
	 * @param c a given color
	 * @return a new Color representing a contrasting color
	 */
	private static Color contrastingColor(final Color c) {
		final double r = c.getRed() * c.getRed() * .241;
		final double g = c.getGreen() * c.getGreen() * .691;
		final double b = c.getBlue() * c.getBlue() * .068;
		final double bright = Math.sqrt(r + g + b);
		return (bright < 130) ? Utils.COLOR_FOREGROUND : Utils.COLOR_BACKGROUND.darker();
	}
	
	/**
	 * Simply calculates a y position from a number of hours (double for min inclusion)
	 * 
	 * @param time an hour/min time
	 * @return a value to set for Y
	 */
	private int getYPos(final double time) {
		return (int) ((time / HRS) * (getHeight() - Y_PAD) + Y_PAD);
	}
	
	/**
	 * Simply calculates an x position from a day
	 * 
	 * @param day an hour/min time
	 * @return a value to set for X
	 */
	private int getXPos(final double day) {
		return (int) ((day / DAYS) * (getWidth() - X_OFFSET) + X_OFFSET);
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
		return hour + ":00 " + (i < 12 || i == 24 ? "am" : "pm");
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100, 2000);
	}
}
