package hub;

import java.util.Date;
import java.util.List;

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
				
				try {
					// Make sure the template and assignment are in the DB
					if (StorageService.getTemplate(tempId) == null) {
						StorageService.addTemplate(a.getTemplate());
					}
					// Learner requires that the template be in the db
					Learner.optimizeTasks(a);
					StorageService.addAssignment(a);
					
					// Allocate time and then merge time blocks in DB
					final Date start = new Date();
					final TimeAllocator talloc = new TimeAllocator(a);
					talloc.insertAsgn(start, a.getDueDate());
					StorageService.mergeAllTimeBlocks(talloc.getEntireBlockSet());
				} catch (final NotEnoughTimeException | StorageServiceException err) {
					
					// Not enough time to add or storage error, so present the error dialog after removing from DB
					StorageService.removeAssignment(a);
					showError(err, "Close");
				}
				
				// Everything went well, so reload the app data
				reloadApp();
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
		
		// Time modifier updates old block with newStart and newEnd so by the time the learner acts on it, it's "new"
		if (TimeModifier.updateBlock(oldBlock, newStart, newEnd)) {
			Learner.considerBlockUpdate(oldBlock, oldStart, oldEnd);
		}
		reloadApp();
	}
	
	/**
	 * Updates a task in the database with a new completion amount
	 * 
	 * @param oldTask the actual task to update
	 * @param newCompletion a double between 0 and 1 inclusive to represent percent complete
	 */
	public static void changeTask(final ITask oldTask, final double newCompletion) {
		final double magnitudeChange = newCompletion - oldTask.getPercentComplete();
		TimeModifier.updateBlocksInTask(oldTask, newCompletion);
		Learner.considerTaskUpdate(oldTask, new Date(System.currentTimeMillis()), magnitudeChange);
		
		oldTask.setPercentComplete(newCompletion);
		StorageService.updateTask(oldTask);
		reloadApp();
	}
	
	/**
	 * For unavailable blocks instead of regular ones
	 * 
	 * @param startDate the start of the list of blocks
	 * @param endDate the end of the list of blocks
	 * @param blockList the actual blocks to save
	 */
	public static void replaceUnavailableBlocks(final Date startDate, final Date endDate,
			final List<ITimeBlockable> blockList) {
		for (final ITimeBlockable t : blockList) {
			t.renewID();
		}
		StorageService.replaceUnavailableBlocks(startDate, endDate, blockList);
		reloadApp();
	}
	
	/**
	 * Convenience method to reload the app
	 */
	private static void reloadApp() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				_app.reload();
			};
		});
	}
	
	/**
	 * Convenience method to show an error
	 * 
	 * @param err the exception to use
	 * @param buttonText the text of the button
	 */
	private static void showError(final Exception err, final String buttonText) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				_app.presentErrorDialog(err, buttonText);
			}
		});
	}
}
