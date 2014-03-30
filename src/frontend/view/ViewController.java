package frontend.view;

import javax.swing.JFrame;

import frontend.app.GUIApp;

/**
 * Class that deals with drawing information to screen
 * 
 * @author dgattey
 */
public class ViewController {
	
	private final GUIApp	app;
	
	// View stuff
	private JFrame			window;
	
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
}