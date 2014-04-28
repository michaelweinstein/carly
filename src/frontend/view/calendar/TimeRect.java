package frontend.view.calendar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import data.ITimeBlockable;
import frontend.Utils;
import frontend.view.DrawingConstants;

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
		_c = DrawingConstants.getColor(_t);
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
			return new Color(120, 120, 120);
		} else if (_canvas.getHighlightedBlocks().contains(_t)) {
			return new Color(_c.getRed(), _c.getGreen(), _c.getBlue(), 100);
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
		return _canvas.getHighlightedBlocks().contains(_t) ? Utils.COLOR_FOREGROUND : Utils.COLOR_LIGHT_BG;
	}
	
	/**
	 * Returns the actual stroke
	 * 
	 * @return the stroke to set around the block
	 */
	public Stroke getStroke() {
		if (_canvas.getHighlightedBlocks().contains(_t)) {
			final float dash1[] = { 4.0f };
			return new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash1, 0);
		}
		return new BasicStroke(1);
	}
}