package frontend.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import frontend.Utils;

/**
 * Class for the dialog box that pops up when adding a new assignment
 * 
 * @author dgattey
 */
public class AddAssignmentDialog extends JDialog {
	
	private static final long	serialVersionUID	= -5465413225077024401L;
	private JButton				cancelButton;
	private JButton				addButton;
	private JTextField			titleField;
	private JSpinner			dateTimeField;
	
	/**
	 * Constructor creates all relevant data
	 */
	public AddAssignmentDialog() {
		Utils.themeComponent(this);
		Utils.themeComponent(getRootPane());
		Utils.padComponent(getRootPane(), 15, 15);
		setMinimumSize(getMinimumSize()); // Silly but required
		
		final JLabel dialogTitle = createDialogTitle();
		final JPanel centerPane = createFieldsAndLabels();
		final JPanel buttons = createButtonsPane();
		
		// Addition of all items to dialog
		add(dialogTitle, BorderLayout.NORTH);
		add(centerPane, BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);
		pack();
	}
	
	/**
	 * Creates the buttons panel and returns it
	 * 
	 * @return a new JPanel for the buttons
	 */
	private JPanel createButtonsPane() {
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
				clearContents();
				dispose();
			}
			
		});
		addButton = new JButton("Add Assignment");
		final JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		buttons.add(cancelButton);
		buttons.add(addButton);
		Utils.themeComponent(buttons);
		return buttons;
	}
	
	/**
	 * Creates all fields and labels in a center panel
	 * 
	 * @return a new JPanel for the center full of labels and fields
	 */
	private JPanel createFieldsAndLabels() {
		final JPanel pane = new JPanel();
		final GridBagConstraints c = new GridBagConstraints();
		pane.setLayout(new GridBagLayout());
		Utils.themeComponent(pane);
		
		// Title label
		final JLabel titleLabel = new JLabel("Title: ");
		Utils.themeComponent(titleLabel);
		Utils.setFont(titleLabel, 14);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(titleLabel, c);
		
		// Title field
		titleField = new JTextField();
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridy = 0;
		pane.add(titleField, c);
		
		// Time and Date label
		final JLabel timeDateLabel = new JLabel("Due Date: ");
		Utils.themeComponent(timeDateLabel);
		Utils.setFont(timeDateLabel, 14);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		pane.add(timeDateLabel, c);
		
		// Date and time spinner
		dateTimeField = new JSpinner(new SpinnerDateModel());
		final JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(dateTimeField, "MMMM dd, yyyy 'at' hh:mm a");
		dateTimeField.setEditor(timeEditor);
		dateTimeField.setValue(new Date());
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(dateTimeField);
		
		Utils.padComponent(pane, 20, 0);
		
		return pane;
	}
	
	/**
	 * Creates a label representing the title of the dialog
	 * 
	 * @return a new JLabel for the title
	 */
	private static JLabel createDialogTitle() {
		final JLabel dialogTitle = new JLabel("Add New Assignment");
		Utils.setFont(dialogTitle, 22);
		dialogTitle.setOpaque(true);
		Utils.padComponent(dialogTitle, 0, 0, 10, 0);
		Utils.addBorderBottom(dialogTitle);
		Utils.padComponentWithBorder(dialogTitle, 0, 10);
		Utils.themeComponent(dialogTitle);
		return dialogTitle;
	}
	
	/**
	 * Clears the contents of all the fields
	 */
	private void clearContents() {
		
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(350, 400);
	}
	
}