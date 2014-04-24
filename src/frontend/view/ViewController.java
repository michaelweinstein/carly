package frontend.view;

import java.util.ArrayList;
import java.util.List;

import data.Assignment;
import frontend.app.GUIApp;

/**
 * Class that deals with drawing information to screen
 * 
 * @author dgattey
 */
public class ViewController {
	
	private final GUIApp			app;
	private MainFrame				window;
	
	private final List<Assignment>	allAssignments	= new ArrayList<>();	// FOR TESTING ONLY!
																			
	/**
	 * Constructor with an app for use later
	 * 
	 * @param app the parent app
	 */
	public ViewController(final GUIApp app) {
		this.app = app;
	}
	
	/**
	 * Makes a new MainView, that itself sets up data
	 */
	public void create() {
		window = new MainFrame(this);
		
		// Show it
		window.pack();
		window.setVisible(true);
	}
	
	/**
	 * STRICTLY FOR TESTING
	 * 
	 * @param a the assignment to add
	 */
	public void addAssignment(final Assignment a) {
		allAssignments.add(a);
	}
	
	/**
	 * STRICTLY FOR TESTING
	 * 
	 * @return the list of assignments
	 */
	public List<Assignment> getAssignments() {
		return allAssignments;
	}
	
	/**
	 * Redraws whole window
	 */
	public void redraw() {
		window.reloadData();
		window.revalidate();
		window.repaint();
	}
}