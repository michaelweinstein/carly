package frontend.app;

import frontend.view.ViewController;

/**
 * Creates a GUI interface for the Maps program
 * 
 * @author dgattey
 */
public class GUIApp extends App {
	
	private final ViewController	viewController;
	
	/**
	 * Uses the App constructor plus gui specific stuff
	 * 
	 * @param debug if we should be in debug mode
	 */
	public GUIApp(final boolean debug) {
		super(debug);
		viewController = new ViewController(this);
	}
	
	/**
	 * Method called from App - creates window, adds objects, links up the listeners for the field, and shows the window
	 */
	@Override
	public void start() {
		viewController.create();
	}
	
	/**
	 * Public getter for the view controller
	 * 
	 * @return the current view controller
	 */
	public ViewController getViewController() {
		return viewController;
	}
	
}
