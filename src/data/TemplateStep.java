package data;


public class TemplateStep implements ITemplateStep {
	/* constructor vars */
	private String _name;
	private double _percentOfTotal = 0;
	private int _stepNumber = 0;
	
	private TimeOfDay _timeOfDay;	
	
////////// We are getting rid of these prpoerties of TemplateStep
	// Aprox number of days this step will take to complete
	private int _numDays;
	// Aprox number of hours user per day user will work
	private double _hoursPerDay;
	// Preferred time of day to work on this Step
////////////^^^^^^
	
	public TemplateStep(String name, double percentOfTemplate) {
		_name = name;
		_percentOfTotal = handlePercent(percentOfTemplate);	
		setupVars();
	}
	public TemplateStep(String name, double percentOfTemplate, int stepNumber) {
		_name = name;
		_percentOfTotal = handlePercent(percentOfTemplate);
		_stepNumber = stepNumber;		
		setupVars();
	}
	
/////// TODO: To be deleted -- delete this constructor
	/**
	 * TO BE DELETED!
	 * Switch to a constructor without numDays (int) and hoursPerDay (double)
	 */
	@Deprecated
	public TemplateStep(String name, double percentOfTemplate, int stepNumber, 
			int numDays, double hoursPerDay) {
		_name = name;
		_percentOfTotal = handlePercent(percentOfTemplate);
		_stepNumber = stepNumber;
		_numDays = numDays;
		_hoursPerDay = hoursPerDay;
		_timeOfDay = DataUtil.DEFAULT_TIME_OF_DAY;
	}

	/**
	 * Constructor used by StorageService to reconstruct step object
	 */
	public TemplateStep(String name, double percentTotal, int stepNumber, TimeOfDay tod) {
		_name = name;
		_percentOfTotal = handlePercent(percentTotal);
		_stepNumber = stepNumber;
		_timeOfDay = tod;
	}
	
//////// TODO: To be deleted -- old constructor used by storage service
	/**
	 * TO BE DELETED!
	 * Switch to a constructor without numDays (int) and hoursPerDay (double).
	 */
	@Deprecated
	public TemplateStep(String name, double percentTotal, int stepNumber, int numDays, 
			double hoursPerDay, TimeOfDay timeOfDay) {
		_name = name; 
		_percentOfTotal = handlePercent(percentTotal);
		_stepNumber = stepNumber; 
		_numDays = numDays; 
		_hoursPerDay = hoursPerDay; 
		_timeOfDay = timeOfDay; 
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
	private Double handlePercent(double percent) {
		if (DataUtil.percentRepOK(percent)) {
			return percent;
		} 
		else {
			System.out.println("ERROR: Percent representation invalid " + percent + ". " + 
					"Expecting format: 0 < percent <= 1  (TemplateStep.handlePercent)");
			return null;
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
	 * Format: "name, % of total, stepNumber"
	 * Percent in String returned as [1, 100]
	 */
	@Override
	public String toString() {
		return new String(_name + ", " + _percentOfTotal*100 + ", " + _stepNumber);
	}
	@Override
	public String fullString() {
		return "Task: [" + _name + ", " + (_percentOfTotal*100) + ", " + _stepNumber + 
				", " + _timeOfDay.name() + "]";
	}
}
