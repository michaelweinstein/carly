package frontend.view.assignments;

import java.awt.Font;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import data.Assignment;
import data.ITask;
import frontend.Utils;

/**
 * Represents an assignment item in the sidebar assignment view
 * 
 * @author dgattey
 */
public class AssignmentItemView extends JPanel {
	
	private static final long	serialVersionUID	= -4869025641418957982L;
	private final Assignment	assignment;
	
	/**
	 * Constructs a view from an assignment object for use later
	 * 
	 * @param assignment an Assignment
	 */
	public AssignmentItemView(final Assignment assignment) {
		this.assignment = assignment;
		
		createView();
	}
	
	/**
	 * Actually grabs everything to make the view
	 */
	private void createView() {
		Utils.themeComponent(this);
		Utils.padComponent(this, 0, 10, 20, 0);
		Utils.addBorderLeft(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(LEFT_ALIGNMENT);
		
		// Title
		final String title = assignment.getName();
		final JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 16));
		Utils.padComponent(titleLabel, 0, 0);
		Utils.themeComponent(titleLabel);
		add(titleLabel);
		
		// Date
		final Date due = assignment.getDueDate();
		final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		final JLabel dueLabel = new JLabel("Due Date: " + formatter.format(due));
		dueLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.ITALIC, 11));
		Utils.padComponent(dueLabel, 0, 0);
		Utils.themeComponent(dueLabel);
		Utils.addBorderBottom(dueLabel);
		add(dueLabel);
		
		add(Box.createVerticalStrut(20));
		
		// Tasks
		final List<ITask> tasks = assignment.getTasks();
		final Object dataValues[][] = new Object[tasks.size()][2];
		for (int i = 0; i < tasks.size(); i++) {
			dataValues[i][0] = tasks.get(i).getName().split(":")[1];
			dataValues[i][1] = tasks.get(i).getPercentOfTotal() * 100 + "%";
			// TODO: More with percent of total, better stuff in general
		}
		final String colNames[] = { "Step Name", "% of Total" };
		final StepModel mod = new StepModel(dataValues, colNames);
		mod.setEditable(false);
		final StepViewTable taskTable = new StepViewTable(mod);
		Utils.themeComponent(taskTable);
		Utils.padComponent(taskTable, 0, 10, 10, 10);
		add(taskTable);
	}
}
