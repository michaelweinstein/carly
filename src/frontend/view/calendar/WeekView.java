package frontend.view.calendar;

import javax.swing.JScrollPane;

import frontend.Utils;
import frontend.view.CScrollBarUI;

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
		_canvas = new WeekCanvas(cv, this);
		setViewportView(_canvas);
		getVerticalScrollBar().setUI(new CScrollBarUI(Utils.COLOR_ALTERNATE));
	}
}