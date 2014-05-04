package data;

import frontend.Utils;

public class TemplateStep implements ITemplateStep {
	
	/* constructor vars */
	private final String	_name;
	private double			_percentOfTotal	= 0;
	private int				_stepNumber		= 0;
	
	private TimeOfDay		_timeOfDay;
	
	// Aprox number of days this step will take to complete
	private int				_numDays;
	// Aprox number of hours user per day user will work
	private double			_hoursPerDay;
	
	// Preferred time of day to work on this Step
	
	public TemplateStep(final String name, final double percentOfTemplate) {
		_name = name;
		_percentOfTotal = handlePercent(percentOfTemplate);
		setupVars();
	}
	
	public TemplateStep(final String name, final double percentOfTemplate, final int stepNumber) {
		_name = name;
		_percentOfTotal = handlePercent(percentOfTemplate);
		_stepNumber = stepNumber;
		setupVars();
	}
	
	/**
	 * Constructor used by StorageService to reconstruct step object
	 */
	public TemplateStep(final String name, final double percentTotal, final int stepNumber, final TimeOfDay tod) {
		_name = name;
		_percentOfTotal = handlePercent(percentTotal);
		_stepNumber = stepNumber;
		_timeOfDay = tod;
	}
	
	/* Private methods */
	
	/**
	 * Private method called to setup vars not passed into constructor. Uses DataUtil DEFAULT values. Can be changed by
	 * user or by learning algorithm in mutators. _timeOfDay, _numDays, _hoursPerDay
	 */
	private void setupVars() {
		_numDays = DataUtil.DEFAULT_STEP_DAYS;
		_hoursPerDay = DataUtil.DEFAULT_HOURS_PER_DAY;
		_timeOfDay = DataUtil.DEFAULT_TIME_OF_DAY;
	}
	
	/**
	 * Sets _percentOfTotal to percent passed into constructor, IF the representation is valid. Else _percentOfTotal = 0
	 * and message is printed.
	 * 
	 * @param percent between 0 and 1 (inclusive of 1)
	 * @return
	 */
	private static Double handlePercent(final double percent) {
		if (DataUtil.percentRepOK(percent)) {
			return percent;
		}
		Utils.printError("Percent representation invalid " + percent + ". "
			+ "Expecting format: 0 < percent <= 1  (TemplateStep.handlePercent)");
		return null;
	}
	
	/* ITemplateStep mutators */
	
	/**
	 * Step number must be positive integer. Otherwise var _stepNumber is not assigned, and message is printed as error.
	 */
	@Override
	public void setStepNumber(final int index) {
		if (index >= 0) {
			_stepNumber = index;
		} else {
			System.out.println("ERROR: Step number must " + // print line
				"be positive integer (TemplateStep.setStepNumber)");
		}
	}
	
	/* ITemplateStep accessors (commented in interface) */
	
	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public int getStepNumber() {
		return _stepNumber;
	}
	
	@Override
	public double getPercentOfTotal() {
		return _percentOfTotal;
	}
	
	@Override
	public TimeOfDay getBestTimeToWork() {
		return _timeOfDay;
	}
	
	@Override
	public int getNumberOfDays() {
		// TODO Auto-generated method stub
		return _numDays;
	}
	
	@Override
	public double getHoursPerDay() {
		// TODO Auto-generated method stub
		return _hoursPerDay;
	}
	
	/* Holy trinity */
	
	/**
	 * Format: "name, % of total, stepNumber" Percent in String returned as [1, 100]
	 */
	@Override
	public String toString() {
		return new String(_name + ", " + _percentOfTotal * 100 + ", " + _stepNumber);
	}
	
	@Override
	public String fullString() {
		return "Task: [" + _name + ", " + (_percentOfTotal * 100) + ", " + _stepNumber + ", " + _timeOfDay.name() + "]";
	}
}
