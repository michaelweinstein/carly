package frontend.view.assignments;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import data.Assignment;
import frontend.Utils;
import frontend.view.ViewController;

/**
 * Represents all current tasks and their collective data
 * 
 * @author dgattey
 */
public class AssignmentsView extends JPanel {
	
	private final JScrollPane		scroller;
	private final ViewController	vc;
	private final JPanel			assignmentItems;
	
	// Constants
	private static final long		serialVersionUID	= -3581722774976194311L;
	
	/**
	 * Constructor sets colors and puts the scroll view in place
	 * 
	 * @param vc the view controller for this view
	 */
	public AssignmentsView(final ViewController vc) {
		this.vc = vc;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.themeComponent(this);
		
		// Make title and scroll pane and add to view
		assignmentItems = new JPanel();
		assignmentItems.setLayout(new BoxLayout(assignmentItems, BoxLayout.Y_AXIS));
		assignmentItems.setAlignmentX(LEFT_ALIGNMENT);
		Utils.themeComponent(assignmentItems);
		scroller = new JScrollPane(assignmentItems);
		scroller.setBorder(null);
		Utils.themeComponent(scroller);
		Utils.themeComponent(scroller.getViewport());
		
		final JLabel title = new JLabel("All Assignments");
		title.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 18));
		Utils.themeComponent(title);
		
		add(Box.createVerticalStrut(20));
		add(title);
		add(Box.createVerticalStrut(15));
		add(scroller);
	}
	
	/**
	 * Loads in assignments
	 */
	@Override
	public void repaint() {
		if (assignmentItems != null) {
			assignmentItems.removeAll();
			for (final Assignment a : vc.getAssignments()) {
				assignmentItems.add(new AssignmentItemView(a));
			}
		}
		super.repaint();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(250, 300);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(250, 300);
	}
}
