package frontend.view.assignments;

import hub.HubController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;

import data.Assignment;
import data.ITemplate;
import data.ITemplateStep;
import data.Template;
import data.TemplateStep;
import frontend.Utils;
import frontend.app.GUIApp;

/**
 * Class for the dialog box that pops up when adding a new assignment
 * 
 * @author dgattey
 */
public class AddAssignmentDialog extends JDialog implements TableModelListener {
	
	private final GUIApp			app;
	private static final String		DATE_FORMAT_STRING	= "MMMM dd, yyyy 'at' hh:mm a";
	private static final String		DEFAULT_LABEL		= " ";
	private static final long		serialVersionUID	= -5465413225077024401L;
	private JButton					_cancelButton;
	private JButton					_addButton;
	private JTextField				_titleField;
	private JSpinner				_dateTimeField;
	private JLabel					_statusLabel;
	private JComboBox<ITemplate>	_templatePicker;
	private StepViewTable			_stepList;
	private StepModel				_stepModel;
	
	/**
	 * Constructor creates all relevant data
	 * 
	 * @param vc the parent view controller
	 */
	public AddAssignmentDialog(final GUIApp app) {
		super();
		this.app = app;
		
		Utils.themeComponent(this);
		Utils.themeComponent(getRootPane());
		Utils.padComponent(getRootPane(), 15, 15);
		setMinimumSize(getMinimumSize()); // Silly but required
		
		final JLabel dialogTitle = createDialogTitle();
		final JPanel centerPane = createFieldsAndLabels();
		final JScrollPane scroller = new JScrollPane(centerPane);
		final JPanel bottom = createButtonsAndStatusPane();
		
		Utils.themeComponent(scroller);
		Utils.themeComponent(scroller.getViewport());
		Utils.themeComponent(scroller.getVerticalScrollBar());
		Utils.padComponent(scroller, 0, 0);
		
		// Addition of all items to dialog
		add(dialogTitle, BorderLayout.NORTH);
		add(scroller, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);
		pack();
	}
	
