package frontend.view.calendar;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import data.AssignmentBlock;
import data.ITimeBlockable;
import data.Task;
import frontend.Utils;

/**
 * Represents the panel holding the line and week views for the calendar
 * 
 * @author dgattey
 */
public class CalendarView extends JPanel {
	
	private final LineCanvas			lineCanvas;
	private final WeekView				weekView;
	private final List<DayLabel>		dayLabelList;
	private final List<ITimeBlockable>	timeBlocks;
	
	private static final long			serialVersionUID	= -1015403696639767751L;
	
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
		lineCanvas = new LineCanvas(this);
		weekView = new WeekView(this);
		timeBlocks = new ArrayList<>();
		dayLabelList = new ArrayList<>(7);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.themeComponentAlt(this);
		
		add(makeDays());
		add(lineCanvas);
		add(Box.createVerticalStrut(10));
		add(weekView);
		
		// TESTING TESTING 1 2 3
		final Calendar c = getCalendarInstance();
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		c.set(Calendar.HOUR_OF_DAY, 3);
		c.set(Calendar.MINUTE, 30);
		Date start = c.getTime();
		c.add(Calendar.MINUTE, 30);
		Date end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Early Morning Task - Wait, $#($32", 0.3)));
		
		c.set(Calendar.HOUR_OF_DAY, 20);
		c.set(Calendar.MINUTE, 15);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 12);
		end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Overnight 1", 0.3)));
		
		c.add(Calendar.HOUR_OF_DAY, 2);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 74);
		end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Overnight Full Days", 0.3)));
		
		c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		c.set(Calendar.HOUR_OF_DAY, 11);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 2);
		c.add(Calendar.MINUTE, 30);
		end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Sunday Brunch", 0.3)));
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Double Booked!!?", 0.3)));
		
		c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 12);
		end = c.getTime();
		timeBlocks
				.add(new AssignmentBlock(start, end, new Task("Half Day Saturday Event with a Very Long Title", 0.3)));
		
		c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		c.set(Calendar.HOUR_OF_DAY, 10);
		c.set(Calendar.MINUTE, 47);
		start = c.getTime();
		c.add(Calendar.MINUTE, 80000);
		end = c.getTime();
		timeBlocks.add(new AssignmentBlock(start, end, new Task("Around the Weekend", 0.3)));
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
		
		final Font ft = new Font(Utils.APP_FONT_NAME, Font.BOLD, 11);
		par.add(Box.createHorizontalStrut(CanvasConstants.X_OFFSET + 5));
		
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
			Utils.themeComponentAlt(d);
			days.add(d);
			dayOfWeek++;
		}
		
		par.add(days);
		
		return par;
	}
	
	/**
	 * Returns the current week of the year
	 * 
	 * @return "Week\n##" where ## is week of year
	 */
	public int getWeek() {
		final Calendar c = getCalendarInstance();
		c.setTime(new Date());
		return c.get(Calendar.WEEK_OF_YEAR);
	}
	
	/**
	 * Does nothing currently
	 */
	public void reloadData() {}
	
	/**
	 * Gets current scrollbar width
	 * 
	 * @return returns the scrollbar width
	 */
	public int getScrollWidth() {
		return weekView.getVerticalScrollBar().getWidth();
	}
	
	/**
	 * Gets the time blocks for use elsewhere
	 * 
	 * @return a list of ITimeBlockables to draw
	 */
	public List<ITimeBlockable> getTimeBlocks() {
		return timeBlocks;
	}
}
