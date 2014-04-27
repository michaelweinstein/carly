package frontend.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.lang.reflect.Method;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import frontend.Utils;
import frontend.app.GUIApp;
import frontend.view.assignments.AssignmentsView;
import frontend.view.calendar.CalendarView;

/**
 * Represents a JFrame class that is the frontend interface
 * 
 * @author dgattey
 */
public class MainFrame extends JFrame {
	
	// Child views
	private final CalendarView		calendar;
	private final AssignmentsView	assignmentsView;
	private final ToolbarView		toolbar;
	
	// Constants
	private static final long		serialVersionUID	= -5684060306972975687L;
	
	/**
	 * Constructor sets window properties and adds newly created views
	 * 
	 * @param app the app controller in control
	 */
	public MainFrame(final GUIApp app) {
		super(Utils.APP_NAME);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(850, 500));
		setResizable(true);
		
		// Make child views and add them
		calendar = new CalendarView();
		toolbar = new ToolbarView(app);
		assignmentsView = new AssignmentsView(app);
		
		// Whole view
		final JPanel mainPanel = new JPanel();
		add(calendar, BorderLayout.CENTER);
		add(assignmentsView, BorderLayout.EAST);
		add(toolbar, BorderLayout.NORTH);
		Utils.padComponent(calendar, 10, 10);
		Utils.themeComponent(mainPanel);
		tryEnableFullScreenMode(this);
		pack();
		
		reloadData();
	}
	
	/**
	 * Loads data recursively
	 */
	public void reloadData() {
		calendar.reloadData();
		assignmentsView.reloadData();
	}
	
	/**
	 * For a given JFrame, tries to use Apple APIs to enable native fullscreen mode
	 * 
	 * @param window a JFrame window
	 */
	public static void tryEnableFullScreenMode(final Window window) {
		final String className = "com.apple.eawt.FullScreenUtilities";
		final String methodName = "setWindowCanFullScreen";
		
		try {
			final Class<?> clazz = Class.forName(className);
			final Method method = clazz.getMethod(methodName, new Class<?>[] { Window.class, boolean.class });
			method.invoke(null, window, true);
		} catch (final Throwable t) {} // We don't care if it didn't work (just means non-Apple)
	}
	
	/**
	 * Returns the calendar
	 * 
	 * @return the calendar associated with this view
	 */
	public CalendarView getCalendarView() {
		return calendar;
	}
}
