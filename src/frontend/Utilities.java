package frontend;

import java.awt.Color;

/**
 * Constants class to package up strings used in multiple classes
 * 
 * @author dgattey
 */
public abstract class Utilities {
	
	public static final String	APPNAME				= "Carly";
	public static final Color	COLOR_BACKGROUND	= Color.DARK_GRAY;
	public static final String	GUI					= "gui";
	public static final String	DEBUG				= "debug";
	public static final String	USAGE				= "Usage: carly [--gui] [--debug]";
	
	/**
	 * Calculates minimum of many ints
	 * 
	 * @param ints a list of ints to find min of
	 * @return the smallest of a variable number of ints
	 */
	public static int min(final int... ints) {
		int curr = Integer.MAX_VALUE;
		for (final int i : ints) {
			curr = Math.min(i, curr);
		}
		return curr;
	}
	
	/**
	 * Calculates maximum of many ints
	 * 
	 * @param ints a list of ints to find min of
	 * @return the smallest of a variable number of ints
	 */
	public static int max(final int... ints) {
		int curr = Integer.MIN_VALUE;
		for (final int i : ints) {
			curr = Math.max(i, curr);
		}
		return curr;
	}
	
	/**
	 * Prints an error to syserr
	 * 
	 * @param msg the details
	 */
	public static void printError(final String msg) {
		System.err.println("ERROR: " + msg);
	}
}
