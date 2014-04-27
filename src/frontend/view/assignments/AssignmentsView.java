package frontend.view.assignments;

import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import data.IAssignment;
import frontend.Utils;
import frontend.app.GUIApp;

/**
 * Represents all current tasks and their collective data
 * 
 * @author dgattey
 */
public class AssignmentsView extends JPanel {
	
	private final JScrollPane	scroller;
	private final JPanel		assignmentItems;
	
	// Constants
	private static final long	serialVersionUID	= -3581722774976194311L;
	
	/**
	 * Constructor sets colors and puts the scroll view in place
	 * 
	 * @param app the view controller for this view
	 */
	public AssignmentsView(final GUIApp app) {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.themeComponent(this);
		
		// Make title and scroll pane and add to view
		assignmentItems = new JPanel();
		assignmentItems.setLayout(new BoxLayout(assignmentItems, BoxLayout.Y_AXIS));
		assignmentItems.setAlignmentX(LEFT_ALIGNMENT);
		Utils.padComponent(assignmentItems, 0, 30);
		final JPanel aiWrapper = new JPanel();
		aiWrapper.add(assignmentItems);
		Utils.themeComponent(aiWrapper);
		Utils.themeComponent(assignmentItems);
		scroller = new JScrollPane(aiWrapper);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setBorder(null);
		Utils.themeComponent(scroller);
		Utils.themeComponent(scroller.getViewport());
		
		final JLabel title = new JLabel("All Assignments");
		title.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 22));
		Utils.themeComponent(title);
		Utils.padComponent(title, 15, 0, 0, 0);
		Utils.addBorderBottom(title);
		Utils.padComponentWithBorder(title, 0, 20);
		
		add(title);
		add(scroller);
		
		reloadData();
	}
	
	/**
	 * Reloads assignments
	 */
	public void reloadData() {
		assignmentItems.removeAll();
		final List<IAssignment> ass = reloadAllAssignments();
		if (ass.isEmpty()) {
			final JLabel l = new JLabel("You're free!");
			Utils.themeComponent(l);
			l.setFont(new Font(Utils.APP_FONT_NAME, Font.PLAIN, 13));
			assignmentItems.add(l);
		}
		for (final IAssignment a : ass) {
			assignmentItems.add(new AssignmentItemView(a));
		}
	}
	
	/**
	 * Reads in all assignments from database
	 * 
	 * @return a list of IAssignment to use in actually populating the view
	 */
	private static List<IAssignment> reloadAllAssignments() {
		// TODO actually read in from DB
		return null;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(280, 300);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(280, 300);
	}
}
