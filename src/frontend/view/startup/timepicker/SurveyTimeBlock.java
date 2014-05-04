package frontend.view.startup.timepicker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.Date;

import data.Vec2d;

public class SurveyTimeBlock extends Rectangle2D.Double {
	
	private static final long	serialVersionUID	= -4648292002477697311L;
	
	/* Styling vals */
	private static final Color	unselectedColor		= Color.DARK_GRAY;
	private static final Color	selectedColor		= Color.ORANGE;
	private static final Color	borderColor			= Color.LIGHT_GRAY;
	// private static final Color dashedBorderColor = new Color(179, 179, 179);
	private static final float	border_width		= 1.55f;
	// private static float border_dash_length;
	private static BasicStroke	border_stroke;
	// private static BasicStroke border_stroke_dashed;
	
	private static final Color	hoverColor			= Color.LIGHT_GRAY.darker();
	
	/* Boolean vars */
	private boolean				_isSelected;
	private final boolean		_isOnHalfHour;
	private boolean				_isHovering;
	private boolean				_isFirstBlock		= false;
	
	/* Data vars */
	private final Vec2d			_loc;
	private final Vec2d			_dim;
	
	public SurveyTimeBlock(final double x, final double y, final boolean startsOnHalfHour, final boolean isFirstBlock) {
		super(x, y, SurveyWeekView.COL_WIDTH, SurveyWeekView.ROW_HEIGHT);
		_loc = new Vec2d(x, y);
		_dim = new Vec2d(SurveyWeekView.COL_WIDTH, SurveyWeekView.ROW_HEIGHT);
		
		_isSelected = false;
		_isOnHalfHour = startsOnHalfHour;
		_isHovering = false;
		_isFirstBlock = isFirstBlock;
		
		// Create borders
		border_stroke = new BasicStroke(border_width);
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
	
	// TODO: Comment
	public void draw(final Graphics2D g) {
		
		/* Paint border */
		
		g.setColor(borderColor);
		g.setStroke(border_stroke);
		// Clip borders for half-hour boxes
		if (_isOnHalfHour) {
			g.clipRect((int) (getX() - border_width), (int) (getY()), (int) (getWidth() + 2 * border_width),
					(int) (getHeight()));
		}
		
		// TODO: If _isFirstBlock, need to draw left and upper border
		
		g.draw(this);
		// Reset clip after borders were trimmed
		if (_isOnHalfHour) {
			g.setClip(null);
		}
		
		/* Paint fill */
		
		// Fill with color
		g.setColor(_isSelected ? selectedColor : _isHovering ? hoverColor : unselectedColor);
		
		// Hover over unselected block
		if (_isHovering && !_isSelected) {
			g.setColor(hoverColor);
		}
		
		g.fill(this);
		
		// /////
		// TODO: Try to paint lines over left, right borders
		/*
		 * if (_isOnHalfHour) { g.setColor(borderColor); g.setStroke(border_stroke); g.drawLine
		 * ((int)(getX()+getWidth()), (int)(getY()), (int)(getX()+getWidth()), (int)(getY()+getHeight())); }
		 */
	}
}
