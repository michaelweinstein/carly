package frontend;

import java.util.HashMap;
import java.util.Map;

import backend.StorageService;
import backend.Utilities;
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
		possibleFlags.put(Utils.GUI, null);
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

///////////// THIS BLOCK HAS ERRORS, IF YOU DON'T NEED THIS DELETE IT (WHOEVER'S THIS IS)
//		// Check the arguments for validity and create objects as needed
//		try {
//			parser.parse(args);
//		} catch (final IllegalArgumentException e) {
//			Utilities.printError(e.getMessage());
//			System.out.println(Utilities.USAGE);
//			return;
//		}
		
//		// Create an App, subtype dependent on the GUI command line flag
//		final boolean debug = parser.existsFlag(Utilities.DEBUG);
//		a = (parser.existsFlag(Utilities.GUI)) ? new GUIApp(debug) : new REPLApp(debug);
//		a.start();
////////////////^^^^^^^^^^^^^^^^^^^^^
		
		/*
		 * Eric: I've commented out this valid code block below so I can just test Storage Service 
		 * without having the GUI start. Feel free to uncomment it if you need it. 
		 */
///////// THIS BLOCK IS CURRENT VALID CODE; Please let it execute for GUI to run
//		// Check the arguments for validity and create objects as needed
//		try {
//			parser.parse(args);
//		} catch (final IllegalArgumentException e) {
//			Utils.printError(e.getMessage());
//			System.out.println(Utils.USAGE);
//			return;
//		}
//		
//		// Create an App, subtype dependent on the GUI command line flag
//		final boolean debug = parser.existsFlag(Utils.DEBUG);
//		a = (parser.existsFlag(Utils.GUI)) ? new GUIApp(debug) : new REPLApp(debug);
//		a.start();
//		
//		/* For StorageService.initialize(false||true):
//		 * Set 'true' if you want to drop old tables at the start of every run
//		 * Set 'false' for persistence */
		
		//DEBUG
		StorageService.initialize(true);
	}
}
