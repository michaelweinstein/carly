package frontend.view.calendar;

import static frontend.view.calendar.CanvasConstants.DAYS;
import static frontend.view.calendar.CanvasConstants.HRS;
import static frontend.view.calendar.CanvasConstants.X_OFFSET;
import static frontend.view.calendar.CanvasConstants.Y_PAD;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.Date;
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
	
	private final CalendarView	_cv;
	private Date				_weekStartDate;
	private Date				_weekEndDate;
	private int					_y;
	private static final long	serialVersionUID	= 8788849553807412908L;
	
	/**
	 * Creates a canvas object
	 * 
	 * @param cv the calendar view object
	 */
	public LineCanvas(final CalendarView cv) {
		_cv = cv;
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
		brush.setColor(Utils.COLOR_ALTERNATE);
		brush.fill(new Rectangle2D.Double(0, 0, CanvasConstants.X_OFFSET, getHeight()));
		
		// Do the vertical lines
		brush.setColor(Utils.COLOR_LIGHT_BG);
		for (int i = 1; i < DAYS; i++) {
			final double x = (i / DAYS) * (getWidth() - X_OFFSET - _cv.getScrollWidth()) + X_OFFSET;
			brush.draw(new Line2D.Double(x, 0, x, getHeight()));
		}
		
		// Reloads week start and end date
		_weekStartDate = _cv.getCurrentWeekStartDate();
		_weekEndDate = _cv.getCurrentWeekEndDate();
		
		// Draws all lines for the tasks, but first checks for validity
		final List<ITimeBlockable> timeBlocks = _cv.getTimeBlocks();
		int size = timeBlocks.size();
		for (final ITimeBlockable t : timeBlocks) {
			if (t.getStart().after(_weekEndDate) || t.getEnd().before(_weekStartDate)) {
				--size;
			}
		}
		_y = (int) (Y_PAD / 2.0);
		final int height = Math.min((int) ((getHeight() - Y_PAD) / (size + 1)), 18);
		final int space = (int) ((getHeight() - Y_PAD) / size);
		for (final ITimeBlockable t : timeBlocks) {
			if (placeAndDrawLine(brush, t, height)) {
				_y += space;
			}
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
		final Color currColor = CanvasConstants.getColor(t);
		
		// Draw block
		brush.setColor(currColor);
		brush.fill(rect);
		brush.setColor(Utils.COLOR_LIGHT_BG);
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
