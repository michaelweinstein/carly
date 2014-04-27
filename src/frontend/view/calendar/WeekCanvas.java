package frontend.view.calendar;

import static frontend.view.calendar.CanvasConstants.DAYS;
import static frontend.view.calendar.CanvasConstants.HRS;
import static frontend.view.calendar.CanvasConstants.X_OFFSET;
import static frontend.view.calendar.CanvasConstants.Y_PAD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import data.ITimeBlockable;
import data.Tuple;
import frontend.Utils;

/**
 * Draws a week to canvas representation
 * 
 * @author dgattey
 */
public class WeekCanvas extends JPanel implements MouseListener, MouseMotionListener {
	
	private static final long	serialVersionUID	= 1L;
	private final CalendarView	_cv;
	private Date				_weekStartDate;
	private Date				_weekEndDate;
	private Point				_mousePoint;
	
	/**
	 * Class of rectangle to use only for drawing - associates with others of same time automatically
	 * 
	 * @author dgattey
	 */
	private static final class TimeRect extends Rectangle2D.Double {
		
		private static final long									serialVersionUID	= 1L;
		private static final Map<ITimeBlockable, List<TimeRect>>	allBlocks			= new HashMap<>();
		private static final Set<ITimeBlockable>					highlightedBlocks	= new HashSet<>();
		
		private final Color											_c;
		private final ITimeBlockable								_t;
		
		/**
		 * Creates a new rectangle with the given size and associates t with this rect
		 * 
		 * @param x x pos
		 * @param y y pos
		 * @param w width of rect
		 * @param h height of rect
		 * @param t the TimeBlockable to associate with this
		 */
		public TimeRect(final double x, final double y, final double w, final double h, final ITimeBlockable t) {
			super(x, y, w, h);
			_t = t;
			_c = CanvasConstants.getColor(_t);
			
			// Adds to all blocks for use later
			List<TimeRect> currBlocks = allBlocks.get(t);
			if (currBlocks == null) {
				currBlocks = new ArrayList<>();
			}
			currBlocks.add(this);
			allBlocks.put(t, currBlocks);
		}
		
		/**
		 * Returns the color of this block
		 * 
		 * @return the expected color
		 */
		public Color getColor() {
			return highlightedBlocks.contains(_t) ? new Color(_c.getRed(), _c.getGreen(), _c.getBlue(), 100) : _c;
		}
		
		/**
		 * Returns the color of the stroke
		 * 
		 * @return the stroke color
		 */
		public Color getStrokeColor() {
			return highlightedBlocks.contains(_t) ? Utils.COLOR_FOREGROUND : Utils.COLOR_LIGHT_BG;
		}
		
		/**
		 * Returns the actual stroke
		 * 
		 * @return the stroke to set around the block
		 */
		public Stroke getStroke() {
			if (highlightedBlocks.contains(_t)) {
				final float dash1[] = { 4.0f };
				return new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash1, 0);
			}
			return new BasicStroke(1);
		}
		
		/**
		 * Clears it out!
		 */
		public static void clear() {
			allBlocks.clear();
		}
		
		/**
		 * Gets the blockable object for a given point
		 * 
		 * @param mousePoint a point in 2D space
		 * @return an ITimeBlockable for a given point
		 */
		public static ITimeBlockable blockForPoint(final Point mousePoint) {
			for (final ITimeBlockable t : allBlocks.keySet()) {
				final List<TimeRect> list = allBlocks.get(t);
				
				// Checks each block for containment
				for (final TimeRect rec : list) {
					if (rec.contains(mousePoint)) {
						return t;
					}
				}
			}
			return null;
		}
		
		/**
		 * Checks for dates overlapping
		 * 
		 * @param d1 a tuple of date ranges
		 * @param d2 another tuple of date ranges
		 * @return if d1 and d2 overlap
		 */
		private static boolean overlaps(final Tuple<Date, Date> d1, final Tuple<Date, Date> d2) {
			final boolean d1StartInD2 = (d1.a.before(d2.b) || d1.a.equals(d2.b))
				&& (d1.a.after(d2.a) || d1.a.equals(d2.a));
			final boolean d1EndInD2 = (d1.b.before(d2.b) || d1.b.equals(d2.b))
				&& (d1.b.after(d2.a) || d1.b.equals(d2.a));
			final boolean d1EncompassesD2 = (d1.a.before(d2.a) || d1.a.equals(d2.a))
				&& (d1.b.after(d2.b) || d1.b.equals(d2.b));
			return d1StartInD2 || d1EndInD2 || d1EncompassesD2;
		}
		
