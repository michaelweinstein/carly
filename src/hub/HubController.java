package hub;

import java.util.Date;

import javax.swing.SwingUtilities;

import backend.Learner;
import backend.database.StorageService;
import backend.database.StorageServiceException;
import backend.time.NotEnoughTimeException;
import backend.time.TimeAllocator;
import backend.time.TimeModifier;
import data.Assignment;
import data.ITask;
import data.ITimeBlockable;
import frontend.Utils;
import frontend.app.GUIApp;

/**
 * Controller that deals with handoff between frontend and backend
 * 
 * @author dgattey
 */
public class HubController {
	
	private static GUIApp	_app;
	
	/**
	 * Constructor for the controller
	 * 
	 * @param app the app to use
	 */
	public static void initialize(final GUIApp app) {
		_app = app;
	}
	
	/**
	 * Adds the assignment to database, updating the learner and such along the way
	 * 
	 * @param a the assignment to add
	 */
	public static void addAssignmentToCalendar(final Assignment a) {
		new Thread() {
			
			@Override
			public void run() {
				final String tempId = a.getTemplate().getID();
				
				// TODO: Make learner act on this
				
				// Insert template into db if not already there
				if (StorageService.getTemplate(tempId) == null) {
					try {
						StorageService.addTemplate(a.getTemplate());
					} catch (final StorageServiceException sse) {
						Utils.printError("SSE in addAssignmentToCalendar() - inserting ITemplate");
						return;
					}
				}
				
				// Insert assignment into db
				try {
					StorageService.addAssignment(a);
				} catch (final StorageServiceException sse) {
					Utils.printError("SSE in addAssignmentToCalendar() - inserting Assignment");
					return;
				}
				
				final Date start = new Date();
				final TimeAllocator talloc = new TimeAllocator(a);
				try {
					talloc.insertAsgn(start, a.getDueDate());
				} catch (final NotEnoughTimeException net) {
					Utils.printError(net.getMessage());
				}
				
				StorageService.mergeAllTimeBlocks(talloc.getEntireBlockSet());
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						_app.reload();
					};
				});
			}
		}.start();
	}
	
	/**
	 * Updates a block in the database when the user moves it or changes the start or end times
	 * 
	 * @param oldBlock the old block, with old data as start/end
	 * @param newStart the new start time
	 * @param newEnd the new end time
	 */
	public static void changeTimeBlock(final ITimeBlockable oldBlock, final Date newStart, final Date newEnd) {
		final Date oldStart = new Date(oldBlock.getStart().getTime());
		final Date oldEnd = new Date(oldBlock.getEnd().getTime());
		
		if (TimeModifier.updateBlock(oldBlock, newStart, newEnd)) {
			Learner.considerBlockUpdate(oldBlock, newStart, newEnd);
		}
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				_app.reload();
			};
		});
	}
	
	/**
	 * Updates a task in the database with a new completion amount
	 * 
	 * @param oldTask the actual task to update
	 * @param newCompletion a double between 0 and 1 inclusive to represent percent complete
	 */
	public static void changeTask(final ITask oldTask, final double newCompletion) {
		final double oldCompletion = oldTask.getPercentComplete();
		TimeModifier.updateBlocksInTask(oldTask, newCompletion);
		// TODO: Update learner using old percent and new percent
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				_app.reload();
			};
		});
	}
}
