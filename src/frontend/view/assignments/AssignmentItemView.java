package frontend.view.assignments;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
	private final IAssignment		_assignment;
	private final AssignmentsView	_parent;
	private JPanel					_taskPanel;
	private StepViewTable			_taskTable;
	private JButton					_delete;
	private JButton					_edit;
	
	/**
	 * Constructs a view from an assignment object for use later
	 * 
	 * @param a an Assignment
	 * @param parent the containing AssignmentsView
	 */
	public AssignmentItemView(final IAssignment a, final AssignmentsView parent) {
		_assignment = a;
		_parent = parent;
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
		final String title = _assignment.getName();
		final JLabel titleLabel = new JLabel(title);
		titleLabel.setToolTipText(title);
		titleLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 16));
		Utils.themeComponent(titleLabel);
		add(titleLabel);
		add(Box.createVerticalStrut(5));
		
		// Date
		final Date due = _assignment.getDueDate();
		final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		final JLabel dueLabel = new JLabel("Due: " + formatter.format(due));
		dueLabel.setForeground(Utils.COLOR_FOREGROUND.darker());
		dueLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.ITALIC, 12));
		add(dueLabel);
		add(Box.createVerticalStrut(10));
		
		// Edit button
		_edit = new JButton("Edit Assignment");
		_edit.setVisible(false);
		_edit.setAlignmentX(LEFT_ALIGNMENT);
		_edit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						_parent.getEditor().setVisible(false);
						_parent.getEditor().setAssignment(_assignment);
						_parent.getEditor().setVisible(true);
					}
				});
			}
		});
		add(_edit);
		
		// Delete button
		_delete = new JButton("Delete Assignment");
		_delete.setVisible(false);
		_delete.setAlignmentX(LEFT_ALIGNMENT);
		_delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						_parent.getEditor().setVisible(false);
						_parent.showDeleteAssignmentDialog(AssignmentItemView.this);
					};
				});
			}
		});
		add(_delete);
		
		// Tasks
		final List<ITask> tasks = _assignment.getTasks();
		final Object dataValues[][] = new Object[tasks.size()][2];
		for (int i = 0; i < tasks.size(); i++) {
			dataValues[i][0] = tasks.get(i);
			dataValues[i][1] = Math.round(tasks.get(i).getPercentOfTotal() * 100) + "%";
			// TODO: More with percent of total, better stuff in general
		}
		final String colNames[] = { "Step Name", "% of Total" };
		final StepModel mod = new StepModel(dataValues, colNames);
		mod.setEditable(false);
		_taskTable = new StepViewTable(mod, _assignment, true);
		_taskTable.setFocusable(false);
		_taskPanel = new JPanel();
		_taskPanel.add(_taskTable);
		_taskTable.addMouseListener(this);
		Utils.themeComponent(_taskPanel);
		Utils.themeComponent(_taskTable);
		add(_taskPanel);
		
		for (final Component comp : getComponents()) {
			comp.addMouseListener(this);
		}
		
		// Before now
		if (due.before(new Date())) {
			dueLabel.setText("Past " + dueLabel.getText());
			dueLabel.setForeground(Utils.COLOR_ACCENT);
			titleLabel.setForeground(Utils.COLOR_LIGHT_BG);
			_taskTable.setForeground(Utils.COLOR_LIGHT_BG);
			_taskTable.setSelectionForeground(Utils.COLOR_LIGHT_BG);
			_taskTable.setGridColor(Utils.COLOR_LIGHT_BG);
		}
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Color c = (_parent.getSelected() != null && _parent.getSelected().equals(this)) ? new Color(50, 50, 50)
				: Utils.COLOR_BACKGROUND;
		setBackground(c);
		_taskPanel.setBackground(c);
		_taskTable.setBackground(c);
		_taskTable.setSelectionBackground(c);
		super.paintComponent(g);
	}
	
	@Override
	public Dimension getMaximumSize() {
		return new Dimension(200, Integer.MAX_VALUE - 1);
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {
		
		// Not meant for this panel
		if (e.getSource() == _taskTable) {
			repaint();
			return;
		}
		
		// Set the currently selected to be null
		if (_parent.getSelected() != null && _parent.getSelected()._delete != null) {
			_parent.getSelected()._delete.setVisible(false);
			_parent.getSelected()._edit.setVisible(false);
		}
		
		// Either set this selected or nothing if already selected (toggle)
		if (_parent.getSelected() != this) {
			_delete.setVisible(true);
			_edit.setVisible(true);
			_parent.setSelected(this);
		} else {
			_delete.setVisible(false);
			_edit.setVisible(false);
			_parent.setSelected(null);
		}
		
		repaint();
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
		return _assignment;
	}
}
