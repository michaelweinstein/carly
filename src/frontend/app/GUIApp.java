package frontend.app;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import data.IAssignment;
import frontend.view.MainFrame;
import frontend.view.calendar.CalendarView;

/**
 * Creates a GUI interface for the Maps program
 * 
 * @author dgattey
 */
public class GUIApp extends App {
	
	private final MainFrame			_window;
	private final List<IAssignment>	allAssignments	= new ArrayList<>();	// TESTINGONLY
																			
	/**
	 * Uses the App constructor plus gui specific stuff
	 * 
	 * @param debug if we should be in debug mode
	 */
	public GUIApp(final boolean debug) {
		super(debug);
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
				_window.pack();
				_window.setVisible(true);
			}
		});
	}
	
	/**
	 * STRICTLY FOR TESTING
	 * 
	 * @param a the assignment to add
	 */
	public void addAssignment(final IAssignment a) {
		allAssignments.add(a);
	}
	
	/**
	 * STRICTLY FOR TESTING
	 * 
	 * @return the list of assignments
	 */
	public List<IAssignment> getAssignments() {
		return allAssignments;
	}
	
	/**
	 * Redraws whole window
	 */
	public void redraw() {
		_window.reloadData();
		_window.revalidate();
		_window.repaint();
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
