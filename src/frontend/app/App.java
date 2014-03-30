package frontend.app;

/**
 * Abstract class to implement an App object, used for running the GUI or REPL (command line interface)
 * 
 * @author dgattey
 */
public abstract class App {
	
	protected final boolean	debug;
	
	/**
	 * Sets debug flag for later
	 * 
	 * @param debug if we're in debug mode
	 */
	public App(final boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Starts the user interface or evaluation of input, based off the class
	 */
	public abstract void start();
	
}
