package data;


public class TemplateStep implements ITemplateStep {
	/* constructor vars */
	private String _name;
	private double _percentOfTotal = 0;
	private int _stepNumber = 0;
	
	// Aprox number of days this step will take to complete
	private int _numDays;
	// Aprox number of hours user per day user will work
	private double _hoursPerDay;
	// Preferred time of day to work on this Step
	private TimeOfDay _timeOfDay;	
	
	public TemplateStep(String name, double percentOfTemplate) {
		_name = name;
		handlePercent(percentOfTemplate);	
		setupVars();
	}
	public TemplateStep(String name, double percentOfTemplate, int stepNumber) {
		_name = name;
		handlePercent(percentOfTemplate);
		_stepNumber = stepNumber;		
		setupVars();
	}
	public TemplateStep(String name, double percentOfTemplate, int stepNumber, 
			int numDays, double hoursPerDay) {
		_name = name;
		handlePercent(percentOfTemplate);
		_stepNumber = stepNumber;
		_numDays = numDays;
		_hoursPerDay = hoursPerDay;
		_timeOfDay = DataUtil.DEFAULT_TIME_OF_DAY;
	}
	
	/* Private methods */
	
	/**
	 * Private method called to setup vars not 
	 * passed into constructor. Uses DataUtil
	 * DEFAULT values. Can be changed by user
	 * or by learning algorithm in mutators.
	 * _timeOfDay, _numDays, _hoursPerDay
	 */
	private void setupVars() {
		_numDays = DataUtil.DEFAULT_STEP_DAYS;
		_hoursPerDay = DataUtil.DEFAULT_HOURS_PER_DAY;
		_timeOfDay = DataUtil.DEFAULT_TIME_OF_DAY;
	}
	
	/**
	 * Sets _percentOfTotal to percent passed
	 * into constructor, IF the representation
	 * is valid. Else _percentOfTotal = 0 and
	 * message is printed.
	 * 
	 * @param 0 < percent <= 1
	 */
	private void handlePercent(double percent) {
		if (DataUtil.percentRepOK(percent)) {
			_percentOfTotal = percent;
		} 
		else {
			System.out.println("ERROR: Percent representation invalid " + percent + ". " + 
					"Expecting format: 0 < percent <= 1  (TemplateStep.handlePercent)");
		}
	}
	
	/* ITemplateStep mutators */
	
	/**
	 * Step number must be positive integer.
	 * Otherwise var _stepNumber is not assigned, 
	 * and message is printed as error.
	 */
	@Override
	public void setStepNumber(int index) {
		if (index >= 0) {
			_stepNumber = index;
		}
		else {
			System.out.println("ERROR: Step number must " + 			//print line
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
}