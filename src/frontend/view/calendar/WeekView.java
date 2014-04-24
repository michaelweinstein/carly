package frontend.view.calendar;

import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import frontend.Utils;

/**
 * Represents a week in the calendar with events throughout the day, every day
 * 
 * @author dgattey
 */
public class WeekView extends JScrollPane {
	
	private static final long	serialVersionUID	= -4485951680510823881L;
	private final WeekCanvas	_canvas;
	
	/**
	 * Creates things
	 * 
	 * @param cv the calendar view that contains this
	 */
	public WeekView(final CalendarView cv) {
		_canvas = new WeekCanvas(cv);
		setViewportView(_canvas);
		
		themeAll();
	}
	
	/**
	 * Themes everything in scope
	 */
	private void themeAll() {
		
		Utils.themeComponent(this);
		Utils.themeComponent(getViewport());
		Utils.themeComponent(_canvas);
		
		setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		Utils.padComponent(this, 0, 0);
		Utils.padComponent(_canvas, 0, 0);
	}
}
