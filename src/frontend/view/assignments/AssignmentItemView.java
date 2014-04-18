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
		Utils.padComponent(this, 10, 10);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Title
		final String title = assignment.getName();
		final JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 16));
		Utils.themeComponent(titleLabel);
		add(titleLabel);
		
		// Date
		final Date due = assignment.getDueDate();
		final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		final JLabel dueLabel = new JLabel(formatter.format(due));
		dueLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.ITALIC, 11));
		Utils.themeComponent(dueLabel);
		Utils.addBorderBottom(dueLabel);
		Utils.padComponent(dueLabel, 0, 10);
		add(dueLabel);
		
		add(Box.createVerticalStrut(20));
		
		// Tasks
		final List<ITask> tasks = assignment.getTasks();
		final Object dataValues[][] = new Object[tasks.size()][2];
		for (int i = 0; i < tasks.size(); i++) {
			dataValues[i][0] = tasks.get(i).getName();
			dataValues[i][1] = tasks.get(i).getPercentComplete();
			// TODO: More with percent of total, better stuff in general
		}
		final String colNames[] = { "Step Name", "% of Total" };
		final StepViewTable taskTable = new StepViewTable(new StepModel(dataValues, colNames));
		Utils.themeComponent(taskTable);
		add(taskTable);
	}
}
