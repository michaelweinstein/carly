package frontend.view.assignments;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import data.IAssignment;
import data.ITask;
import frontend.Utils;

/**
 * Represents an assignment item in the sidebar assignment view
 * 
 * @author dgattey
 */
public class AssignmentItemView extends JPanel implements MouseListener {
	
	private static final long		serialVersionUID	= 1L;
	private final IAssignment		assignment;
	private final AssignmentsView	parent;
	private JPanel					taskPanel;
	private StepViewTable			taskTable;
	
	/**
	 * Constructs a view from an assignment object for use later
	 * 
	 * @param a an Assignment
	 * @param parent the containing AssignmentsView
	 */
	public AssignmentItemView(final IAssignment a, final AssignmentsView parent) {
		assignment = a;
		this.parent = parent;
		addMouseListener(this);
		createView();
		setFocusable(false);
	}
	
	/**
	 * Actually grabs everything to make the view
	 */
	private void createView() {
		Utils.themeComponent(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setAlignmentX(LEFT_ALIGNMENT);
		setAlignmentY(LEFT_ALIGNMENT);
		
		// Title
		final String title = assignment.getName();
		final JLabel titleLabel = new JLabel(title);
		titleLabel.setToolTipText(title);
		titleLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 16));
		Utils.themeComponent(titleLabel);
		add(titleLabel);
		add(Box.createVerticalStrut(5));
		
		// Date
		final Date due = assignment.getDueDate();
		final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		final JLabel dueLabel = new JLabel("Due: " + formatter.format(due));
		dueLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.ITALIC, 12));
		dueLabel.setForeground(Utils.COLOR_FOREGROUND.darker());
		add(dueLabel);
		add(Box.createVerticalStrut(10));
		
		// Tasks
		final List<ITask> tasks = assignment.getTasks();
		final Object dataValues[][] = new Object[tasks.size()][2];
		for (int i = 0; i < tasks.size(); i++) {
			dataValues[i][0] = tasks.get(i).getName().split(":")[1];
			dataValues[i][1] = Math.round(tasks.get(i).getPercentOfTotal() * 100) + "%";
			// TODO: More with percent of total, better stuff in general
		}
		final String colNames[] = { "Step Name", "% of Total" };
		final StepModel mod = new StepModel(dataValues, colNames);
		mod.setEditable(false);
		taskTable = new StepViewTable(mod, assignment);
		taskTable.setFocusable(false);
		taskPanel = new JPanel();
		taskPanel.add(taskTable);
		taskTable.addMouseListener(this);
		Utils.themeComponent(taskPanel);
		Utils.themeComponent(taskTable);
		add(taskPanel);
		
		for (final Component comp : getComponents()) {
			comp.addMouseListener(this);
		}
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Color c = (parent.getSelected() != null && parent.getSelected().equals(this)) ? new Color(50, 50, 50)
				: Utils.COLOR_BACKGROUND;
		setBackground(c);
		taskPanel.setBackground(c);
		taskTable.setBackground(c);
		taskTable.setSelectionBackground(c);
		super.paintComponent(g);
	}
	
	@Override
	public Dimension getMaximumSize() {
		return new Dimension(200, Integer.MAX_VALUE - 1);
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {
		if (parent.getSelected() != this) {
			parent.setSelected(this);
		} else {
			parent.setSelected(null);
		}
	}
	
	@Override
	public void mousePressed(final MouseEvent e) {}
	
	@Override
	public void mouseReleased(final MouseEvent e) {}
	
	@Override
	public void mouseEntered(final MouseEvent e) {}
	
	@Override
	public void mouseExited(final MouseEvent e) {}
	
	/**
	 * Gets the associated assignment
	 * 
	 * @return the IAssignment object
	 */
	public IAssignment getAssignment() {
		return assignment;
	}
}
