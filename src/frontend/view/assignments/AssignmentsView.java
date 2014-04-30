package frontend.view.assignments;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;

import backend.database.StorageService;
import data.IAssignment;
import frontend.Utils;
import frontend.app.GUIApp;

/**
 * Represents all current tasks and their collective data
 * 
 * @author dgattey
 */
public class AssignmentsView extends JPanel {
	
	private final JScrollPane			_scroller;
	private final JPanel				_assignmentItems;
	private AssignmentItemView			_selected;
	private JDialog						_deletionDialog;
	private final EditAssignmentDialog	_editor;
	private JTextArea					_deleteText;
	private JButton						_confirm;
	private final GUIApp				_app;
	private ActionListener				_deleter;
	
	// Constants
	private static final long			serialVersionUID	= -3581722774976194311L;
	
	/**
	 * Constructor sets colors and puts the scroll view in place
	 * 
	 * @param app the view controller for this view
	 */
	public AssignmentsView(final GUIApp app) {
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.themeComponent(this);
		
		_app = app;
		
		// Make title and scroll pane and add to view
		_assignmentItems = new JPanel();
		_assignmentItems.setLayout(new BoxLayout(_assignmentItems, BoxLayout.Y_AXIS));
		_assignmentItems.setAlignmentX(LEFT_ALIGNMENT);
		Utils.padComponent(_assignmentItems, 10, 0, 40, 0);
		Utils.themeComponent(_assignmentItems);
		final JPanel holder = new JPanel();
		Utils.themeComponent(holder);
		holder.add(_assignmentItems);
		_scroller = new JScrollPane(holder);
		_scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		_scroller.setBorder(null);
		Utils.themeComponent(_scroller);
		Utils.themeComponent(_scroller.getViewport());
		
		// Title
		final JLabel title = new JLabel("All Assignments");
		title.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 22));
		Utils.themeComponent(title);
		Utils.padComponent(title, 15, 0, 0, 0);
		Utils.addBorderBottom(title);
		Utils.padComponentWithBorder(title, 0, 20);
		
		add(title);
		add(_scroller);
		createDeletionDialog();
		_editor = new EditAssignmentDialog(app);
		
		reloadData();
	}
	
	/**
	 * Creates the deletion dialog to show later
	 */
	private void createDeletionDialog() {
		_deletionDialog = new JDialog();
		_deletionDialog.setAlwaysOnTop(true);
		
		// Create main pane
		final JPanel deleteMain = new JPanel();
		deleteMain.setLayout(new BoxLayout(deleteMain, BoxLayout.Y_AXIS));
		deleteMain.setAlignmentX(LEFT_ALIGNMENT);
		Utils.themeComponent(deleteMain);
		Utils.padComponent(deleteMain, 15, 15);
		
		// Delete text and title
		final JLabel deleteTitle = new JLabel("Delete Assignment?");
		_deleteText = new JTextArea("Are you sure?");
		_deleteText.setAlignmentX(LEFT_ALIGNMENT);
		deleteTitle.setAlignmentX(LEFT_ALIGNMENT);
		_deleteText.setFont(new Font(Utils.APP_FONT_NAME, Font.PLAIN, 14));
		_deleteText.setEditable(false);
		_deleteText.setFocusable(false);
		_deleteText.setWrapStyleWord(true);
		_deleteText.setLineWrap(true);
		Utils.themeComponent(deleteTitle);
		Utils.setFont(deleteTitle, 20);
		Utils.padComponent(deleteTitle, 0, 0, 20, 0);
		Utils.addBorderBottom(deleteTitle);
		Utils.padComponentWithBorder(deleteTitle, 0, 0, 10, 0);
		Utils.themeComponent(_deleteText);
		
		// Create buttons
		_confirm = new JButton("Delete");
		final JButton cancel = new JButton("Cancel");
		_confirm.setAlignmentX(RIGHT_ALIGNMENT);
		cancel.setAlignmentX(RIGHT_ALIGNMENT);
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				_deletionDialog.setVisible(false);
			}
		});
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(cancel);
		buttonPanel.add(_confirm);
		buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
		Utils.themeComponent(buttonPanel);
		
		// Add all to dialog
		deleteMain.add(deleteTitle);
		deleteMain.add(Box.createVerticalStrut(10));
		deleteMain.add(_deleteText);
		deleteMain.add(buttonPanel);
		_deletionDialog.setContentPane(deleteMain);
		_deletionDialog.setMinimumSize(new Dimension(350, 200));
		_deletionDialog.pack();
		
		// Listens for delete key
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "delete");
		getActionMap().put("delete", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				showDeleteAssignmentDialog(getSelected());
			}
		});
	}
	
	/**
	 * Shows the delete assignment dialog
	 * 
	 * @param view the assignment view to show
	 */
	public void showDeleteAssignmentDialog(final AssignmentItemView view) {
		if (view != null) {
			final String title = view.getAssignment().getName();
			_deleteText.setText(String.format("Are you sure you want to delete \"%s\"? This action can't be undone.",
					title));
			_confirm.removeActionListener(_deleter);
			_deleter = new ActionListener() {
				
				@Override
				public void actionPerformed(final ActionEvent e) {
					StorageService.removeAssignment(view.getAssignment());
					_app.reload();
					_deletionDialog.setVisible(false);
				}
			};
			_confirm.addActionListener(_deleter);
			_deletionDialog.setVisible(true);
		}
	}
	
	/**
	 * Reloads assignments
	 */
	public void reloadData() {
		_assignmentItems.removeAll();
		final List<IAssignment> ass = reloadAllAssignments();
		
		if (ass == null || ass.isEmpty()) {
			final JLabel l = new JLabel("You have nothing left to do!");
			final JLabel l2 = new JLabel("<html>Add an assignment using <br/> the button above.</html>");
			Utils.padComponent(l, 40, 0, 10, 0);
			Utils.themeComponent(l);
			Utils.themeComponent(l2);
			l.setFont(new Font(Utils.APP_FONT_NAME, Font.PLAIN, 15));
			l2.setFont(new Font(Utils.APP_FONT_NAME, Font.ITALIC, 12));
			l2.setForeground(Utils.COLOR_FOREGROUND.darker());
			_assignmentItems.add(l);
			_assignmentItems.add(l2);
		} else {
			for (final IAssignment a : ass) {
				final JPanel add = new AssignmentItemView(a, this);
				Utils.addBorderBottom(add);
				Utils.padComponentWithBorder(add, 0, 20);
				_assignmentItems.add(add);
			}
		}
	}
	
	/**
	 * Reads in all assignments from database
	 * 
	 * @return a list of IAssignment to use in actually populating the view
	 */
	private static List<IAssignment> reloadAllAssignments() {
		final List<IAssignment> aments = StorageService.getAllAssignmentsWithinRange(new Date(0), new Date(
				Long.MAX_VALUE - 1));
		Collections.sort(aments, new Comparator<IAssignment>() {
			
			@Override
			public int compare(final IAssignment o1, final IAssignment o2) {
				return o1.getDueDate().compareTo(o2.getDueDate());
			}
			
		});
		return aments;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(220, 200);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(220, 200);
	}
	
	/**
	 * Gives back the selected item
	 * 
	 * @return the currently selected AssignmentItemView
	 */
	public AssignmentItemView getSelected() {
		return _selected;
	}
	
	/**
	 * Sets the selected item
	 * 
	 * @param assignmentItemView the currently selected AssignmentItemView
	 */
	public void setSelected(final AssignmentItemView assignmentItemView) {
		_selected = assignmentItemView;
		repaint();
	}
	
	/**
	 * Gets the editor
	 * 
	 * @return the editor dialog
	 */
	public EditAssignmentDialog getEditor() {
		return _editor;
	}
}
