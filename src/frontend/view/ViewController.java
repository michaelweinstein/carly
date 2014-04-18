package frontend.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import data.Assignment;
import frontend.app.GUIApp;

/**
 * Class that deals with drawing information to screen
 * 
 * @author dgattey
 */
public class ViewController {
	
	private final GUIApp			app;
	
	// View stuff
	private JFrame					window;
	
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
		window = new MainView();
		
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
	 * Redraws whole window
	 */
	public void redraw() {
		window.revalidate();
		window.repaint();
	}
}