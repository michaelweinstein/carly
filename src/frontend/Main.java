package frontend;

import java.util.HashMap;
import java.util.Map;

import backend.database.StorageService;
import frontend.app.App;
import frontend.app.GUIApp;
import frontend.app.REPLApp;

/**
 * Main class - runs the entire program
 * 
 * @author dgattey
 */
public class Main {
	
	private static App	a;
	
	/**
	 * Sets up a new parser for the command line args - creates a map from each possible command line argument to its
	 * corresponding class type for object instantiation, and tells the parser it expects 0 filenames
	 * 
	 * @return a new command line parser, ready to parse
	 */
	public static ArgParser createFlagParser() {
		final Map<String, Class<?>> possibleFlags = new HashMap<>();
		possibleFlags.put(Utils.REPL, null);
		possibleFlags.put(Utils.DEBUG, null);
		return new ArgParser(possibleFlags, 0);
	}
	
	/**
	 * Main method - parses command line arguments and starts REPL or GUI
	 * 
	 * @param args command line args for the CLP to use to generate tokens
	 */
	public static void main(final String[] args) {
		final ArgParser parser = createFlagParser();
		
		// Check the arguments for validity and create objects as needed
		try {
			parser.parse(args);
		} catch (final IllegalArgumentException e) {
			Utils.printError(e.getMessage());
			System.out.println(Utils.USAGE);
			return;
		}
		
		/**
		 * For StorageService.initialize(false||true): <br>
		 * Set 'true' if you want to drop old tables at the start of every run <br>
		 * Set 'false' for persistence
		 */
		
		// DEBUG
		boolean runStartUp = StorageService.initialize(false);
		
		// Create an App, subtype dependent on the GUI command line flag
		final boolean debug = parser.existsFlag(Utils.DEBUG);
		a = (parser.existsFlag(Utils.REPL)) ? new REPLApp(debug) : new GUIApp(debug, runStartUp);
		a.start();
	}
}
