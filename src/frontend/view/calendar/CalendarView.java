package frontend.view.calendar;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
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
	
	private static final long			serialVersionUID	= 1L;
	private final LineCanvas			_lineCanvas;
	private final WeekView				_weekView;
	private final List<DayLabel>		_dayLabelList;
	private JLabel						_weekYearLabel;
	private final List<ITimeBlockable>	_timeBlocks;
	
	// Info for view
	private int							_currWeek;
	private int							_currYear;
	
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
		_lineCanvas = new LineCanvas(this);
		_weekView = new WeekView(this);
		_timeBlocks = new ArrayList<>();
		_dayLabelList = new ArrayList<>(7);
		
		final Calendar cal = getCalendarInstance();
		_currWeek = cal.get(Calendar.WEEK_OF_YEAR);
		_currYear = cal.get(Calendar.YEAR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.themeComponentAlt(this);
		
		add(makeToolbar());
		add(makeDays());
		add(_lineCanvas);
		add(Box.createVerticalStrut(10));
		add(_weekView);
		
		// TESTING TESTING 1 2 3
		final Calendar c = getCalendarInstance();
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		c.set(Calendar.HOUR_OF_DAY, 3);
		c.set(Calendar.MINUTE, 30);
		Date start = c.getTime();
		c.add(Calendar.MINUTE, 30);
		Date end = c.getTime();
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Early Morning Task - Wait, $#($32", 0.3)));
		
		c.set(Calendar.HOUR_OF_DAY, 20);
		c.set(Calendar.MINUTE, 15);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 12);
		end = c.getTime();
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Overnight 1", 0.3)));
		
		c.add(Calendar.HOUR_OF_DAY, 2);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 74);
		end = c.getTime();
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Overnight Full Days", 0.3)));
		
		c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		c.set(Calendar.HOUR_OF_DAY, 11);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 2);
		c.add(Calendar.MINUTE, 30);
		end = c.getTime();
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Sunday Brunch", 0.3)));
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Double Booked!!?", 0.3)));
		
		c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 12);
		end = c.getTime();
		_timeBlocks
				.add(new AssignmentBlock(start, end, new Task("Half Day Saturday Event with a Very Long Title", 0.3)));
		
		c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		c.set(Calendar.HOUR_OF_DAY, 10);
		c.set(Calendar.MINUTE, 47);
		start = c.getTime();
		c.add(Calendar.MINUTE, 80000);
		end = c.getTime();
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Around the Weekend", 0.3)));
		
		c.setTime(new Date());
		c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		c.set(Calendar.HOUR_OF_DAY, 20);
		c.set(Calendar.MINUTE, 17);
		c.add(Calendar.WEEK_OF_YEAR, -1);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 8);
		end = c.getTime();
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Around the Weekend 2", 0.3)));
		
		c.setTime(new Date());
		c.add(Calendar.WEEK_OF_YEAR, -4);
		start = c.getTime();
		c.add(Calendar.HOUR_OF_DAY, 8);
		end = c.getTime();
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Shouldn't appear before", 0.3)));
		
		c.setTime(new Date());
		c.add(Calendar.WEEK_OF_YEAR, 4);
		start = c.getTime();
		c.add(Calendar.YEAR, 2000);
		end = c.getTime();
		_timeBlocks.add(new AssignmentBlock(start, end, new Task("Shouldn't appear after", 0.3)));
		
		/*
		 * TheWorst-DoesFULLWEEKc.setTime(new Date()); c.add(Calendar.YEAR, -1); start = c.getTime();
		 * c.add(Calendar.YEAR, 2); end = c.getTime(); _timeBlocks.add(new AssignmentBlock(start, end, new
		 * Task("FULL WEEK", 0.3)));
		 */
		
	}
	
	/**
	 * Makes the toolbar panel with the buttons and information
	 * 
	 * @return the toolbar for changing weeks right above the title
	 */
	private JPanel makeToolbar() {
		final JPanel tools = new JPanel();
		tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));
		_weekYearLabel = new JLabel();
		_weekYearLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 22));
		updateWeekYearLabel();
		Utils.themeComponentAlt(tools);
		Utils.themeComponent(_weekYearLabel);
		_weekYearLabel.setForeground(Utils.COLOR_FOREGROUND);
		final ArrowButton left = new ArrowButton(ArrowButton.Direction.LEFT_BUTTON) {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void mouseClicked(final MouseEvent e) {
				shiftWeekBackward();
			}
			
		};
		final ArrowButton right = new ArrowButton(ArrowButton.Direction.RIGHT_BUTTON) {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void mouseClicked(final MouseEvent e) {
				shiftWeekForward();
			}
		};
		tools.add(Box.createHorizontalStrut(10));
		tools.add(left);
		tools.add(Box.createHorizontalGlue());
		tools.add(_weekYearLabel);
		tools.add(Box.createHorizontalGlue());
		tools.add(right);
		tools.add(Box.createHorizontalStrut(10));
		Utils.padComponent(tools, 5, 0, 15, 0);
		return tools;
	}
	
	/**
	 * Updates the week year label with new values read in dynamically
	 */
	private void updateWeekYearLabel() {
		final Calendar cal = getCalendarInstance();
		cal.setTime(getCurrentWeekStartDate());
		final String startM = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, getDefaultLocale());
		final int startY = cal.get(Calendar.YEAR);
		cal.setTime(getCurrentWeekEndDate());
		final String endM = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, getDefaultLocale());
		final int endY = cal.get(Calendar.YEAR);
		
		// Set the format string for a normal week
		String contents = String.format("Week %d (%s %d)", _currWeek, startM, _currYear);
		if (!startM.equals(endM)) {
			// Spanning a year as well as a month?
			if (startY != endY) {
				contents = String.format("Week %d (%s %d-%s %d)", _currWeek, startM, startY, endM, endY);
			} else {
				contents = String.format("Week %d (%s-%s %d)", _currWeek, startM, endM, _currYear);
			}
		}
		_weekYearLabel.setText(contents);
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
		
		final Font ft = new Font(Utils.APP_FONT_NAME, Font.BOLD, 12);
		
		_dayLabelList.add(new DayLabel("Sun"));
		_dayLabelList.add(new DayLabel("Mon"));
		_dayLabelList.add(new DayLabel("Tues"));
		_dayLabelList.add(new DayLabel("Wed"));
		_dayLabelList.add(new DayLabel("Thurs"));
		_dayLabelList.add(new DayLabel("Fri"));
		_dayLabelList.add(new DayLabel("Sat"));
		
		// Theme all labels
		int dayOfWeek = 1;
		for (final DayLabel d : _dayLabelList) {
			d.setDate(dayOfWeek);
			d.setFont(ft);
			Utils.themeComponentAlt(d);
			days.add(d);
			dayOfWeek++;
		}
		
		par.add(Box.createHorizontalStrut(CanvasConstants.X_OFFSET));
		par.add(days);
		
		return par;
	}
	
	/**
	 * Repaints children
	 */
	public void reloadData() {
		_weekView.repaint();
		_lineCanvas.repaint();
		for (int i = 0; i < _dayLabelList.size(); i++) {
			_dayLabelList.get(i).setDate(i + 1, getCurrentWeekStartDate());
		}
		updateWeekYearLabel();
	}
	
	/**
	 * Shifts the current week and full view forward by 1
	 */
	public void shiftWeekForward() {
		_currWeek++;
		
		// Deals with week overflow
		if (_currWeek > 52) {
			_currWeek = 1;
			_currYear++;
		}
		reloadData();
	}
	
	/**
	 * Shifts the current week and full view backwards by 1
	 */
	public void shiftWeekBackward() {
		_currWeek--;
		
		// Deals with week overflow
		if (_currWeek < 1) {
			_currWeek = 52;
			_currYear--;
		}
		reloadData();
	}
	
	/**
	 * Simply resets everything to today!
	 */
	public void shiftWeekToToday() {
		final Calendar cal = getCalendarInstance();
		_currWeek = cal.get(Calendar.WEEK_OF_YEAR);
		_currYear = cal.get(Calendar.YEAR);
		reloadData();
	}
	
	/**
	 * Returns the current week of the year
	 * 
	 * @return an int representing the current week of the year
	 */
	public int getCurrentWeek() {
		return _currWeek;
	}
	
	/**
	 * Returns the current year
	 * 
	 * @return an int representing the current year
	 */
	public int getCurrentYear() {
		return _currYear;
	}
	
	/**
	 * Clears a calendar instance and sets default values with current week and year preserved
	 * 
	 * @return a calendar with current week and year
	 */
	private Calendar getCorrectClearedCal() {
		final Calendar c = getCalendarInstance();
		c.clear();
		c.set(Calendar.YEAR, _currYear);
		c.set(Calendar.WEEK_OF_YEAR, _currWeek);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.DAY_OF_WEEK, c.getMinimum(Calendar.DAY_OF_WEEK));
		return c;
	}
	
	/**
	 * Gives the current week's start date
	 * 
	 * @return a new date representing the current week's start
	 */
	public Date getCurrentWeekStartDate() {
		final Calendar c = getCorrectClearedCal();
		return c.getTime();
	}
	
	/**
	 * Gives the current week's end date
	 * 
	 * @return a new date representing the current week's end
	 */
	public Date getCurrentWeekEndDate() {
		final Calendar c = getCorrectClearedCal();
		c.add(Calendar.WEEK_OF_YEAR, 1);
		c.add(Calendar.MILLISECOND, -1);
		return c.getTime();
	}
	
	/**
	 * Gets current scrollbar width
	 * 
	 * @return returns the scrollbar width
	 */
	public int getScrollWidth() {
		return _weekView.getVerticalScrollBar().getWidth();
	}
	
	/**
	 * Gets the time blocks for use elsewhere
	 * 
	 * @return a list of ITimeBlockables to draw
	 */
	public List<ITimeBlockable> getTimeBlocks() {
		return _timeBlocks;
	}
}
