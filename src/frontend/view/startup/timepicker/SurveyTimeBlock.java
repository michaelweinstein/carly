package frontend.view.startup.timepicker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class SurveyTimeBlock extends Rectangle2D.Double {
	
	private static final long serialVersionUID = -4648292002477697311L;
	
	/* Styling vals */
	private static final Color unselectedColor = Color.DARK_GRAY;
	private static final Color selectedColor = Color.ORANGE;
	private static final Color borderColor = Color.LIGHT_GRAY;
	private static final Color dashedBorderColor = new Color(179, 179, 179);
	private static final float border_width = 1.55f;
	private static float border_dash_length;
	private static BasicStroke border_stroke;
	private static BasicStroke border_stroke_dashed;
	
	/* Boolean vars */
	private boolean _selected;
	private boolean _onHalfHour;

	public SurveyTimeBlock(double x, double y, boolean startsOnHalfHour) {
		super(x, y, SurveyWeekView.COL_WIDTH, SurveyWeekView.ROW_HEIGHT);
		
		_selected = false;
		_onHalfHour = startsOnHalfHour;
		
		// Create borders
		border_dash_length = (float) SurveyWeekView.ROW_HEIGHT;	
		border_stroke = new BasicStroke(border_width);
		final float dashes[] = {border_dash_length, 4.0f};
		border_stroke_dashed = new BasicStroke(	border_width, 	
												BasicStroke.CAP_BUTT, 
												BasicStroke.JOIN_MITER, border_dash_length, 
												dashes, 0.0f);
	}
	
	/* Selection methods */
	
	/**
	 * @return whether or not this block is currently selected by user
	 */
	public boolean isSelected() {
		return _selected;
	}
	
	/** 
	 * Mutator sets whether this block is _selected by user
	 * or not. Determines color of block in draw().
	 */
	public void setSelected(boolean sel) {
		_selected = sel;
	}
	
	/**
	 * Toggles _selected boolean. <br>
	 * Called when user clicks or drags on this block.
	 */
	public void toggleSelected() {
		_selected = _selected? false: true;
	}
	
	/* Drawing methods */
	
	// TODO: Comment
	public void draw(Graphics2D g) {	
		// Paint border
/// 	TODO: On half hour, draw dashed lines or no border?
//		g.setColor(_onHalfHour? dashedBorderColor : borderColor);
//		g.setStroke(_onHalfHour? border_stroke_dashed : border_stroke);
		
		g.setColor(borderColor);
		g.setStroke(border_stroke);
		
/////	// TODO Try to clip rect to get rid of top, bottom borders
		if (_onHalfHour) {
			g.clipRect((int)(getX()-border_width), (int)(getY()), (int)(getWidth()+border_width), (int)(getHeight()));
		}
		
		g.draw(this);
		
///		// TODO Delete if clip never set
		if (_onHalfHour) g.setClip(null);
		
		// Fill with color
		g.setColor(_selected? selectedColor : unselectedColor);
		g.fill(this);
		
///////
		// TODO: Try to paint lines over left, right borders
/*		if (_onHalfHour) {
			g.setColor(borderColor);
			g.setStroke(border_stroke);
			g.drawLine	((int)(getX()+getWidth()), (int)(getY()), 
						(int)(getX()+getWidth()), (int)(getY()+getHeight()));	
		}*/
	}
}
