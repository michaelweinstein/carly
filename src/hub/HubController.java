package hub;

import java.util.Date;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import backend.time.TimeAllocator;
import data.Assignment;
import frontend.Utils;

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
	
	public static void addAssignmentToCalendar(final Assignment a) {
		final String tempId = a.getTemplate().getID();
		
		// Insert template into db if not already there
		if (StorageService.getTemplate(tempId) == null) {
			try {
				StorageService.addTemplate(a.getTemplate());
			} catch (final StorageServiceException sse) {
				Utils.printError("SSE in addAssignmentToCalendar() - inserting ITemplate");
			}
		}
		
		// Insert assignment into db
		try {
			StorageService.addAssignment(a);
		} catch (final StorageServiceException sse) {
			Utils.printError("SSE in addAssignmentToCalendar() - inserting Assignment");
		}
		
		final Date start = new Date();
		final TimeAllocator talloc = new TimeAllocator(a);
		talloc.insertAsgn(start, a.getDueDate());
		
		StorageService.mergeAllTimeBlocks(talloc.getEntireBlockSet());
	}
	
}
