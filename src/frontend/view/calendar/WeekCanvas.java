package frontend.view.calendar;

import static frontend.view.CanvasUtils.DAYS;
import static frontend.view.CanvasUtils.HRS;
import static frontend.view.CanvasUtils.X_OFFSET;
import static frontend.view.CanvasUtils.Y_PAD;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import backend.database.StorageService;
import data.ITimeBlockable;
import data.Tuple;
import data.UnavailableBlock;
import frontend.Utils;
import frontend.view.CanvasUtils;
import frontend.view.ScrollablePanel;
import frontend.view.calendar.CalendarView.DragType;

/**
 * Draws a week to canvas representation
 * 
 * @author dgattey
 */
public class WeekCanvas extends ScrollablePanel implements MouseListener, MouseMotionListener {
	
	private static final long							serialVersionUID	= 1L;
	private static final int							CURSOR_CUST			= Cursor.N_RESIZE_CURSOR;
	private static final int							CURSOR_DEF			= Cursor.DEFAULT_CURSOR;
	private final Map<ITimeBlockable, List<TimeRect>>	_allBlocks;
	private final CalendarView							_cv;
	private Date										_weekStartDate;
	private Date										_weekEndDate;
	private Point										_dragCurrPoint;
	private final Timer									_timer;
	
	/**
	 * Constructor
	 * 
	 * @param cv the calendar view containing this
	 * @param scroller the scroll pane this is added to
	 */
	public WeekCanvas(final CalendarView cv, final JScrollPane scroller) {
		super(scroller);
		_cv = cv;
		_allBlocks = new HashMap<>();
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
		}, 5000, 60000); // Once a minute, reload (after 5 seconds)
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	/**
	 * Clears it out!
	 */
	public void clearAllBlocks() {
		getAllBlocks().clear();
	}
	
	/**
	 * Gets the timerect for a given point
	 * 
	 * @param mousePoint a point in 2D space
	 * @return an ITimeBlockable for a given point
	 */
	public TimeRect getRectForPoint(final Point mousePoint) {
		if (getAllBlocks() == null) {
			return null;
		}
		for (final ITimeBlockable t : getAllBlocks().keySet()) {
			final List<TimeRect> list = getAllBlocks().get(t);
			if (list == null) {
				return null;
			}
			
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
	 * Take a time blockable and new start/end positions and checks for overlap across all blockables
	 * 
	 * @param newStart a new start date
	 * @param newEnd a new end date
	 * @param oldBlock the old time blockable
	 * @return if oldBlock with a new date overlaps with any other block
	 */
	public boolean checkBlockForOverlap(final Date newStart, final Date newEnd, final ITimeBlockable oldBlock) {
		for (final ITimeBlockable t : getAllBlocks().keySet()) {
			if (!t.equals(oldBlock)
				&& Utils.dateRangesOverlap(new Tuple<>(newStart, newEnd), new Tuple<>(t.getStart(), t.getEnd()))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Given an x and y, gets that time ("snaps" to the nearest 10 minute time if snap)
	 * 
	 * @param p an x,y point on the canvas
	 * @param snap if it should snap to nearest time
	 * @return the date represented by (x,y) on the canvas, snapped to the nearest 15 minutes
	 */
	public Date convertPointToTime(final Point p, final boolean snap) {
		int day = (int) Math.floor(((p.getX() + 1 - X_OFFSET) / (getWidth() - X_OFFSET)) * DAYS) + 1;
		day = (day < 1) ? 1 : (day > 7) ? 7 : day;
		final double hrsAndMin = HRS * ((p.getY() - Y_PAD) / (getHeight() - Y_PAD));
		final int hours = (int) Math.floor(hrsAndMin);
		final int min = (snap ? 10 : 1) * (int) (Math.round(((hrsAndMin - hours) * (snap ? 6 : 60))));
		
		// Get the given time
		final Calendar c = CalendarView.getCalendarInstance();
		c.set(Calendar.WEEK_OF_YEAR, _cv.getCurrentWeek());
		c.set(Calendar.YEAR, _cv.getCurrentYear());
		c.set(Calendar.MINUTE, min);
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.DAY_OF_WEEK, day);
		
		// Adjusts for daylight savings and returns
		return c.getTime();
	}
	
	/**
	 * Draws all things for the view!
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		
		// Paints the given view
		paintView(g);
		
	}
	
	/**
	 * Paints the current view
	 * 
	 * @param g the graphics object to start
	 * @return the graphics object created as a result
	 */
	private Graphics paintView(final Graphics g) {
		final Graphics2D brush = (Graphics2D) g;
		brush.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		brush.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// Reloads week start and end date, plus clears the previous rectangle array
		_weekStartDate = _cv.getCurrentWeekStartDate();
		_weekEndDate = _cv.getCurrentWeekEndDate();
		final Tuple<ITimeBlockable, DragType> currentlyMoving = _cv.getMovingBlock();
		clearAllBlocks();
		
		// Today colors
		final double dayWidth = (getWidth() - X_OFFSET - _cv.getScrollWidth()) / DAYS;
		if (_weekStartDate.before(new Date()) && _weekEndDate.after(new Date())) {
			brush.setColor(Utils.transparentColor(Utils.COLOR_ACCENT, 0.05));
			final Calendar c = CalendarView.getCalendarInstance();
			c.setTime(new Date());
			final int i = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
			final int start = (int) ((i / DAYS) * (getWidth() - X_OFFSET) + X_OFFSET);
			brush.fillRect(start, 0, (int) dayWidth + 2, getHeight());
		}
		
		// Background of labels
		brush.setColor(Utils.COLOR_ALTERNATE);
		brush.fillRect(X_OFFSET, 0, getWidth() - X_OFFSET, (int) Y_PAD);
		brush.fillRect(X_OFFSET, (int) (getHeight() - Y_PAD), getWidth() - X_OFFSET, (int) Y_PAD);
		brush.setColor(Utils.COLOR_ALTERNATE);
		brush.fillRect(0, 0, X_OFFSET, getHeight());
		
		// Do the vertical lines
		for (int i = 1; i < DAYS; i++) {
			final double x = (i / DAYS) * (getWidth() - X_OFFSET) + X_OFFSET;
			brush.draw(new Line2D.Double(x, Y_PAD, x, getHeight() - Y_PAD));
		}
		
		// Horizontal lines plus labels
		for (int i = 0; i <= HRS; i++) {
			final double y = (i / HRS) * (getHeight() - Y_PAD) + Y_PAD;
			
			// Horizontal lines
			brush.setColor(Utils.COLOR_LIGHT_BG);
			if (i != 0) {
				brush.draw(new Line2D.Double(X_OFFSET, y, getWidth(), y));
			}
			
			// Text
			brush.setColor(Utils.COLOR_FOREGROUND);
			brush.setFont(Utils.getFont(Font.BOLD, 11));
			if (i == HRS) {
				// Why necessary to special case? I don't know but it doesn't draw right otherwise
				brush.drawString(Utils.getHourString(i), 5, (int) (getHeight() - Y_PAD));
			} else {
				brush.drawString(Utils.getHourString(i), 5, (int) y + 5);
			}
		}
		
		// Takes blockables and makes them into rects - Draws all non-moving blocks first, along with unavailable blocks
		brush.setFont(Utils.getFont(Font.BOLD, 12));
		for (final ITimeBlockable t : _cv.getUnavailableTimeBlocks()) {
			placeBlock(brush, t);
		}
		for (final ITimeBlockable t : _cv.getTimeBlocks()) {
			if ((currentlyMoving != null && !t.equals(currentlyMoving.first)) || currentlyMoving == null) {
				placeBlock(brush, t);
			}
		}
		
		// Line for the current time on the current week
		if (_weekStartDate.before(new Date()) && _weekEndDate.after(new Date())) {
			final Calendar c = CalendarView.getCalendarInstance();
			final double min = Math.round((c.get(Calendar.MINUTE) / 60.0) * 12) * 5;
			final int y = getYPos(c.get(Calendar.HOUR_OF_DAY) + min / 60.0);
			
			brush.setStroke(new BasicStroke(3));
			brush.setColor(Utils.COLOR_ACCENT);
			brush.setFont(Utils.getFont(Font.BOLD, 12));
			brush.drawString("Now", X_OFFSET + 15, y + 4);
			brush.drawLine(X_OFFSET + 48, y, getWidth(), y);
			brush.drawLine(X_OFFSET, y, X_OFFSET + 8, y);
		}
		
		// Finally, draw the moving blocks
		if (currentlyMoving != null) {
			placeBlock(brush, currentlyMoving.first);
		}
		return brush;
	}
	
	/**
	 * Deals with daylight savings by taking a calendar and a date and normalizing them. The gist of it is that we only
	 * care if it's DST if we're on a week that doesn't start and end in the same setting. If they're different, that
	 * means drawing WILL fail because things are longer/shorter than they should be. So for the current week, just
	 * adjust the display of the actual date and all should be well!
	 * 
	 * @param time the time we start with
	 * @param c the calendar to adjust
	 * @return the time adjusted for DS
	 */
	private Date adjustForDaylightSavings(Date time, final Calendar c) {
		final TimeZone tz = TimeZone.getDefault();
		final boolean startDS = tz.inDaylightTime(_weekStartDate);
		final boolean endDS = tz.inDaylightTime(_weekEndDate);
		if (startDS != endDS) {
			final boolean currDS = tz.inDaylightTime(time);
			c.setTime(time);
			
			// Fall back because beginning is not in DS
			if (!startDS && currDS) {
				c.add(Calendar.MINUTE, -60);
				time = c.getTime();
			}
			
			// Spring forward because the beginning is DS
			else if (startDS && !currDS) {
				c.add(Calendar.MINUTE, 60);
				time = c.getTime();
			}
		}
		return time;
	}
	
	/**
	 * Draws a block to canvas
	 * 
	 * @param brush the brush for the canvas
	 * @param t a time blockable to draw
	 */
	private void placeBlock(final Graphics2D brush, final ITimeBlockable t) {
		final Tuple<ITimeBlockable, DragType> moving = _cv.getMovingBlock();
		
		// Checks bounds so we know not to place line if dates don't match up and not the moving block
		if ((moving != null && !t.equals(moving.first) || moving == null)
			&& (t.getStart().after(_weekEndDate) || t.getEnd().before(_weekStartDate))) {
			return;
		}
		
		// Shared measurements
		final double dayWidth = (getWidth() - X_OFFSET) / DAYS;
		final Calendar c = CalendarView.getCalendarInstance();
		c.setTime(t.getStart());
		
		Date startDate = adjustForDaylightSavings(c.getTime(), c);
		int startDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		c.setTime(t.getEnd());
		Date endDate = adjustForDaylightSavings(c.getTime(), c);
		int endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		// Sets correct start bounds
		c.setTime(startDate);
		
		int startX = getXPos(startDay);
		int startY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
		if (moving != null && moving.first.equals(t)) {
			final int set = (int) Math.min(Math.max(_dragCurrPoint.getY(), Y_PAD + 1), getHeight() - Y_PAD);
			switch (moving.second) {
			case FULL:
				startX = (int) _dragCurrPoint.getX();
				startY = set;
				startDate = convertPointToTime(new Point(startX, startY), false);
				break;
			case TOP:
				c.setTime(convertPointToTime(new Point((int) _dragCurrPoint.getX(), set), false));
				startDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
				startX = getXPos(startDay);
				startY = set;
				startDate = convertPointToTime(new Point(startX, startY), false);
				break;
			default:
				break;
			}
		}
		if (startDate.before(_weekStartDate)) {
			startY = 0;
			startDay = c.getMinimum(Calendar.DAY_OF_WEEK) - 1;
			startX = getXPos(startDay);
		}
		
		// Sets correct end bounds
		c.setTime(endDate);
		int endX = getXPos((int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS));
		int endY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
		if (moving != null && moving.first.equals(t)) {
			final int set = (int) Math.min(Math.max(_dragCurrPoint.getY(), Y_PAD + 1), getHeight() - Y_PAD);
			switch (moving.second) {
			case FULL:
				if (endDay != startDay) {
					endX = startX;
					endY = startY + 300;
				} else {
					endX = startX;
					c.setTime(new Date(convertPointToTime(new Point(startX, startY), false).getTime() + t.getLength()));
					endY = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
				}
				endDate = convertPointToTime(new Point(endX, endY), false);
				break;
			case BOTTOM:
				c.setTime(convertPointToTime(new Point((int) _dragCurrPoint.getX(), set), false));
				endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
				endX = getXPos(endDay);
				endY = set;
				endDate = convertPointToTime(new Point(endX, endY), false);
				break;
			default:
				break;
			}
		}
		if (endDate.after(_weekEndDate)) {
			endY = getHeight();
			endDay = c.getMaximum(Calendar.DAY_OF_WEEK) - 1;
			endX = getXPos(endDay);
		}
		
		// Sets the nearest 15 min to not make it too short
		capTo15Min(startDate, endDate);
		
		// For a movable block, draws large enough to not truncate the text
		if (!t.isMovable() && moving != null && moving.first.equals(t) && endY - startY < 50) {
			endY = startY + 50;
		}
		
		// Dragging in the wrong direction, won't draw
		if (moving != null && endDate.before(startDate)) {
			return;
		}
		
		// Start and end on same day
		if (startX == endX) {
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
	private void drawBlock(final Graphics2D g, final ITimeBlockable t, final TimeRect rect) {
		g.setClip(rect.createIntersection(getVisibleRect()));
		g.setStroke(rect.getStroke());
		g.setPaint(rect.getColor());
		g.fill(rect);
		
		// If assignment, draw stroke and done - if in past, darker stroke
		if (t.getTask() != null) {
			g.setClip(getVisibleRect());
			g.setPaint(t.getEnd().before(new Date()) ? rect.getStrokeColor().darker() : rect.getStrokeColor());
			g.draw(rect);
		}
		
		// If unavailable block, draw diagonal lines and return
		else {
			g.setPaint(rect.getStrokeColor());
			
			// Draws stripes
			for (double x = rect.x, y2 = rect.y; y2 - rect.height < (rect.y * 2 + rect.height * 2 + rect.width * 2);) {
				g.draw(new Line2D.Double(x, y2, x + rect.width, y2 - rect.width));
				y2 += 5;
			}
			g.setClip(getVisibleRect());
			g.setPaint(rect.getStrokeColor());
			g.draw(rect);
			if (_cv.getMovingBlock() == null || t != _cv.getMovingBlock().first) {
				return;
			}
		}
		
		// Wrap titles and draw them
		final Font aFont = Utils.getFont(Font.BOLD, 12);
		final Font taskFont = Utils.getFont(Font.PLAIN | Font.ITALIC, 11);
		final List<String> taskT = new ArrayList<>(4);
		final List<String> assT = new ArrayList<>(2);
		String text = "Drag off the view to delete";
		String text2 = "Unavailable";
		if (t.isMovable()) {
			text = t.getTask().getName();
			text2 = StorageService.getAssignment(t.getTask().getAssignmentID()).getName();
		}
		taskT.add(text);
		assT.add(text2);
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
		g.setColor(t.getEnd().before(new Date()) ? rect.getStrokeColor() : Utils.contrastingColor(rect.getColor()));
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
		g.setClip(getVisibleRect());
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
	 * Getter for all blocks
	 * 
	 * @return the current map of time blockable to list of rects
	 */
	public Map<ITimeBlockable, List<TimeRect>> getAllBlocks() {
		return _allBlocks;
	}
	
	/**
	 * Getter for the calendar view
	 * 
	 * @return the calendar view
	 */
	public CalendarView getCalendarView() {
		return _cv;
	}
	
	/**
	 * Sets the block for the mouse point to be selected
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		_cv.clearMovingBlock();
		_dragCurrPoint = e.getPoint();
		TimeRect rect = getRectForPoint(e.getPoint());
		
		// Trying to add a new unavailable block
		if (rect == null) {
			// Start a block at the drag point, then adding 15 min to it to end
			final Date start = convertPointToTime(_dragCurrPoint, false);
			final Calendar c = CalendarView.getCalendarInstance();
			c.setTime(start);
			c.add(Calendar.MINUTE, 30);
			final Date end = c.getTime();
			final UnavailableBlock u = new UnavailableBlock(start, end);
			
			// Get locations and make a new rect (constructor puts it in this automatically
			c.setTime(start);
			final int x = getXPos((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
			final int y = getYPos(c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0));
			rect = new TimeRect(x, y, (getWidth() - X_OFFSET) / DAYS, 100, u, this);
		}
		
		// Doesn't allow editing of blocks before the current time
		if (convertPointToTime(new Point((int) rect.getMinX(), (int) rect.getMaxY()), false).before(new Date())) {
			_cv.getApp().presentOneTimeErrorDialog("EDIT",
					"Oops, you can't edit anything in the past! Try again on a block that ends in the future");
			_cv.repaint();
			return;
		}
		
		// Drag type is either top, bottom, or full (default full)
		DragType drag = DragType.FULL;
		if (CanvasUtils.atTopEdge(e, rect, Y_PAD)) {
			drag = DragType.TOP;
		} else if (CanvasUtils.atBottomEdge(e, rect, getHeight() - Y_PAD)) {
			drag = DragType.BOTTOM;
		}
		
		// Add to highlighted stuff and repaint
		_cv.setMovingBlock(rect.getBlockable(), drag);
		_cv.repaint();
	}
	
	/**
	 * Scrolls view as well as repainting for the highlighted block drawing to do its thing
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		_dragCurrPoint = e.getPoint();
		final Rectangle visible = getVisibleRect();
		
		// Scroll to view if need be for available blocks
		if (_cv.getMovingBlock() != null && _cv.getMovingBlock().first.isMovable()) {
			
			// Backward a week
			if (e.getX() < visible.getX() && System.currentTimeMillis() - _cv.getTimeChanged() > 500) {
				_cv.shiftWeekBackwardWithHighlights();
			}
			
			// Forward a week
			else if (e.getX() > visible.getMaxX() && System.currentTimeMillis() - _cv.getTimeChanged() > 500) {
				_cv.shiftWeekForwardWithHighlights();
			} else {
				
				// Up
				if (e.getY() < visible.getY()) {
					visible.translate(0, -20);
					scrollRectToVisible(visible);
				}
				
				// Down
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
		final Tuple<ITimeBlockable, DragType> moving = _cv.getMovingBlock();
		
		// If dragged, update DB
		if (_dragCurrPoint != null && moving != null) {
			final ITimeBlockable oldTask = moving.first;
			final DragType drag = moving.second;
			
			// Get new start and end points
			Date start = oldTask.getStart();
			Date end = oldTask.getEnd();
			switch (drag) {
			case FULL:
				start = convertPointToTime(_dragCurrPoint, true);
				end = new Date(start.getTime() + oldTask.getLength());
				break;
			case TOP:
				start = convertPointToTime(_dragCurrPoint, true);
				break;
			case BOTTOM:
				end = convertPointToTime(_dragCurrPoint, true);
				break;
			default:
				break;
			}
			
			// Sets the nearest 5 min to not make it too short
			capTo15Min(start, end);
			
			// Delete block if unavailable
			if (!oldTask.isMovable()
				&& (_dragCurrPoint.x < 0 || _dragCurrPoint.x > getWidth()
					|| _dragCurrPoint.y < getVisibleRect().getMinY() || _dragCurrPoint.y > getVisibleRect().getMaxY())) {
				StorageService.removeTimeBlock(oldTask);
			}
			
			// Shows a one time error if trying to overlap blocks
			else if (checkBlockForOverlap(start, end, oldTask)) {
				_cv.getApp().presentOneTimeErrorDialog("OVERLAP",
						"Oops, can't drag a block onto another! Blocks must be non-overlapping.");
			} else if (start.before(new Date())) {
				_cv.getApp().presentOneTimeErrorDialog(
						"PAST",
						"Looks like you're trying to put something in the past. Blocks can't be moved into the past. "
							+ "To update your progess, set a task's progress by using the sidebar.");
			}
			
			// Toss it in the database if in the right order
			else if (!end.before(start)) {
				
				// Updates the block in the DB differently depending on AssignmentBlock vs. UnavailableBlock
				if (oldTask.getTask() != null) {
					HubController.changeTimeBlock(oldTask, start, end);
				} else {
					final List<ITimeBlockable> unavailable = new ArrayList<>();
					for (final ITimeBlockable t : getAllBlocks().keySet()) {
						if (t == oldTask) {
							oldTask.setStart(start);
							oldTask.setEnd(end);
						}
						if (t != null && !t.isMovable()) {
							unavailable.add(t);
						}
					}
					HubController.replaceUnavailableBlocks(_weekStartDate, _weekEndDate, unavailable);
				}
			}
		}
		
		// Reset highlights
		_dragCurrPoint = null;
		_cv.clearMovingBlock();
		_cv.reloadData();
		_cv.repaint();
	}
	
	/**
	 * Caps start/end date to be 5 minutes even if under that
	 * 
	 * @param start the start date
	 * @param end the end date
	 */
	private static void capTo15Min(final Date start, final Date end) {
		if (end.getTime() - start.getTime() < TimeUnit.MILLISECONDS.convert(15, TimeUnit.MINUTES)) {
			final Calendar c = CalendarView.getCalendarInstance();
			c.setTime(start);
			c.add(Calendar.MINUTE, 5);
			end.setTime(c.getTimeInMillis());
		}
	}
	
	@Override
	public void mouseMoved(final MouseEvent e) {
		final TimeRect t = getRectForPoint(e.getPoint());
		final int type = getCursor().getType();
		
		// Top edge or bottom edge
		if (t != null && !t.getBlockable().getEnd().before(new Date())
			&& (CanvasUtils.atTopEdge(e, t, Y_PAD) || CanvasUtils.atBottomEdge(e, t, getHeight() - Y_PAD))) {
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
