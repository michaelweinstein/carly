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
	
	private final JScrollPane	scroller;
	private final JPanel		assignmentItems;
	private AssignmentItemView	_selected;
	private JDialog				_deletionDialog;
	
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
		Utils.padComponent(assignmentItems, 10, 0, 40, 0);
		Utils.themeComponent(assignmentItems);
		final JPanel holder = new JPanel();
		Utils.themeComponent(holder);
		holder.add(assignmentItems);
		scroller = new JScrollPane(holder);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setBorder(null);
		Utils.themeComponent(scroller);
		Utils.themeComponent(scroller.getViewport());
		
		// Title
		final JLabel title = new JLabel("All Assignments");
		title.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 22));
		Utils.themeComponent(title);
		Utils.padComponent(title, 15, 0, 0, 0);
		Utils.addBorderBottom(title);
		Utils.padComponentWithBorder(title, 0, 20);
		
		add(title);
		add(scroller);
		createDeletionDialog(app);
		
		reloadData();
	}
	
	/**
	 * Creates the deletion dialog to show later
	 * 
	 * @param app the app to use to reload
	 */
	private void createDeletionDialog(final GUIApp app) {
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
		final JTextArea deleteText = new JTextArea("Are you sure?");
		deleteText.setAlignmentX(LEFT_ALIGNMENT);
		deleteTitle.setAlignmentX(LEFT_ALIGNMENT);
		deleteText.setFont(new Font(Utils.APP_FONT_NAME, Font.PLAIN, 14));
		deleteText.setEditable(false);
		deleteText.setFocusable(false);
		deleteText.setWrapStyleWord(true);
		deleteText.setLineWrap(true);
		Utils.themeComponent(deleteTitle);
		Utils.setFont(deleteTitle, 20);
		Utils.padComponent(deleteTitle, 0, 0, 20, 0);
		Utils.addBorderBottom(deleteTitle);
		Utils.padComponentWithBorder(deleteTitle, 0, 0, 10, 0);
		Utils.themeComponent(deleteText);
		
		// Create buttons
		final JButton confirm = new JButton("Delete");
		final JButton cancel = new JButton("Cancel");
		confirm.setAlignmentX(RIGHT_ALIGNMENT);
		confirm.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				StorageService.removeAssignment(getSelected().getAssignment());
				app.reload();
				_deletionDialog.setVisible(false);
			}
		});
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
		buttonPanel.add(confirm);
		buttonPanel.add(cancel);
		buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
		Utils.themeComponent(buttonPanel);
		
		// Add all to dialog
		deleteMain.add(deleteTitle);
		deleteMain.add(Box.createVerticalStrut(10));
		deleteMain.add(deleteText);
		deleteMain.add(buttonPanel);
		_deletionDialog.setContentPane(deleteMain);
		_deletionDialog.setMinimumSize(new Dimension(350, 200));
		_deletionDialog.pack();
		
		// Listens for delete key
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "delete");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("BACKSPACE"), "delete");
		getActionMap().put("delete", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (getSelected() != null) {
					final String title = getSelected().getAssignment().getName();
					deleteText.setText(String.format(
							"Are you sure you want to delete \"%s\"? This action can't be undone.", title));
					_deletionDialog.setVisible(true);
				}
			}
		});
	}
	
	/**
	 * Reloads assignments
	 */
	public void reloadData() {
		assignmentItems.removeAll();
		final List<IAssignment> ass = reloadAllAssignments();
		
		if (ass == null || ass.isEmpty()) {
			final JLabel l = new JLabel("You're free!");
			Utils.themeComponent(l);
			l.setFont(new Font(Utils.APP_FONT_NAME, Font.PLAIN, 15));
			assignmentItems.add(l);
		} else {
			for (final IAssignment a : ass) {
				final JPanel add = new AssignmentItemView(a, this);
				Utils.addBorderBottom(add);
				Utils.padComponentWithBorder(add, 0, 20);
				assignmentItems.add(add);
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
}