		/**
		 * Take a time blockable and a new start position and check for overlap across all things
		 * 
		 * @param newStart a new start date
		 * @param oldBlock the old time blockable
		 * @return if oldBlock with a new date overlaps with any other block
		 */
		public static boolean checkForOverlap(final Date newStart, final ITimeBlockable oldBlock) {
			final Date newEnd = new Date(newStart.getTime() + oldBlock.getLength());
			for (final ITimeBlockable t : allBlocks.keySet()) {
				if (!t.equals(oldBlock)
					&& overlaps(new Tuple<>(newStart, newEnd), new Tuple<>(t.getStart(), t.getEnd()))) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Constructor
	 * 
	 * @param cv the calendar view containing this
	 */
	public WeekCanvas(final CalendarView cv) {
		_cv = cv;
		addMouseListener(this);
		addMouseMotionListener(this);
		
		// Scrolls the view up and down with arrows
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");
		getActionMap().put("up", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Rectangle newR = getVisibleRect();
				newR.translate(0, -30);
				scrollRectToVisible(newR);
			}
		});
		getActionMap().put("down", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Rectangle newR = getVisibleRect();
				newR.translate(0, 30);
				scrollRectToVisible(newR);
			}
		});
	}
	
	/**
	 * Clears leftover highlights
	 */
	public void clearHighlights() {
		TimeRect.highlightedBlocks.clear();
	}
	
	/**
	 * Given an x and y, "snaps" to the nearest 15 minute time
	 * 
	 * @param p an x,y point on the canvas
	 * @return the date represented by (x,y) on the canvas, snapped to the nearest 15 minutes
	 */
	public Date getTimeForLocation(final Point p) {
		final int day = (int) Math.floor(((p.getX() - X_OFFSET) / (getWidth() - X_OFFSET)) * DAYS) + 1;
		final double hrsAndMin = HRS * ((p.getY() - Y_PAD) / (getHeight() - Y_PAD));
		final int hours = (int) Math.floor(hrsAndMin);
		final int min = 15 * (int) (Math.floor(((hrsAndMin - hours) * 4)));
		
		final Calendar c = CalendarView.getCalendarInstance();
		c.set(Calendar.MINUTE, min);
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.DAY_OF_WEEK, day);
		
		return c.getTime();
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
		
		// Reloads week start and end date, plus clears the previous rectangle array
		_weekStartDate = _cv.getCurrentWeekStartDate();
		_weekEndDate = _cv.getCurrentWeekEndDate();
		TimeRect.clear();
		
		// Takes blockables and makes them into rects
		brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 12));
		ITimeBlockable highlight = null;
		for (final ITimeBlockable t : _cv.getTimeBlocks()) {
			if (t.equals(_cv.getHighlightedTask())) {
				highlight = t;
			} else {
				placeAndDrawBlock(brush, t);
			}
		}
		if (highlight != null) {
			placeAndDrawHighlightedBlock(brush, highlight);
		}
		
	}
	
	/**
	 * Simply draws the highlighted block at the mouse point
	 * 
	 * @param brush the brush for the canvas
	 * @param t a time blockable to draw
	 */
	private void placeAndDrawHighlightedBlock(final Graphics2D brush, final ITimeBlockable t) {
		// Checks bounds so we know not to place line if dates don't match up
		if (t.getStart().after(_weekEndDate) || t.getEnd().before(_weekStartDate)) {
			return;
		}
		
		// Shared measurements
		final double dayWidth = (getWidth() - X_OFFSET) / DAYS;
		final Calendar c = CalendarView.getCalendarInstance();
		
		c.setTime(t.getStart());
		final int startY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
		final int startDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		c.setTime(t.getEnd());
		final int endY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
		final int endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		
		double height = 300;
		
		// We know it's the same day if the following is true - otherwise, approximate it with 300
		if (startDay == endDay && !t.getStart().before(_weekStartDate) && !t.getEnd().after(_weekEndDate)) {
			height = endY - startY;
		}
		
		final TimeRect rect = new TimeRect(_mousePoint.getX(), _mousePoint.getY(), dayWidth, height, t);
		drawBlock(brush, t, rect);
		
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
		if (startX == endX && startDay == endDay) {
			final int height = endY - startY;
			drawBlock(brush, t, new TimeRect(startX, startY, dayWidth, height, t));
			return;
		}
		
		// For events spanning at least one night
		drawBlock(brush, t, new TimeRect(startX, startY, dayWidth, getHeight() - startY, t));
		for (int i = startDay + 1; i < endDay; i++) {
			// Draw full day
			drawBlock(brush, t, new TimeRect(getXPos(i), 0, dayWidth, getHeight(), t));
		}
		drawBlock(brush, t, new TimeRect(endX, 0, dayWidth, endY, t));
	}
	
	/**
	 * Draws an actual block
	 * 
	 * @param g the graphics object
	 * @param t the block itself
	 * @param rect where this block should be drawn
	 */
	private static void drawBlock(final Graphics2D g, final ITimeBlockable t, final TimeRect rect) {
		g.setColor(rect.getColor());
		g.fill(rect);
		g.setColor(rect.getStrokeColor());
		g.setStroke(rect.getStroke());
		g.draw(rect);
		
		// Draw title
		g.setColor(contrastingColor(rect.getColor()));
		final List<String> titleParts = new ArrayList<>(4);
		titleParts.add(t.getTask().getName());
		int i = 0;
		
		// Go through the title parts and check the bounds of each word - while too big, put on next line
		for (i = 0; i < titleParts.size() && i < 3; ++i) {
			String currPart = titleParts.get(i);
			while (g.getFontMetrics().getStringBounds(currPart, g).getWidth() >= rect.getWidth() - 10) {
				
				// Split the current title part into words
				final String[] words = currPart.split("\\s+");
				if (words.length > 1) {
					// Put the last word on the next line
					final String part2 = words[words.length - 1] + " ";
					currPart = currPart.substring(0, currPart.length() - part2.length());
					titleParts.set(i, currPart);
					
					// Actually put it in the array
					if (titleParts.size() - 1 == i) {
						titleParts.add(part2);
					} else {
						titleParts.set(i + 1, part2 + titleParts.get(i + 1));
					}
				} else {
					// Last word is too long to fit on line, but no more left, so just set ...
					titleParts.set(i, "...");
					break;
				}
			}
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
		return hour + (i < 12 || i == 24 ? "am" : "pm");
	}
	
	@Override
	public void mousePressed(final MouseEvent e) {
		_mousePoint = e.getPoint();
		_cv.setHighlightedTask(null);
		final ITimeBlockable t = TimeRect.blockForPoint(_mousePoint);
		if (t != null) {
			TimeRect.highlightedBlocks.add(t);
			_cv.setHighlightedTask(t);
			_cv.repaint();
		}
	}
	
	@Override
	public void mouseDragged(final MouseEvent e) {
		_mousePoint = e.getPoint();
		final Rectangle visible = getVisibleRect();
		
		// If highlighted and within x bounds
		if (_cv.getHighlightedTask() != null
			&& (e.getX() > visible.getX() || e.getX() < visible.getX() + visible.getWidth())) {
			
			// If above visible rect, scroll up
			if (e.getY() < visible.getY()) {
				visible.translate(0, -20);
				scrollRectToVisible(visible);
			}
			
			// If below visible rect, scroll down
			else if (e.getY() > visible.getY() + visible.getHeight()) {
				visible.translate(0, 20);
				scrollRectToVisible(visible);
			}
		}
		_cv.repaint();
	}
	
	@Override
	public void mouseReleased(final MouseEvent e) {
		// Figure out where it dropped and update info
		if (_mousePoint != null && _cv.getHighlightedTask() != null) {
			final Date d = getTimeForLocation(_mousePoint);
			if (!TimeRect.checkForOverlap(d, _cv.getHighlightedTask())) {
				final ITimeBlockable newBlock = _cv.getHighlightedTask();
				final long len = newBlock.getLength();
				newBlock.setStart(d);
				newBlock.setEnd(new Date(d.getTime() + len));
				_cv.replaceTimeBlock(_cv.getHighlightedTask(), newBlock);
			}
		}
		
		// Reset highlights
		_mousePoint = null;
		TimeRect.highlightedBlocks.remove(_cv.getHighlightedTask());
		_cv.setHighlightedTask(null);
		_cv.repaint();
	}
	
	@Override
	public void mouseMoved(final MouseEvent e) {}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100, 2000);
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {}
	
	@Override
	public void mouseEntered(final MouseEvent e) {}
	
	@Override
	public void mouseExited(final MouseEvent e) {}
}
