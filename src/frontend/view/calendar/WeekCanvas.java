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
import java.awt.event.ActionEvent;
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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import backend.database.StorageService;
import data.ITimeBlockable;
import data.Tuple;
import frontend.Utils;
import frontend.view.CanvasUtils;
import frontend.view.calendar.CalendarView.DragType;

/**
 * Draws a week to canvas representation
 * 
 * @author dgattey
 */
public class WeekCanvas extends JPanel implements MouseListener, MouseMotionListener, Scrollable {
	
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
	 */
	public WeekCanvas(final CalendarView cv) {
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
		
		final Calendar c = CalendarView.getCalendarInstance();
		c.set(Calendar.WEEK_OF_YEAR, _cv.getCurrentWeek());
		c.set(Calendar.YEAR, _cv.getCurrentYear());
		c.set(Calendar.MINUTE, min);
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.DAY_OF_WEEK, day);
		
		System.out.println(c.getTime());
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
		
		// Today
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
				brush.drawString(getHourString(i), 5, (int) (getHeight() - Y_PAD));
			} else {
				brush.drawString(getHourString(i), 5, (int) y + 5);
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
		Date startDate = c.getTime();
		int startDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		c.setTime(t.getEnd());
		Date endDate = c.getTime();
		int endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
		
		// Sets correct start bounds
		c.setTime(t.getStart());
		int startX = getXPos((int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS));
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
		c.setTime(t.getEnd());
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
	private static void drawBlock(final Graphics2D g, final ITimeBlockable t, final TimeRect rect) {
		g.setColor(rect.getColor());
		g.fill(rect);
		g.setColor(t.getEnd().before(new Date()) ? rect.getStrokeColor().darker() : rect.getStrokeColor());
		g.setStroke(rect.getStroke());
		g.draw(rect);
		
		// For unavailable blocks, doesn't draw them
		if (t.getTask() == null) {
			return;
		}
		
		// Wrap titles and draw them
		final Font aFont = Utils.getFont(Font.BOLD, 12);
		final Font taskFont = Utils.getFont(Font.PLAIN | Font.ITALIC, 11);
		final List<String> taskT = new ArrayList<>(4);
		final List<String> assT = new ArrayList<>(2);
		taskT.add(t.getTask().getName());
		assT.add(StorageService.getAssignment(t.getTask().getAssignmentID()).getName());
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
		_cv.clearMovingBlock();
		_dragCurrPoint = e.getPoint();
		final TimeRect rect = getRectForPoint(e.getPoint());
		
		// Doesn't allow editing of blocks before the current time
		if (rect == null) {
			return;
		}
		if (convertPointToTime(new Point((int) rect.getMinX(), (int) rect.getMaxY()), false).before(new Date())) {
			_cv.repaint();
			return;
		}
		
		// Drag type is either top, bottom, or full (default full)
		DragType drag = DragType.FULL;
		if (CanvasUtils.atBottomEdge(e, rect, getHeight() - Y_PAD)) {
			drag = DragType.BOTTOM;
		} else if (CanvasUtils.atTopEdge(e, rect, Y_PAD)) {
			drag = DragType.TOP;
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
		
		// Scroll to view if need be
		if (_cv.getMovingBlock() != null) {
			
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
			
			// Toss it in the database if in the right order and no overlap
			if (!end.before(start) && !checkBlockForOverlap(start, end, oldTask)) {
				HubController.changeTimeBlock(oldTask, start, end);
			}
		}
		
		// Reset highlights
		_dragCurrPoint = null;
		_cv.clearMovingBlock();
		_cv.reloadData();
		_cv.repaint();
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
	
	// All dimension code below sets information to be better scrollable
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	@Override
	public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
		return 10;
	}
	
	@Override
	public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
		return 10;
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
