package frontend.app;

import hub.HubController;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.SwingUtilities;

import frontend.view.MainFrame;
import frontend.view.calendar.CalendarView;
import frontend.view.startup.SurveyView;

/**
 * Creates a GUI interface for the Maps program
 * 
 * @author dgattey
 */
public class GUIApp extends App {
	
	private final MainFrame		_window;
	private final boolean		_runStartUp;
	private final ErrorDialog	_error;
	
	/**
	 * Uses the App constructor plus gui specific stuff
	 * 
	 * @param debug if we should be in debug mode
	 * @param runStartUp if the startup survey should be run
	 */
	public GUIApp(final boolean debug, final boolean runStartUp) {
		super(debug);
		_runStartUp = runStartUp;
		_window = new MainFrame(this);
		_error = new ErrorDialog();
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
		}
	}
	
	/**
	 * Once the statup survey has been completed, actually shows the main
	 */
	private void showMain() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				HubController.initialize(GUIApp.this);
				reload();
				_window.pack();
				_window.setVisible(true);
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
	 * Shows a dialog when there's an error - present here rather than somewhere else because it may be application
	 * specific and thus can't be shown from the MainFrame
	 * 
	 * @param e the exception that should be shown to the user
	 */
	public void presentErrorDialog(final Exception e) {
		_error.setTitle("Error");
		_error.setText(e.getMessage() == null ? "Something went wrong" : e.getMessage());
		_error.setVisible(true);
	}
}
