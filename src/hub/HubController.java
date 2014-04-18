package hub;

import data.Assignment;

/**
 * Controller that deals with handoff between frontend and backend
 * 
 * @author dgattey
 */
public class HubController {
	
	/**
	 * Method to be called with a valid assignment (though that'll be checked) to pass the assignment to the learner
	 * 
	 * @param a an IAssignment to add to the database
	 */
	public static void passAssignmentToLearner(final Assignment a) {
		System.out.println("Added " + a.fullString());
	}
}
