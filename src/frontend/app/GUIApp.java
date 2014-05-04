package frontend.app;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import hub.HubController;

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
	
	private final MainFrame	_window;
	
	/**
	 * Instance variable set in SurveyView's WindowListener's
	 * onClose method, to indicate to the <code>while</code> loop
	 * that the user has submitted the survey, and execution can continue.
	 */
	private boolean _isFillingOutSurvey;
//	private boolean _runSurvey;
	
	/**
	 * Uses the App constructor plus gui specific stuff
	 * 
	 * @param debug if we should be in debug mode
	 * @param runStartUp if the startup survey should be run
	 */
	public GUIApp(final boolean debug, final boolean runStartUp) {
		super(debug);
		_window = new MainFrame(this);
		
//		_runSurvey = runStartUp;
		
		// TODO: Re-comment runSurvey block if we want to lock execution until survey is submitted
		if (runStartUp) {
			runSurvey();
		}
	}
	
	/**
	 * Method called from App - creates window, adds objects, links up the listeners for the field, and shows the window
	 */
	@Override
	public void start() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				HubController.initialize(GUIApp.this);
				_window.pack();
				_window.setVisible(true);
				
				// TODO: Either run this block or runSurvey in constructor
				// Called after MainFrame so it appears on top
/*				if (_runSurvey) {
					SurveyView survey = new SurveyView();
					survey.setVisible(true);
				}*/
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
	 * Contains a <code>while</code> loop to halt execution until
	 * user finishes start-up survey. Creates new <code>SurveyView</code>
	 * and sets it to visible immediately, then adds a <code>WindowListener</code>
	 * to trigger the <code>_isFillingOutSurvey</code> boolean when
	 * the user closes the window, which occurs on submit.
	 */
	private void runSurvey() {
		// Open Start-up Survey
		SurveyView survey = new SurveyView();
		survey.setVisible(true);
		// Trigger when user submits or closes window
		_isFillingOutSurvey = true;
		survey.addWindowListener(new WindowListener() {				
			public void windowClosed(WindowEvent e) { 
				_isFillingOutSurvey = false;
			}
			public void windowOpened(WindowEvent e) { }				
			public void windowClosing(WindowEvent e) { }				
			public void windowIconified(WindowEvent e) { }				
			public void windowDeiconified(WindowEvent e) { }				
			public void windowActivated(WindowEvent e) { }
			public void windowDeactivated(WindowEvent e) { }
		});
		// TODO: Pauses execution until user submits; Should we do this?
		// WAITS until user submits or closes window.
		boolean print = true;
		while (_isFillingOutSurvey) {
			if (print) {
				System.out.println("User is filling out survey. " + 
					"Must submit or close to run MainFrame.");
				print = false;
			}
		}
	}
}
