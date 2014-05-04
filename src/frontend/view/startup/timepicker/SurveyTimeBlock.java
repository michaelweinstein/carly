package frontend.view.startup.timepicker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.Date;

import data.Vec2d;

public class SurveyTimeBlock extends Rectangle2D.Double {
	
	private static final long serialVersionUID = -4648292002477697311L;

	/* Styling vals */
	private static final Color unselectedColor = Color.DARK_GRAY;
	private static final Color selectedColor = Color.ORANGE;
	private static final Color borderColor = Color.LIGHT_GRAY;
//	private static final Color dashedBorderColor = new Color(179, 179, 179);
	private static final float border_width = 1.55f;
//	private static float border_dash_length;
	private static BasicStroke border_stroke;
//	private static BasicStroke border_stroke_dashed;
	
	/* Boolean vars */
	private boolean _isSelected;
	private boolean _isOnHalfHour;
	private boolean _isFirstBlock;
	
	/* Data vars */
	private Vec2d _loc;
	private Vec2d _dim;

	public SurveyTimeBlock(double x, double y, boolean startsOnHalfHour, boolean isFirstBlock) {
		super(x, y, SurveyWeekView.COL_WIDTH, SurveyWeekView.ROW_HEIGHT);
		_loc = new Vec2d(x, y);
		_dim = new Vec2d(SurveyWeekView.COL_WIDTH, SurveyWeekView.ROW_HEIGHT);
		
		_isSelected = false;
		_isOnHalfHour = startsOnHalfHour;
		_isFirstBlock = isFirstBlock;
				
		// Create borders
		border_stroke = new BasicStroke(border_width);
//		border_dash_length = (float) SurveyWeekView.ROW_HEIGHT;	
/*		final float dashes[] = {border_dash_length, 4.0f};
		border_stroke_dashed = new BasicStroke(	border_width, 	
												BasicStroke.CAP_BUTT, 
												BasicStroke.JOIN_MITER, border_dash_length, 
												dashes, 0.0f);*/
	}
	
	/* Data access methods */
	
	/**
	 * Returns two <code>Date</code> objects in an 
	 * array, start and end time of this block, respectively. <br>
	 * End time is always a half hour after Start time. <br>
	 * All <code>Date</code> instances are the week starting at
	 * the epoch, at 0.0 milliseconds, since <code>SurveyTimeBlock</code>
	 * represents a block of time in a generic week.
	 * 
	 * @return Date[] where [0] = start time [1] = end time
	 */
	public Date[] getRange() {
		// Create Calendar starting at first Sunday after epoch (1/1/1970)
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(0));
		c.add(Calendar.DAY_OF_YEAR, 3);	// move to January 4, 1970

		// Set start time in minutes
		Date[] startAndEnd = new Date[2];
		int day = (int) (_loc.x / _dim.x);
		int timeStart = (int) (_loc.y / _dim.y) * 30;	// half hour number --> minutes
		
		// Start time
		c.add(Calendar.DAY_OF_YEAR, day);
		c.add(Calendar.MINUTE, timeStart);
		startAndEnd[0] = c.getTime();
		
		// End time (start time + 30 minutes)
		c.add(Calendar.MINUTE, 30);
		startAndEnd[1] = c.getTime();
				
		return startAndEnd;
	}
	
	/* Selection methods */
	
	/**
	 * @return whether or not this block is currently selected by user
	 */
	public boolean isSelected() {
		return _isSelected;
	}
	
	/** 
	 * Mutator sets whether this block is _isSelected by user
	 * or not. Determines color of block in draw().
	 */
	public void setSelected(boolean sel) {
		_isSelected = sel;
	}
	
	/**
	 * Toggles _isSelected boolean. <br>
	 * Called when user clicks or drags on this block.
	 */
	public void toggleSelected() {
		_isSelected = _isSelected? false: true;
	}
	
	/* Drawing methods */
	
	// TODO: Comment
	public void draw(Graphics2D g) {	
		// Paint border
/// 	TODO: On half hour, draw dashed lines or no border?
//		g.setColor(_isOnHalfHour? dashedBorderColor : borderColor);
//		g.setStroke(_isOnHalfHour? border_stroke_dashed : border_stroke);
		
		g.setColor(borderColor);
		g.setStroke(border_stroke);
		
		if (_isOnHalfHour) 
			g.clipRect((int)(getX()-border_width), (int)(getY()), (int)(getWidth()+border_width), (int)(getHeight()));
		
		
		// TODO: If _isFirstBlock, need to draw left and upper border
		
		g.draw(this);
		
		if (_isOnHalfHour) 
			g.setClip(null);
		
		// Fill with color
		g.setColor(_isSelected? selectedColor : unselectedColor);
		g.fill(this);
		
///////
		// TODO: Try to paint lines over left, right borders
/*		if (_isOnHalfHour) {
			g.setColor(borderColor);
			g.setStroke(border_stroke);
			g.drawLine	((int)(getX()+getWidth()), (int)(getY()), 
						(int)(getX()+getWidth()), (int)(getY()+getHeight()));	
		}*/
	}
}
