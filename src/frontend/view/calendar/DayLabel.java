package frontend.view.calendar;

import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;

import frontend.Utils;

/**
 * Represents a label for a day (Mon 3/5 for example)
 * 
 * @author dgattey
 */
public class DayLabel extends JLabel {
	
	private static final long	serialVersionUID	= 1L;
	
	/**
	 * Sets the day of this label, no date
	 * 
	 * @param dayName the day of the week ("Mon" or "Tues")
	 */
	public DayLabel(final String dayName) {
		super(dayName);
	}
	
	/**
	 * Sets the text label to include the given date, assuming current week
	 * 
	 * @param dayOfWeek an int for the current week
	 */
	public void setDate(final int dayOfWeek) {
		setDate(dayOfWeek, new Date());
	}
	
	/**
	 * Sets the text label to include the given date and day of week
	 * 
	 * @param dayOfWeek an int for the current day
	 * @param weekStart the date that starts the week
	 */
	public void setDate(final int dayOfWeek, final Date weekStart) {
		final Calendar c = CalendarView.getCalendarInstance();
		c.setTime(weekStart);
		c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		setTextForDate(c);
	}
	
	/**
	 * Actually sets the text of the label given a calendar with the correct date
	 * 
	 * @param c the calendar
	 */
	private void setTextForDate(final Calendar c) {
		final int day = c.get(Calendar.DAY_OF_MONTH);
		final int month = c.get(Calendar.MONTH);
		final String dayN = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, getLocale());
		setText(dayN + " " + (month + 1) + "/" + day);
		final int year = c.get(Calendar.YEAR);
		
		c.setTime(new Date());
		final int toDay = c.get(Calendar.DAY_OF_MONTH);
		final int toMonth = c.get(Calendar.MONTH);
		final int toYear = c.get(Calendar.YEAR);
		if (day == toDay && month == toMonth && year == toYear) {
			setForeground(Utils.COLOR_ACCENT);
		} else {
			Utils.themeComponent(this);
		}
	}
}
