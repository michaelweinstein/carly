package frontend.view.calendar;

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
import java.util.Random;

import javax.swing.JPanel;

import data.AssignmentBlock;
import data.ITimeBlockable;
import data.Task;
import frontend.Utils;

/**
 * Draws a week to canvas representation
 * 
 * @author dgattey
 */
public class WeekCanvas extends JPanel {
	
	private static final long			serialVersionUID	= 1L;
	private static final double			DAYS				= 7;
	private static final double			HRS					= 24;
	private static final int			X_OFFSET			= 56;
	private static final double			Y_PAD				= 10;
	private final List<ITimeBlockable>	timeBlocks			= new ArrayList<>();
	
	/**
	 * TEST for TimeBlocks
	 */
	public WeekCanvas() {
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		c.set(Calendar.HOUR_OF_DAY, 3);
		c.set(Calendar.MINUTE, 30);
		Date start = c.getTime();
		c.add(Calendar.MINUTE, 30);
		Date end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Early Morning Task", 0.3)));
		
		c.set(Calendar.HOUR_OF_DAY, 20);
		c.set(Calendar.MINUTE, 15);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 12);
		end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Overnight 1", 0.3)));
		
		c.add(Calendar.HOUR_OF_DAY, 2);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 74);
		end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Overnight Full Days", 0.3)));
		
		c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		c.set(Calendar.HOUR_OF_DAY, 11);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 2);
		c.add(Calendar.MINUTE, 30);
		end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Sunday Brunch", 0.3)));
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
		brush.setColor(Utils.COLOR_LIGHT_BG);
		brush.fillRect(0, 0, X_OFFSET, getHeight());
		
		// Do the vertical lines
		for (int i = 1; i < DAYS; i++) {
			final double x = (i / DAYS) * (getWidth() - X_OFFSET) + X_OFFSET;
			brush.draw(new Line2D.Double(x, 0, x, getHeight()));
		}
		
		for (int i = 0; i < HRS; i++) {
			final double y = (i / HRS) * (getHeight() - Y_PAD) + Y_PAD;
			
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
			placeAndDrawBlock(brush, t);
		}
		
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
	 * Simply calculates an x position from a number of hours (double for min inclusion)
	 * 
	 * @param time an hour/min time
	 * @return a value to set for X
	 */
	private int getXPos(final double time) {
		return (int) ((time / DAYS) * (getWidth() - X_OFFSET) + X_OFFSET);
	}
	
	/**
	 * Draws an actual block
	 * 
	 * @param g the graphics object
	 * @param t the block itself
	 * @param rect where this block should be drawn
	 */
	private static void drawBlock(final Graphics2D g, final ITimeBlockable t, final Rectangle2D.Double rect) {
		final Random r = new Random(t.hashCode());
		final Color currColor = t.isMovable() ? new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255))
				: Utils.COLOR_ALTERNATE;
		g.setColor(currColor);
		g.fill(rect);
		g.setColor(Utils.COLOR_FOREGROUND);
		g.draw(rect);
		
		// Make title parts
		g.setColor(contrastingColor(currColor));
		final List<String> titleParts = new ArrayList<>(4);
		titleParts.add(t.getTask().getName());
		int i = 0;
		
		// Iterate while i is less than size and only two lines of text appear
		while (i < titleParts.size() && i < 2) {
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
		
		// Finally, draw the title
		for (i = 0; i < titleParts.size(); i++) {
			g.drawString(titleParts.get(i), (int) rect.getX() + 5, (int) rect.getY() + (15 * (i + 1)));
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
		return (bright < 130) ? Color.WHITE : Color.BLACK;
	}
	
	/**
	 * Draws a block to canvas
	 * 
	 * @param brush the brush for the canvas
	 * @param t a time blockable to draw
	 */
	private void placeAndDrawBlock(final Graphics2D brush, final ITimeBlockable t) {
		final Calendar c = Calendar.getInstance();
		c.setTime(t.getStart());
		final double startTime = c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0);
		int currDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		c.setTime(t.getEnd());
		final double endTime = c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0);
		final int endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		
		// Shared measurements
		final double dayWidth = (getWidth() - X_OFFSET) / DAYS;
		int x = getXPos(currDay);
		
		// Basic, works
		if (currDay == endDay) {
			final int y = getYPos(startTime);
			final int height = (int) (getYPos(endTime - startTime) - Y_PAD);
			drawBlock(brush, t, new Rectangle2D.Double(x, y, dayWidth, height));
			
		}
		
		// Different days, overnight
		else {
			// First day
			final int firstDayHeight = getYPos(HRS - startTime);
			final int startY = getYPos(startTime);
			drawBlock(brush, t, new Rectangle2D.Double(x, startY, dayWidth, firstDayHeight));
			
			// For literal full days, just draw a huge rectangle and move on
			currDay++;
			while (currDay <= DAYS && currDay != endDay) {
				x = getXPos(currDay);
				drawBlock(brush, t, new Rectangle2D.Double(x, 0, dayWidth, getHeight()));
				currDay++;
			}
			
			// If ends during the current week, draw it
			if (currDay <= DAYS) {
				x = getXPos(endDay);
				final int secondDayHeight = getYPos(endTime);
				drawBlock(brush, t, new Rectangle2D.Double(x, 0, dayWidth, secondDayHeight));
			}
		}
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
