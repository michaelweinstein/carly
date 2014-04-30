package frontend.view.calendar;

import static frontend.view.CanvasUtils.DAYS;
import static frontend.view.CanvasUtils.HRS;
import static frontend.view.CanvasUtils.X_OFFSET;
import static frontend.view.CanvasUtils.Y_PAD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import data.ITask;
import data.ITimeBlockable;
import frontend.Utils;
import frontend.view.CanvasUtils;

/**
 * Represents a continuous line of calendar data
 * 
 * @author dgattey
 */
public class LineCanvas extends JPanel {
	
	private static final int						MAX_BLOCK_HEIGHT	= 18;
	private static final int						MIN_BLOCK_HEIGHT	= 5;
	private static final long						serialVersionUID	= 1L;
	private final CalendarView						_cv;
	private Date									_weekStartDate;
	private Date									_weekEndDate;
	private int										_y;
	private final Map<ITask, List<ITimeBlockable>>	_taskMap;
	
	/**
	 * Creates a canvas object
	 * 
	 * @param cv the calendar view object
	 */
	public LineCanvas(final CalendarView cv) {
		_cv = cv;
		_taskMap = new HashMap<>();
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
		
		// Week box
		brush.setColor(Utils.COLOR_ALTERNATE);
		brush.fill(new Rectangle2D.Double(0, 0, CanvasUtils.X_OFFSET, getHeight()));
		
		// Reloads week start and end date
		_weekStartDate = _cv.getCurrentWeekStartDate();
		_weekEndDate = _cv.getCurrentWeekEndDate();
		
		// Today background
		final double dayWidth = (getWidth() - X_OFFSET - _cv.getScrollWidth()) / DAYS;
		if (_weekStartDate.before(new Date()) && _weekEndDate.after(new Date())) {
			brush.setColor(Utils.transparentColor(Utils.COLOR_ACCENT, 0.05));
			final Calendar c = CalendarView.getCalendarInstance();
			c.setTime(new Date());
			final int i = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
			final int start = (int) ((i / DAYS) * (getWidth() - X_OFFSET - _cv.getScrollWidth()) + X_OFFSET);
			brush.fillRect(start, 0, (int) dayWidth, getHeight());
		}
		
		// Do the vertical lines
		brush.setColor(Utils.COLOR_LIGHT_BG);
		for (int i = 1; i < DAYS; i++) {
			final double x = (i / DAYS) * (getWidth() - X_OFFSET - _cv.getScrollWidth()) + X_OFFSET;
			brush.draw(new Line2D.Double(x, 0, x, getHeight()));
		}
		
		// Get all tasks and toss them into the task map
		_taskMap.clear();
		for (final ITimeBlockable block : _cv.getTimeBlocks()) {
			List<ITimeBlockable> list = _taskMap.get(block.getTask());
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(block);
			_taskMap.put(block.getTask(), list);
		}
		_y = (int) Y_PAD / 2;
		
		// Draw out all blocks
		final double height = getHeight() / (_taskMap.keySet().size() * 1.5);
		final int h = (int) Math.max(Math.min(MAX_BLOCK_HEIGHT, height), MIN_BLOCK_HEIGHT);
		for (final ITask task : _taskMap.keySet()) {
			final List<ITimeBlockable> list = _taskMap.get(task);
			for (final ITimeBlockable block : list) {
				placeAndDrawLine(brush, block, (h));
			}
			_y += h * 1.3;
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
		double xStart = ((day / DAYS) * (getWidth() - X_OFFSET - _cv.getScrollWidth())) + X_OFFSET;
		xStart += (time / HRS) * ((getWidth() - X_OFFSET) / DAYS);
		return (int) xStart;
	}
	
	/**
	 * Draws all lines from blocks in the calendar view
	 * 
	 * @param brush the graphics object
	 * @param t the block itself
	 * @param height the height of the block itself
	 * @return if it got added
	 */
	private boolean placeAndDrawLine(final Graphics2D brush, final ITimeBlockable t, final int height) {
		// Checks bounds so we know not to place line if dates don't match up
		if (t.getStart().after(_weekEndDate) || t.getEnd().before(_weekStartDate)) {
			return false;
		}
		
		// Start point - deals with before this week
		final Calendar c = CalendarView.getCalendarInstance();
		int startX;
		if (t.getStart().before(_weekStartDate)) {
			startX = X_OFFSET;
		} else {
			c.setTime(t.getStart());
			final double startTime = c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0);
			final int startDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
			startX = getXPos(startTime, startDay);
		}
		
		// End point - deals with after this week
		int endX;
		if (t.getEnd().after(_weekEndDate)) {
			endX = getWidth();
		} else {
			c.setTime(t.getEnd());
			final double endTime = c.get(Calendar.HOUR_OF_DAY) + (c.get(Calendar.MINUTE) / 60.0);
			final int endDay = (int) ((c.get(Calendar.DAY_OF_WEEK) - 1) % DAYS);
			endX = getXPos(endTime, endDay);
		}
		
		// At minimum, width must be 5 pixels - also set color and rect
		final int width = Math.max(endX - startX, 5);
		final Rectangle2D.Double rect = new Rectangle2D.Double(startX, _y, width, height);
		final boolean highlighted = _cv.getMovingBlock() != null && _cv.getMovingBlock().first.equals(t);
		final Color currColor = highlighted ? Utils.COLOR_ACCENT : CanvasUtils.getColor(t);
		
		// Draw block
		if (highlighted) {
			brush.setColor(Utils.COLOR_FOREGROUND);
			final float dash1[] = { 4.0f };
			final BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash1, 0);
			brush.setStroke(dashed);
		} else {
			brush.setColor(currColor);
			brush.fill(rect);
			brush.setColor(Utils.COLOR_LIGHT_BG);
			brush.setStroke(new BasicStroke(1));
		}
		brush.draw(rect);
		return true;
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
