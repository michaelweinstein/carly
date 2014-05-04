package frontend.view.startup.timepicker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.Date;

import data.Vec2d;
import frontend.Utils;

public class SurveyTimeBlock extends Rectangle2D.Double {
	
	private static final long		serialVersionUID	= -4648292002477697311L;
	
	/* Styling vals */
	public static final Color		SELECTED_COLOR		= Utils.COLOR_ACCENT;
	public static final Color		BORDER_COLOR		= Color.LIGHT_GRAY;
	public static final BasicStroke	BORDER_STROKE		= new BasicStroke(1f);
	public static final Color		HOVER_COLOR			= Color.LIGHT_GRAY.darker();
	
	/* Boolean vars */
	private boolean					_isSelected;
	private final boolean			_isOnHalfHour;
	private boolean					_isHovering;
	
	/* Data vars */
	private final Vec2d				_loc;
	private final Vec2d				_dim;
	
	public SurveyTimeBlock(final double x, final double y, final boolean startsOnHalfHour) {
		super(x, y, SurveyWeekView.COL_WIDTH, SurveyWeekView.ROW_HEIGHT);
		_loc = new Vec2d(x, y);
		_dim = new Vec2d(SurveyWeekView.COL_WIDTH, SurveyWeekView.ROW_HEIGHT);
		
		_isSelected = false;
		_isOnHalfHour = startsOnHalfHour;
		_isHovering = false;
	}
	
	/* Data access methods */
	
	/**
	 * Returns two <code>Date</code> objects in an array, start and end time of this block, respectively. <br>
	 * End time is always a half hour after Start time. <br>
	 * All <code>Date</code> instances are the week starting at the epoch, at 0.0 milliseconds, since
	 * <code>SurveyTimeBlock</code> represents a block of time in a generic week.
	 * 
	 * @return Date[] where [0] = start time [1] = end time
	 */
	public Date[] getRange() {
		// Create Calendar starting at first Sunday after epoch (1/1/1970)
		final Calendar c = Calendar.getInstance();
		c.setTime(new Date(0));
		c.add(Calendar.DAY_OF_YEAR, 3); // move to January 4, 1970

		// Set start time in minutes
		final Date[] startAndEnd = new Date[2];
		final int day = (int) (_loc.x / _dim.x);
		final int timeStart = (int) (_loc.y / _dim.y) * 30; // half hour number --> minutes
		
		// Start time
		c.add(Calendar.DAY_OF_YEAR, day);
		c.add(Calendar.MINUTE, timeStart);
		startAndEnd[0] = c.getTime();
		
		// End time (start time + 30 minutes)
		c.add(Calendar.MINUTE, 30);
		startAndEnd[1] = c.getTime();
		
		return startAndEnd;
	}
	
	// TODO
	public void hover(final boolean h) {
		_isHovering = h;
	}
	
	/* Selection methods */
	
	/**
	 * @return whether or not this block is currently selected by user
	 */
	public boolean isSelected() {
		return _isSelected;
	}
	
	/**
	 * Mutator sets whether this block is _isSelected by user or not. Determines color of block in draw().
	 * 
	 * @param sel if selected
	 */
	public void setSelected(final boolean sel) {
		_isSelected = sel;
	}
	
	/**
	 * Toggles _isSelected boolean. <br>
	 * Called when user clicks or drags on this block.
	 */
	public void toggleSelected() {
		_isSelected = _isSelected ? false : true;
	}
	
	/* Drawing methods */
	
	/**
	 * Draws the given block - only fills if hovering or selected, and otherwise just draws the border (top and left
	 * only because the rest is dealt with by the other blocks). If on half an hour, doesn't draw the top
	 * 
	 * @param g the graphics object to use in drawing
	 */
	public void draw(final Graphics2D g) {
		
		// Draws fill for selected or hovering block
		if (_isSelected || _isHovering) {
			g.setColor(_isHovering ? HOVER_COLOR : SELECTED_COLOR);
			g.fill(this);
		}
		
		// Draws line for left, only drawing top if not on half hour
		g.setColor(BORDER_COLOR);
		g.setStroke(BORDER_STROKE);
		g.draw(new Line2D.Double(getX(), getMinY(), getX(), getMaxY()));
		if (!_isOnHalfHour) {
			g.draw(new Line2D.Double(getMinX(), getMinY(), getMaxX(), getMinY()));
		}
	}
}