	/**
	 * Creates the buttons panel and returns it
	 * 
	 * @return a new JPanel for the buttons
	 */
	private JPanel createButtonsAndStatusPane() {
		_statusLabel = new JLabel(DEFAULT_LABEL);
		_statusLabel.setFont(new Font(Utils.APP_FONT_NAME, Font.PLAIN, 12));
		
		// Simply deletes contents and closes
		_cancelButton = new JButton("Cancel");
		_cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
				clearContents();
				dispose();
			}
			
		});
		
		// Adds a new assignment through the hub
		_addButton = new JButton("Add");
		_addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					final Assignment a = parseFields();
					HubController.passAssignmentToLearner(a);
					// FOR TESTING ONLY
					app.addAssignment(a);
					app.redraw();
					// ///////
					clearContents();
					dispose();
				} catch (final IllegalArgumentException e1) {
					_statusLabel.setText("Oops! " + e1.getMessage());
				}
				
			}
			
		});
		final JPanel pane = new JPanel(new GridLayout(2, 1));
		pane.setAlignmentX(SwingConstants.CENTER);
		pane.setAlignmentY(SwingConstants.CENTER);
		final JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		buttons.add(_cancelButton);
		buttons.add(_addButton);
		pane.add(_statusLabel);
		pane.add(buttons);
		Utils.themeComponent(buttons);
		Utils.themeComponent(pane);
		Utils.themeComponent(_statusLabel);
		Utils.padComponent(_statusLabel, 0, 10);
		Utils.addBorderBottom(_statusLabel);
		Utils.padComponentWithBorder(_statusLabel, 0, 0, 10, 0);
		return pane;
	}
	
	/**
	 * Actually makes an assignment from the fields
	 * 
	 * @return a new IAssignment representing the contents of the pane
	 * @throws IllegalArgumentException if the fields weren't right
	 */
	protected Assignment parseFields() throws IllegalArgumentException {
		
		// Check title
		final String titleText = _titleField.getText();
		if (titleText == null || titleText.isEmpty()) {
			throw new IllegalArgumentException("Title field is empty");
		}
		
		// Check and get date
		Date due = null;
		try {
			final SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
			due = formatter.parse(_dateTimeField.getValue().toString());
			if (due.before(new Date())) {
				throw new IllegalArgumentException("Due date is in the past!");
			}
		} catch (final ParseException e) {
			throw new IllegalArgumentException(String.format("Bad date format (%s)", e.getMessage()));
		}
		
		// Make a list of template steps out of the text
		final List<ITemplateStep> steps = new ArrayList<>();
		double totalPercentage = 0;
		for (int i = 0; i < _stepList.getModel().getRowCount(); i++) {
			final String title = _stepList.getModel().getValueAt(i, StepModel.TITLE_INDEX).toString();
			final String percString = _stepList.getModel().getValueAt(i, StepModel.PERCENT_INDEX).toString();
			
			// Important! Skips the empty field at end
			if (title.isEmpty() && percString.isEmpty()) {
				continue;
			}
			
			if (title.isEmpty()) {
				throw new IllegalArgumentException("Missing title for step!");
			}
			Double perc;
			try {
				perc = Double.parseDouble(percString);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("Step percentage missing!");
			}
			totalPercentage += perc;
			steps.add(new TemplateStep(title, perc / 100.0, i));
		}
		if (totalPercentage != 100.0) {
			throw new IllegalArgumentException("Your total % is not 100%...");
		}
		
		// By this point, all data is great!
		// TODO: Account for already done templates somehow!! - talk about this
		return new Assignment(titleText, due, new Template(_templatePicker.getSelectedItem().toString(), steps));
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
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(0, 0, 10, 0);
		Utils.themeComponent(pane);
		
		// Title label
		final JLabel titleLabel = new JLabel("Title: ");
		Utils.themeComponent(titleLabel);
		Utils.setFont(titleLabel, 14);
		c.gridx = 0;
		c.gridy = 0;
		pane.add(titleLabel, c);
		
		// Title field
		_titleField = new JTextField();
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(_titleField, c);
		
		// Time and Date label
		final JLabel timeDateLabel = new JLabel("Due Date: ");
		Utils.themeComponent(timeDateLabel);
		Utils.setFont(timeDateLabel, 14);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		pane.add(timeDateLabel, c);
		
		// Date and time spinner
		_dateTimeField = new JSpinner(new SpinnerDateModel());
		final JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(_dateTimeField, DATE_FORMAT_STRING);
		_dateTimeField.setEditor(timeEditor);
		_dateTimeField.setValue(new Date());
		Utils.themeComponent(_dateTimeField);
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(_dateTimeField, c);
		
		// Template label
		final JLabel templateLabel = new JLabel("Template: ");
		Utils.themeComponent(templateLabel);
		Utils.setFont(templateLabel, 14);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		pane.add(templateLabel, c);
		
		// Template picker or own - if they pick Custom (which must be an option), we know it's custom
		_templatePicker = new JComboBox<>(); // TODO: Actually fill it with template info
		_templatePicker.addItem(new Template("Custom"));
		_templatePicker.addItemListener(new ItemListener() {
			
			/**
			 * Updates steps
			 */
			@Override
			public void itemStateChanged(final ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					final ITemplate item = (ITemplate) event.getItem();
					_stepModel.clear();
					for (final ITemplateStep step : item.getAllSteps()) {
						_stepModel.addItem(step);
					}
					_stepModel.addBlankItem();
				}
			}
		});
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(_templatePicker, c);
		
		// Expected # hours to complete in total
		// TODO: Expected # hours???
		
		// Tasks label
		final JLabel taskLabel = new JLabel("Steps: ");
		Utils.themeComponent(taskLabel);
		Utils.setFont(taskLabel, 14);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		pane.add(taskLabel, c);
		
		// Tasks view
		final Object dataValues[][] = { { "", "" } };
		final String colNames[] = { "Step Name", "% of Total" };
		_stepModel = new StepModel(dataValues, colNames);
		_stepModel.addTableModelListener(this);
		_stepList = new StepViewTable(_stepModel);
		Utils.padComponent(_stepList, 10, 30);
		c.gridx = 1;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		final JTableHeader header = _stepList.getTableHeader();
		pane.add(header, c);
		c.gridx = 1;
		c.gridy = 4;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(_stepList, c);
		
		Utils.padComponent(pane, 20, 0);
		
		return pane;
	}
	
	/**
	 * For use when the steps list changes
	 */
	@Override
	public void tableChanged(final TableModelEvent e) {
		if (e.getLastRow() == _stepList.getRowCount() - 1) {
			_stepModel.addBlankItem();
		}
		_stepModel.deleteRowsIfEmpty(e.getFirstRow(), e.getLastRow());
		revalidate();
		this.repaint();
	}
	
	/**
	 * Creates a label representing the title of the dialog
	 * 
	 * @return a new JLabel for the title
	 */
	private static JLabel createDialogTitle() {
		final JLabel dialogTitle = new JLabel("Create New Assignment");
		Utils.setFont(dialogTitle, 22);
		dialogTitle.setOpaque(true);
		Utils.padComponent(dialogTitle, 0, 0, 15, 0);
		Utils.addBorderBottom(dialogTitle);
		Utils.padComponentWithBorder(dialogTitle, 0, 10);
		Utils.themeComponent(dialogTitle);
		return dialogTitle;
	}
	
	/**
	 * Clears the contents of all the fields
	 */
	private void clearContents() {
		_statusLabel.setText(DEFAULT_LABEL);
		_titleField.setText("");
		_dateTimeField.setValue(new Date());
		_stepModel.clear();
		_stepModel.addBlankItem();
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(360, 500);
	}
	
}