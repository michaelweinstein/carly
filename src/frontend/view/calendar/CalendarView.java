package frontend.view.calendar;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import backend.database.StorageService;
import data.ITimeBlockable;
import data.Tuple;
import frontend.Utils;
import frontend.app.GUIApp;
import frontend.view.CanvasUtils;

/**
 * Represents the panel holding the line and week views for the calendar
 * 
 * @author dgattey
 */
public class CalendarView extends JPanel {
	
	private static final long				serialVersionUID	= 1L;
	private final GUIApp					_app;
	private final LineCanvas				_lineCanvas;
	private final WeekView					_weekView;
	private final List<DayLabel>			_dayLabelList;
	private JLabel							_weekYearLabel;
	private final List<ITimeBlockable>		_timeBlocks;
	private final List<ITimeBlockable>		_unavailableBlocks;
	
	// Dragging
	private Tuple<ITimeBlockable, DragType>	_movingBlock;
	
	// Info for view
	private int								_currWeek;
	private int								_currYear;
	private long							_lastChanged;
	
	/**
	 * Enum for dragging blocks - Value can either be: <br>
	 * TOP (the top edge is being dragged)<br>
	 * BOTTOM (the bottom edge is)<br>
	 * FULL (the whole block is being moved)
	 * 
	 * @author dgattey
	 */
	public enum DragType {
		TOP, BOTTOM, FULL;
	}
	
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
	 * 
	 * @param app the app running this
	 */
	public CalendarView(final GUIApp app) {
		_app = app;
		_lineCanvas = new LineCanvas(this);
		_weekView = new WeekView(this);
		_timeBlocks = new ArrayList<>();
		_unavailableBlocks = new ArrayList<>();
		_dayLabelList = new ArrayList<>(7);
		
		final Calendar cal = getCalendarInstance();
		_currWeek = cal.get(Calendar.WEEK_OF_YEAR);
		_currYear = cal.get(Calendar.YEAR);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.themeComponentAlt(this);
		_lastChanged = System.currentTimeMillis();
		
		add(makeToolbar());
		add(makeDays());
		add(_lineCanvas);
		add(Box.createVerticalStrut(10));
		add(_weekView);
		
		// Key listeners to move weeks left and right
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "left");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "right");
		getActionMap().put("left", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				shiftWeekBackward();
			}
		});
		getActionMap().put("right", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				shiftWeekForward();
			}
		});
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
		_weekYearLabel.setFont(Utils.getFont(Font.BOLD, 22));
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
		String contents = String.format("%s %d (Week %d)", startM, _currYear, _currWeek);
		if (!startM.equals(endM)) {
			// Spanning a year as well as a month?
			if (startY != endY) {
				contents = String.format("%s %d-%s %d (Week %d)", startM, startY, endM, endY, _currWeek);
			} else {
				contents = String.format("%s-%s %d (Week %d)", startM, endM, _currYear, _currWeek);
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
		
		final Font ft = Utils.getFont(Font.BOLD, 12);
		
		// Add new labels for all days
		final String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		for (final String day : dayNames) {
			_dayLabelList.add(new DayLabel(day));
		}
		
		// Theme all labels
		int dayOfWeek = 1;
		for (final DayLabel d : _dayLabelList) {
			d.setDate(dayOfWeek);
			d.setFont(ft);
			Utils.themeComponentAlt(d);
			days.add(d);
			dayOfWeek++;
		}
		
		par.add(Box.createHorizontalStrut(CanvasUtils.X_OFFSET));
		par.add(days);
		
		return par;
	}
	
	/**
	 * Repaints children after resetting data
	 */
	public void reloadData() {
		_movingBlock = null;
		reloadDataWithHighlights();
	}
	
	/**
	 * Repaints children after resetting data, keeping the highlights
	 */
	public void reloadDataWithHighlights() {
		reloadTimeBlocksFromDB();
		for (int i = 0; i < _dayLabelList.size(); i++) {
			_dayLabelList.get(i).setDate(i + 1, getCurrentWeekStartDate());
		}
		updateWeekYearLabel();
		_lineCanvas.repaint();
		_weekView.repaint();
	}
	
	/**
	 * Reads in all time blocks from database
	 */
	private void reloadTimeBlocksFromDB() {
		final Date start = getCurrentWeekStartDate();
		final Date end = getCurrentWeekEndDate();
		_timeBlocks.clear();
		_unavailableBlocks.clear();
		_timeBlocks.addAll(StorageService.getAllAssignmentBlocksWithinRange(start, end));
		_unavailableBlocks.addAll(StorageService.getAllUnavailableBlocksWithinRange(start, end));
	}
	
	/**
	 * Deals with shifting weeks
	 */
	private void shiftHelp() {
		_lastChanged = System.currentTimeMillis();
		
		// Deals with week overflow
		if (_currWeek < 1) {
			_currWeek = 52;
			_currYear--;
		} else if (_currWeek > 52) {
			_currWeek = 1;
			_currYear++;
		}
		
		_weekView.getViewport().setViewPosition(new Point(0, 0));
	}
	
	/**
	 * Shifts the current week and full view forward by 1
	 */
	public void shiftWeekForward() {
		_currWeek++;
		shiftHelp();
		reloadData();
	}
	
	/**
	 * Shifts the current week and full view forward by 1 keeping the highlight
	 */
	public void shiftWeekForwardWithHighlights() {
		_currWeek++;
		shiftHelp();
		reloadDataWithHighlights();
	}
	
	/**
	 * Shifts the current week and full view backwards by 1 keeping the highlight
	 */
	public void shiftWeekBackward() {
		_currWeek--;
		shiftHelp();
		reloadData();
	}
	
	/**
	 * Shifts the current week and full view backwards by 1 keeping the highlight
	 */
	public void shiftWeekBackwardWithHighlights() {
		_currWeek--;
		shiftHelp();
		reloadDataWithHighlights();
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
	
	/**
	 * Gets the unavailable time blocks for use elsewhere
	 * 
	 * @return a list of ITimeBlockables to draw
	 */
	public List<ITimeBlockable> getUnavailableTimeBlocks() {
		return _unavailableBlocks;
	}
	
	/**
	 * @return the current block in transit
	 */
	public Tuple<ITimeBlockable, DragType> getMovingBlock() {
		return _movingBlock;
	}
	
	/**
	 * Sets a new block in transit
	 * 
	 * @param newBlock the new block
	 * @param drag the drag type
	 */
	public void setMovingBlock(final ITimeBlockable newBlock, final DragType drag) {
		_movingBlock = new Tuple<>(newBlock, drag);
	}
	
	/**
	 * Deletes current moving block
	 */
	public void clearMovingBlock() {
		_movingBlock = null;
	}
	
	/**
	 * Returns when the week last changed
	 * 
	 * @return a long representing system time milliseconds since last change
	 */
	public long getTimeChanged() {
		return _lastChanged;
	}
	
	/**
	 * Reloads full app
	 */
	public void reloadApp() {
		_app.reload();
	}
	
	/**
	 * Getter for the app
	 * 
	 * @return the GUIApp for this
	 */
	public GUIApp getApp() {
		return _app;
	}
}
