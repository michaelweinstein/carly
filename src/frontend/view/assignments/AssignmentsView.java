package frontend.view.assignments;

import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
		title.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 22));
		Utils.themeComponent(title);
		Utils.padComponent(title, 15, 0, 10, 0);
		Utils.addBorderBottom(title);
		Utils.padComponentWithBorder(title, 0, 10);
		
		add(title);
		add(Box.createVerticalStrut(20));
		add(scroller);
		
		reloadData();
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(250, 300);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(250, 300);
	}
	
	/**
	 * Reloads assignments
	 */
	public void reloadData() {
		assignmentItems.removeAll();
		final List<Assignment> ass = vc.getAssignments();
		if (ass.isEmpty()) {
			final JTextArea l = new JTextArea("You're free!");
			Utils.themeComponent(l);
			Utils.padComponent(l, 20, 0);
			l.setEditable(false);
			l.setFont(new Font(Utils.APP_FONT_NAME, Font.PLAIN, 12));
			assignmentItems.add(l);
		}
		for (final Assignment a : ass) {
			assignmentItems.add(new AssignmentItemView(a));
		}
	}
}
