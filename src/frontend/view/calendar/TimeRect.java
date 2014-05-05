package frontend.view.calendar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import data.ITimeBlockable;
import data.Tuple;
import frontend.Utils;
import frontend.view.CanvasUtils;
import frontend.view.calendar.CalendarView.DragType;

/**
 * Class of rectangle to use only for drawing - associates with others of same time automatically
 * 
 * @author dgattey
 */
public class TimeRect extends Rectangle2D.Double {
	
	private static final long		serialVersionUID	= 1L;
	
	private final Color				_c;
	private final WeekCanvas		_canvas;
	private final ITimeBlockable	_t;
	
	/**
	 * Creates a new rectangle with the given size and associates t with this rect
	 * 
	 * @param x x pos
	 * @param y y pos
	 * @param w width of rect
	 * @param h height of rect
	 * @param t the TimeBlockable to associate with this
	 * @param c the WeekCanvas running this
	 */
	public TimeRect(final double x, final double y, final double w, final double h, final ITimeBlockable t,
			final WeekCanvas c) {
		super(x, y, w, h);
		_t = t;
		_c = CanvasUtils.getColor(_t);
		_canvas = c;
		
		// Adds to all blocks for use later
		List<TimeRect> currBlocks = _canvas.getAllBlocks().get(t);
		if (currBlocks == null) {
			currBlocks = new ArrayList<>();
		}
		currBlocks.add(this);
		_canvas.getAllBlocks().put(t, currBlocks);
	}
	
	/**
	 * Returns the color of this block
	 * 
	 * @return the expected color
	 */
	public Color getColor() {
		if (!_t.isMovable()) {
			return new Color(60, 60, 60);
		} else if (equalsMovingBlock()) {
			return new Color(_c.getRed(), _c.getGreen(), _c.getBlue(), 100);
		} else if (_t.getEnd().before(new Date())) {
			final int r = Math.max(_c.getRed() - 30, 20);
			final int g = Math.max(_c.getGreen() - 30, 20);
			final int b = Math.max(_c.getBlue() - 30, 20);
			return new Color(r, g, b, 150);
		}
		return _c;
	}
	
	/**
	 * Returns the color of the stroke
	 * 
	 * @return the stroke color
	 */
	public Color getStrokeColor() {
		if (!_t.isMovable()) {
			return Utils.COLOR_BACKGROUND;
		}
		return equalsMovingBlock() ? Utils.COLOR_FOREGROUND : Utils.COLOR_FOREGROUND.darker();
	}
	
	/**
	 * Returns the actual stroke
	 * 
	 * @return the stroke to set around the block
	 */
	public Stroke getStroke() {
		if (equalsMovingBlock()) {
			final float dash1[] = { 4.0f };
			return new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash1, 0);
		}
		return new BasicStroke(1);
	}
	
	/**
	 * Returns the ITimeBlockable this rect represents
	 * 
	 * @return the reference to the ITimeBlockable of this TimeRect
	 */
	public ITimeBlockable getBlockable() {
		return _t;
	}
	
	/**
	 * Checks if this block is the currently moving one
	 * 
	 * @return if this is currently moving
	 */
	private boolean equalsMovingBlock() {
		final Tuple<ITimeBlockable, DragType> move = _canvas.getCalendarView().getMovingBlock();
		return move != null && move.first.equals(_t);
	}
}