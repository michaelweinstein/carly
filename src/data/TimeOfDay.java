package data;

/**
 * Represents the time of day a Task can be completed or a TimeBlock can be scheduled
 * 
 * @author dgattey
 */
public enum TimeOfDay {
	MORNING(6, 12), AFTERNOON(12, 17), EVENING(17, 21), NIGHT(21, 6);
	
	public final double	start;
	public final double	end;
	
	/**
	 * Internal constructor
	 * 
	 * @param start the start time in 24 hour time
	 * @param end the end time in 24 hour time
	 */
	private TimeOfDay(final double start, final double end) {
		this.start = start;
		this.end = end;
	}
}
