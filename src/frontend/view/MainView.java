package frontend.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import frontend.Utils;

/**
 * Represents a JFrame class that is the frontend interface
 * 
 * @author dgattey
 */
public class MainView extends JFrame {
	
	// Child views
	private final CalendarView		calendar;
	private final AssignmentsView	assignmentsView;
	private final ToolbarView		toolbar;
	
	// Constants
	private static final long		serialVersionUID	= -5684060306972975687L;
	
	/**
	 * Constructor sets window properties and adds newly created views
	 */
	public MainView() {
		super(Utils.APP_NAME);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600, 550));
		setResizable(true);
		
		// Make child views and add them
		calendar = new CalendarView();
		toolbar = new ToolbarView();
		assignmentsView = new AssignmentsView();
		
		// Whole view
		final JPanel mainPanel = new JPanel();
		add(calendar, BorderLayout.CENTER);
		add(assignmentsView, BorderLayout.EAST);
		add(toolbar, BorderLayout.NORTH);
		Utils.padComponent(calendar, 10, 10);
		Utils.themeComponent(mainPanel);
	}
	
	public void showOverlay(final JPanel overlay) {
		
	}
}
