package frontend.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import frontend.Utils;
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
	 * @param vc the view controller in control
	 */
	public MainFrame(final ViewController vc) {
		super(Utils.APP_NAME);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600, 550));
		setResizable(true);
		
		// Make child views and add them
		calendar = new CalendarView();
		toolbar = new ToolbarView(vc);
		assignmentsView = new AssignmentsView(vc);
		
		// Whole view
		final JPanel mainPanel = new JPanel();
		add(calendar, BorderLayout.CENTER);
		add(assignmentsView, BorderLayout.EAST);
		add(toolbar, BorderLayout.NORTH);
		Utils.padComponent(calendar, 10, 10);
		Utils.themeComponent(mainPanel);
		pack();
	}
	
	/**
	 * Loads data recursively
	 */
	public void reloadData() {
		calendar.reloadData();
		assignmentsView.reloadData();
	}
}
