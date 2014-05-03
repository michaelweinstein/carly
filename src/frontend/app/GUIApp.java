package frontend.app;

import hub.HubController;

import javax.swing.SwingUtilities;

import frontend.view.MainFrame;
import frontend.view.calendar.CalendarView;

/**
 * Creates a GUI interface for the Maps program
 * 
 * @author dgattey
 */
public class GUIApp extends App {
	
	private final MainFrame	_window;
	
	/**
	 * Uses the App constructor plus gui specific stuff
	 * 
	 * @param debug if we should be in debug mode
	 */
	public GUIApp(final boolean debug, final boolean runStartUp) {
		super(debug);
		// TODO: Run startup survey according to boolean param
		
		_window = new MainFrame(this);
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
}
