package frontend.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import frontend.Utilities;

/**
 * Represents a JFrame class that is the frontend interface
 * 
 * @author dgattey
 */
public class MainView extends JFrame {
	
	// Child views
	private final LineView		lineView;
	private final TaskView		taskView;
	private final WeekView		weekView;
	private final ToolbarView	toolbar;
	
	// Constants
	private static final long	serialVersionUID	= -5684060306972975687L;
	
	/**
	 * Constructor sets window properties and adds newly created views
	 */
	public MainView() {
		super(Utilities.APP_NAME);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600, 400));
		setResizable(false);
		
		// Make child views and add them
		lineView = new LineView();
		taskView = new TaskView();
		weekView = new WeekView();
		toolbar = new ToolbarView();
		
		// Center of the view
		final JPanel center = new JPanel();
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(lineView);
		center.add(Box.createVerticalStrut(10));
		center.add(weekView);
		Utilities.themeComponent(center);
		Utilities.padComponent(center, 10, 10);
		
		// Whole view
		final JPanel mainPanel = new JPanel();
		add(center, BorderLayout.CENTER);
		add(taskView, BorderLayout.EAST);
		add(toolbar, BorderLayout.NORTH);
		Utilities.themeComponent(mainPanel);
	}
}
