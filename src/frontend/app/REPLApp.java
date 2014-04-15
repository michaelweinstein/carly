package frontend.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import frontend.Utils;

/**
 * REPL class for the command line interface - app runner
 * 
 * @author dgattey
 */
public class REPLApp extends App {
	
	/**
	 * Uses the App constructor and starts a REPL
	 * 
	 * @param debug if we should be in debug mode
	 */
	public REPLApp(final boolean debug) {
		super(debug);
	}
	
	/**
	 * Starts the command line!
	 */
	@Override
	public void start() {
		final Scanner reader = new Scanner(System.in);
		System.out.println("Ready");
		while (reader.hasNextLine()) {
			final String line = reader.nextLine();
			try {
				switch (line) {
				
				// TODO: DO SOMETHING WITH USER INPUT
				
				default:
					System.out.println("Unimplemented");
					break;
				}
			} catch (final IllegalArgumentException e) {
				// CTRL-D
				if (e.getMessage().isEmpty()) {
					break;
				}
				
				// Other error, so just print and continue
				Utils.printError("<REPL> " + e.getMessage());
			}
		}
		reader.close();
	}
	
	/**
	 * Takes input and turns it into tokens. Splits on whitespace and checks that the length is four and nothing is
	 * empty
	 * 
	 * @param input the user input
	 * @return a list of tokens representing user input
	 */
	private static List<String> tokenize(final String input) {
		// Parse into string
		final List<String> tokens = splitWords(input.trim());
		
		if (tokens == null || tokens.isEmpty()) {
			throw new IllegalArgumentException("");
		}
		
		return tokens;
	}
	
	/**
	 * Splits words based off spaces and quotes (spaces in quotes won't trigger a split)
	 * 
	 * @param s the given string
	 * @return a list of tokens
	 */
	private static List<String> splitWords(final String s) {
		if (s.isEmpty()) {
			return new ArrayList<>();
		}
		
		return Arrays.asList(s.split("[ ]+(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
	}
}
