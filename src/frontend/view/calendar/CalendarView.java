package frontend.view.calendar;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import frontend.Utils;

/**
 * Represents the panel holding the line and week views for the calendar
 * 
 * @author dgattey
 */
public class CalendarView extends JPanel {
	
	private final LineView			lineView;
	private final WeekView			weekView;
	private final List<DayLabel>	dayLabelList;
	
	private static final long		serialVersionUID	= -1015403696639767751L;
	
	/**
	 * Getter for a calendar instance with all the correct properties set
	 * 
	 * @return a new Calendar object with correct properties
	 */
	public static Calendar getCalendarInstance() {
		final Calendar c = Calendar.getInstance();
		c.setFirstDayOfWeek(Calendar.SUNDAY);
		return c;
	}
	
	/**
	 * Creates a line and week view and themes things appropriately
	 */
	public CalendarView() {
		lineView = new LineView(this);
		weekView = new WeekView(this);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.themeComponentAlt(this);
		
		dayLabelList = new ArrayList<>(7);
		add(makeDays());
		add(lineView);
		add(Box.createVerticalStrut(10));
		add(weekView);
	}
	
	/**
	 * Makes the days label panel
	 * 
	 * @return a new panel with the labels for days
	 */
	private JPanel makeDays() {
		final JPanel par = new JPanel();
		final JPanel days = new JPanel();
		days.setLayout(new GridLayout(1, 7));
		par.setLayout(new BoxLayout(par, BoxLayout.X_AXIS));
		Utils.themeComponentAlt(days);
		Utils.themeComponentAlt(par);
		Utils.padComponent(par, 0, 10);
		
		final Font ft = new Font(Utils.APP_FONT_NAME, Font.PLAIN, 13);
		par.add(Box.createHorizontalStrut(WeekCanvas.X_OFFSET + 4));
		
		dayLabelList.add(new DayLabel("Sun"));
		dayLabelList.add(new DayLabel("Mon"));
		dayLabelList.add(new DayLabel("Tues"));
		dayLabelList.add(new DayLabel("Wed"));
		dayLabelList.add(new DayLabel("Thurs"));
		dayLabelList.add(new DayLabel("Fri"));
		dayLabelList.add(new DayLabel("Sat"));
		
		// Theme all labels
		int dayOfWeek = 1;
		for (final DayLabel d : dayLabelList) {
			d.setDate(dayOfWeek);
			d.setFont(ft);
			Utils.themeComponent(d);
			days.add(d);
			dayOfWeek++;
		}
		
		par.add(days);
		
		return par;
	}
	
	/**
	 * Does nothing currently
	 */
	public void reloadData() {}
	
}
