package frontend.view.calendar;

import static frontend.view.DrawingConstants.DAYS;
import static frontend.view.DrawingConstants.HRS;
import static frontend.view.DrawingConstants.X_OFFSET;
import static frontend.view.DrawingConstants.Y_PAD;
import hub.HubController;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import data.ITimeBlockable;
import data.Tuple;
import frontend.Utils;

/**
 * Draws a week to canvas representation
 * 
 * @author dgattey
 */
public class WeekCanvas extends JPanel implements MouseListener, MouseMotionListener {
	
	private static final long							serialVersionUID	= 1L;
	private static final int							CURSOR_CUST			= Cursor.N_RESIZE_CURSOR;
	private static final int							CURSOR_DEF			= Cursor.DEFAULT_CURSOR;
	private final Map<ITimeBlockable, List<TimeRect>>	_allBlocks;
	private final Set<ITimeBlockable>					_highlightedBlocks;
	private final CalendarView							_cv;
	private Date										_weekStartDate;
	private Date										_weekEndDate;
	private Point										_mousePoint;
	private final Timer									_timer;
	
	/**
	 * Constructor
	 * 
	 * @param cv the calendar view containing this
	 */
	public WeekCanvas(final CalendarView cv) {
		_cv = cv;
		_allBlocks = new HashMap<>();
		_highlightedBlocks = new HashSet<>();
		_timer = new Timer();
		_timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						_cv.reloadApp();
					}
				});
			}
		}, 5000, 60000); // Once a minute, repaint (after 5 seconds)
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
		getHighlightedBlocks().clear();
	}
	
	/**
	 * Clears it out!
	 */
	public void clearAllBlocks() {
		getAllBlocks().clear();
	}
	
	/**
	 * Gets the blockable object for a given point
	 * 
	 * @param mousePoint a point in 2D space
	 * @return an ITimeBlockable for a given point
	 */
	public ITimeBlockable getBlockForPoint(final Point mousePoint) {
		for (final ITimeBlockable t : getAllBlocks().keySet()) {
			final List<TimeRect> list = getAllBlocks().get(t);
			
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
	 * Gets the timerect for a given point
	 * 
	 * @param mousePoint a point in 2D space
	 * @return an ITimeBlockable for a given point
	 */
	public TimeRect getRectForPoint(final Point mousePoint) {
		for (final ITimeBlockable t : getAllBlocks().keySet()) {
			final List<TimeRect> list = getAllBlocks().get(t);
			
			// Checks each block for containment
			for (final TimeRect rec : list) {
				if (rec.contains(mousePoint)) {
					return rec;
				}
			}
		}
		return null;
	}
	
	/**
	 * Take a time blockable and a new start position and check for overlap across all things
	 * 
	 * @param newStart a new start date
	 * @param oldBlock the old time blockable
	 * @return if oldBlock with a new date overlaps with any other block
	 */
	public boolean checkBlockForOverlap(final Date newStart, final ITimeBlockable oldBlock) {
		final Date newEnd = new Date(newStart.getTime() + oldBlock.getLength());
		for (final ITimeBlockable t : getAllBlocks().keySet()) {
			if (!t.equals(oldBlock)
				&& Utils.dateRangesOverlap(new Tuple<>(newStart, newEnd), new Tuple<>(t.getStart(), t.getEnd()))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Given an x and y, "snaps" to the nearest 10 minute time
	 * 
	 * @param p an x,y point on the canvas
	 * @return the date represented by (x,y) on the canvas, snapped to the nearest 15 minutes
	 */
	public Date getTimeForLocation(final Point p) {
		int day = (int) Math.floor(((p.getX() - X_OFFSET) / (getWidth() - X_OFFSET)) * DAYS) + 1;
		day = (day < 1) ? 1 : (day > 7) ? 7 : day;
		final double hrsAndMin = HRS * ((p.getY() - Y_PAD) / (getHeight() - Y_PAD));
		final int hours = (int) Math.floor(hrsAndMin);
		final int min = 10 * (int) (Math.round(((hrsAndMin - hours) * 6)));
		
		final Calendar c = CalendarView.getCalendarInstance();
		c.set(Calendar.WEEK_OF_YEAR, _cv.getCurrentWeek());
		c.set(Calendar.YEAR, _cv.getCurrentYear());
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
		clearAllBlocks();
		
		// Takes blockables and makes them into rects
		brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 12));
		for (final ITimeBlockable t : _cv.getTimeBlocks()) {
			if (!t.equals(_cv.getHighlightedTask())) {
				placeAndDrawBlock(brush, t);
			}
		}
		for (final ITimeBlockable t : _cv.getUnavailableTimeBlocks()) {
			placeAndDrawBlock(brush, t);
		}
		
		// Line for the current time on the current week
		if (_weekStartDate.before(new Date()) && _weekEndDate.after(new Date())) {
			final Calendar c = CalendarView.getCalendarInstance();
			final double min = Math.round((c.get(Calendar.MINUTE) / 60.0) * 12) * 5;
			final int y = getYPos(c.get(Calendar.HOUR_OF_DAY) + min / 60.0);
			
			brush.setStroke(new BasicStroke(3));
			brush.setColor(Utils.COLOR_ACCENT);
			brush.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 12));
			brush.drawString("Now", X_OFFSET + 15, y + 4);
			brush.drawLine(X_OFFSET + 48, y, getWidth(), y);
			brush.drawLine(X_OFFSET, y, X_OFFSET + 8, y);
		}
		
		// Finally, draw the highlighted block
		if (_cv.getHighlightedTask() != null) {
			placeAndDrawHighlightedBlock(brush, _cv.getHighlightedTask());
		}
		
	}
	
	/**
	 * Simply draws the highlighted block at the mouse point
	 * 
	 * @param brush the brush for the canvas
	 * @param t a time blockable to draw
	 */
	private void placeAndDrawHighlightedBlock(final Graphics2D brush, final ITimeBlockable t) {
		
		// Shared measurements
		final double dayWidth = (getWidth() - X_OFFSET) / DAYS;
		final Calendar c = CalendarView.getCalendarInstance();
		c.set(Calendar.WEEK_OF_YEAR, _cv.getCurrentWeek());
		c.set(Calendar.YEAR, _cv.getCurrentYear());
		
		c.setTime(t.getStart());
		final int startY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
		final int startDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		c.setTime(t.getEnd());
		final int endY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
		final int endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		
		double height = 300;
		
		// We know it's the same day if the following is true - otherwise, approximate it with 300
		if (startDay == endDay) {
			height = endY - startY;
		}
		
		final TimeRect rect = new TimeRect(_mousePoint.getX(), _mousePoint.getY(), dayWidth, height, t, this);
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
			drawBlock(brush, t, new TimeRect(startX, startY, dayWidth, height, t, this));
			return;
		}
		
		// For events spanning at least one night
		drawBlock(brush, t, new TimeRect(startX, startY, dayWidth, getHeight() - startY, t, this));
		for (int i = startDay + 1; i < endDay; i++) {
			// Draw full day
			drawBlock(brush, t, new TimeRect(getXPos(i), 0, dayWidth, getHeight(), t, this));
		}
		drawBlock(brush, t, new TimeRect(endX, 0, dayWidth, endY, t, this));
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
		
		// Wrap titles and draw them
		final Font aFont = new Font(Utils.APP_FONT_NAME, Font.BOLD, 12);
		final Font taskFont = new Font(Utils.APP_FONT_NAME, Font.PLAIN | Font.ITALIC, 11);
		final List<String> taskT = new ArrayList<>(4);
		final List<String> assT = new ArrayList<>(2);
		final String fullTitle = t.getTask().getName();
		final String[] split = fullTitle.split(":");
		taskT.add(fullTitle.substring(split[0].length() + 1));
		assT.add(split[0]);
		int i = 0;
		
		// Wrap the assignment title
		g.setFont(aFont);
		for (i = 0; i < assT.size() && i < 3; ++i) {
			wrapTitleParts(assT, i, g, rect.getWidth() - 10);
		}
		if (i < assT.size()) {
			assT.set(i, "...");
		}
		
		// Wrap the task title
		g.setFont(taskFont);
		for (i = 0; i < taskT.size() && i < 3; ++i) {
			wrapTitleParts(taskT, i, g, rect.getWidth() - 10);
		}
		if (i < taskT.size()) {
			taskT.set(i, "...");
		}
		
		// Finally, draw the titles until they don't fit
		final int xPos = (int) rect.getX();
		final int space = 15;
		int yPos = 0;
		final double nextY = 0;
		g.setColor(Utils.contrastingColor(rect.getColor()));
		g.setFont(aFont);
		for (i = 0; i < taskT.size() + assT.size(); i++) {
			yPos = (int) rect.getY() + (space * (i + 1));
			
			// Change fonts and colors
			if (i == assT.size()) {
				g.setFont(taskFont);
				yPos += 5;
			}
			
			// Over length of block
			if (yPos >= rect.getMaxY()) {
				return;
			}
			
			final String toDraw = i < assT.size() ? assT.get(i) : taskT.get(i - assT.size());
			g.drawString(nextY >= rect.getMaxY() ? "..." : toDraw, xPos + 5, yPos);
		}
		
	}
	
	/**
	 * Wraps the title parts in the array for the current index
	 * 
	 * @param titleParts an list of strings to wrap
	 * @param i the index
	 * @param g the graphics object to use
	 * @param w the width of the spot to put it
	 */
	private static void wrapTitleParts(final List<String> titleParts, final int i, final Graphics2D g, final double w) {
		String currPart = titleParts.get(i);
		while (g.getFontMetrics().getStringBounds(currPart, g).getWidth() >= w) {
			
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
	 * Getter for highlighted blocks
	 * 
	 * @return the set of highlighted blocks
	 */
	public Set<ITimeBlockable> getHighlightedBlocks() {
		return _highlightedBlocks;
	}
	
	/**
	 * Getter for all blocks
	 * 
	 * @return the current map of time blockable to list of rects
	 */
	public Map<ITimeBlockable, List<TimeRect>> getAllBlocks() {
		return _allBlocks;
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
	
	/**
	 * Sets the block for the mouse point to be selected
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		_mousePoint = e.getPoint();
		_cv.setHighlightedTask(null);
		final ITimeBlockable t = getBlockForPoint(_mousePoint);
		if (t != null) {
			getHighlightedBlocks().add(t);
			_cv.setHighlightedTask(t);
			_cv.repaint();
		}
	}
	
	/**
	 * Scrolls view as well as repainting for the highlighted block drawing to do its thing
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		_mousePoint = e.getPoint();
		final Rectangle visible = getVisibleRect();
		if (_cv.getHighlightedTask() != null) {
			
			// Scroll left by a week if under min x (or right if over max x) and hasn't switched recently
			if (e.getX() < visible.getX() && System.currentTimeMillis() - _cv.getTimeChanged() > 500) {
				_cv.shiftWeekBackwardWithHighlights();
			} else if (e.getX() > visible.getMaxX() && System.currentTimeMillis() - _cv.getTimeChanged() > 500) {
				_cv.shiftWeekForwardWithHighlights();
			} else {
				// If in bounds and above visible rect, scroll up
				if (e.getY() < visible.getY()) {
					visible.translate(0, -20);
					scrollRectToVisible(visible);
				}
				
				// If in bounds and below visible rect, scroll down
				else if (e.getY() > visible.getY() + visible.getHeight()) {
					visible.translate(0, 20);
					scrollRectToVisible(visible);
				}
			}
		}
		_cv.repaint();
	}
	
	/**
	 * Resets the highlights, updates info
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		// Figure out where the block dropped and update info
		final ITimeBlockable task = _cv.getHighlightedTask();
		if (_mousePoint != null && task != null) {
			final Date start = getTimeForLocation(_mousePoint);
			if (!checkBlockForOverlap(start, task)) {
				final Date end = new Date(start.getTime() + task.getLength());
				HubController.changeTimeBlock(task, start, end);
			}
		}
		
		// Reset highlights
		_mousePoint = null;
		_cv.reloadData();
		_cv.repaint();
	}
	
	@Override
	public void mouseMoved(final MouseEvent e) {
		final TimeRect t = getRectForPoint(e.getPoint());
		final int type = getCursor().getType();
		final boolean inXBounds = t != null && e.getX() >= t.getMinX() && e.getX() <= t.getMaxX();
		final boolean atTopEdge = t != null && t.getMinY() + 10 >= e.getY();
		final boolean atBottomEdge = t != null && t.getMaxY() - 10 <= e.getY();
		
		// Top edge or bottom edge
		if (inXBounds && (atTopEdge || atBottomEdge)) {
			if (type != CURSOR_CUST) {
				setCursor(Cursor.getPredefinedCursor(CURSOR_CUST));
			}
		}
		
		// Not default but should be
		else if (type != CURSOR_DEF) {
			setCursor(Cursor.getPredefinedCursor(CURSOR_DEF));
		}
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {}
	
	@Override
	public void mouseEntered(final MouseEvent e) {}
	
	@Override
	public void mouseExited(final MouseEvent e) {}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(100, 2000);
	}
}
