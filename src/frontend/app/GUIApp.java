package frontend.app;

import hub.HubController;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import backend.Learner;
import backend.database.StorageService;
import backend.database.StorageServiceException;
import frontend.view.MainFrame;
import frontend.view.calendar.CalendarView;
import frontend.view.settings.SettingsConstants;
import frontend.view.startup.SurveyView;

/**
 * Creates a GUI interface for the Maps program
 * 
 * @author dgattey
 */
public class GUIApp extends App {
	
	private MainFrame			_window;
	private boolean				_runStartUp;
	private final ErrorDialog	_error;
	private Set<String>			_shownErrors;
	
	/**
	 * Uses the App constructor plus gui specific stuff
	 * 
	 * @param debug if we should be in debug mode
	 */
	public GUIApp(final boolean debug) {
		super(debug);
		HubController.initialize(GUIApp.this);
		_error = new ErrorDialog();
		try {
			_runStartUp = StorageService.initialize(false);
			_window = new MainFrame(this);
			_shownErrors = new HashSet<>();
		} catch (final StorageServiceException e) {
			_error.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			presentErrorDialog(e, "Quit");
		}
	}
	
	/**
	 * Method called from App - creates window, adds objects, links up the listeners for the field, and shows the window
	 */
	@Override
	public void start() {
		if (_runStartUp) {
			runSurvey();
		} else {
			showMain();
			// If no start-up survey, enables Learner to previously stored setting
			String learnerStr = StorageService.getSetting(SettingsConstants.LEARNER_SETTING);
			Learner.setEnabled(Boolean.parseBoolean(learnerStr));
		}
	}
	
	/**
	 * Once the statup survey has been completed, actually shows the main
	 */
	private void showMain() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				reload();
				if (_window != null) {
					_window.pack();
					_window.setVisible(true);
				}
			}
		});
	}
	
	/**
	 * Reloads and redraws whole window
	 */
	public void reload() {
		if (_window != null) {
			_window.reloadData();
			_window.revalidate();
			_window.repaint();
		}
	}
	
	/**
	 * Returns the calendar view of the frame
	 * 
	 * @return the view associated with the calendar in the frame
	 */
	public CalendarView getCalendarView() {
		return _window.getCalendarView();
	}
	
	/**
	 * Called to run start-up survey <code>SurveyView</code>. <br>
	 * Contains a <code>while</code> loop to halt execution until user finishes start-up survey. Creates new
	 * <code>SurveyView</code> and sets it to visible immediately, then adds a <code>WindowListener</code> to trigger
	 * the <code>_isFillingOutSurvey</code> boolean when the user closes the window, which occurs on submit.
	 */
	private void runSurvey() {
		
		// Open survey
		final SurveyView survey = new SurveyView();
		survey.addWindowListener(new WindowListener() {
			
			@Override
			public void windowClosed(final WindowEvent e) {
				showMain();
			}
			
			@Override
			public void windowOpened(final WindowEvent e) {}
			
			@Override
			public void windowClosing(final WindowEvent e) {}
			
			@Override
			public void windowIconified(final WindowEvent e) {}
			
			@Override
			public void windowDeiconified(final WindowEvent e) {}
			
			@Override
			public void windowActivated(final WindowEvent e) {}
			
			@Override
			public void windowDeactivated(final WindowEvent e) {}
		});
		survey.setVisible(true);
	}
	
	/**
	 * Shows a one-time error dialog
	 * 
	 * @param id the unique ID to identify this message
	 * @param message the actual message
	 */
	public void presentOneTimeErrorDialog(final String id, final String message) {
		if (!_shownErrors.contains(id)) {
			_shownErrors.add(id);
			presentErrorDialog(new IllegalArgumentException(message), "Close");
		}
	}
	
	/**
	 * Shows a dialog when there's an error - present here rather than somewhere else because it may be application
	 * specific and thus can't be shown from the MainFrame
	 * 
	 * @param e the exception that should be shown to the user
	 * @param buttonText the text to toss on the button
	 */
	public void presentErrorDialog(final Exception e, final String buttonText) {
		_error.setTitle("Error");
		_error.setText(e.getMessage() == null ? "Something went wrong" : e.getMessage());
		_error.setButtonText(buttonText);
		_error.setLocationRelativeTo(_window);
		_error.pack();
		_error.setVisible(true);
	}
	
}
