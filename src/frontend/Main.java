package frontend;

import java.util.HashMap;
import java.util.Map;

import backend.database.StorageService;
import frontend.app.App;
import frontend.app.GUIApp;

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
		
		// Create an App
		final boolean runStartUp = StorageService.initialize(false);
		final boolean debug = parser.existsFlag(Utils.DEBUG);

		a = new GUIApp(debug, runStartUp);
		a.start();
	}
}
