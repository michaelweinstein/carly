package backend.time;

/**
 * An exception that is thrown when an assignment cannot be successfully inserted into the current
 * time stream because a valid fit does not exist.
 * @author evanfuller
 *
 */
public class NotEnoughTimeException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotEnoughTimeException() {
		super();
	}
	
	public NotEnoughTimeException(String msg) {
		super(msg);
	}
	
}
