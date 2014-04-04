package frontend.view;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import frontend.Utilities;

/**
 * Represents all current tasks and their collective data
 * 
 * @author dgattey
 */
public class AssignmentsView extends JPanel {
	
	private final JScrollPane	scroller;
	
	// Constants
	private static final long	serialVersionUID	= -3581722774976194311L;
	
	/**
	 * Constructor sets colors and puts the scroll view in place
	 */
	public AssignmentsView() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utilities.themeComponent(this);
		Utilities.addBorderLeft(this);
		
		// Make title and scroll pane and add to view
		scroller = new JScrollPane();
		scroller.setBorder(null);
		Utilities.themeComponent(scroller);
		Utilities.themeComponent(scroller.getViewport());
		
		final JLabel title = new JLabel("All Assignments");
		title.setFont(new Font(Utilities.APP_FONT_NAME, Font.BOLD, 20));
		Utilities.themeComponent(title);
		
		add(Box.createVerticalStrut(20));
		add(title);
		add(Box.createVerticalStrut(15));
		add(scroller);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200, 300);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(100, 200);
	}
}