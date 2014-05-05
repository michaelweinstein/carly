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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import data.Assignment;
import data.ITemplate;
import data.ITemplateStep;
import data.Template;
import data.TemplateStep;
import frontend.Utils;
import frontend.app.GUIApp;
import frontend.view.CButton;
import frontend.view.ScrollablePanel;

/**
 * Class for the dialog box that pops up when adding a new assignment
 * 
 * @author dgattey
 */
public class AddAssignmentDialog extends JDialog implements TableModelListener {
	
	private static final String		DATE_FORMAT_STRING	= "MMMM dd, yyyy 'at' hh:mm a";
	private static final String		DEFAULT_LABEL		= " ";
	private static final long		serialVersionUID	= -5465413225077024401L;
	protected final GUIApp			_app;
	private CButton					_cancelButton;
	private CButton					_templateButton;
	protected CButton				_addButton;
	protected JTextField			_titleField;
	protected JSpinner				_dateTimeField;
	protected JLabel				_statusLabel;
	protected JComboBox<ITemplate>	_templatePicker;
	protected StepViewTable			_stepList;
	protected boolean				_saveTable			= false;
	protected StepModel				_stepModel;
	protected JTextField			_numHours;
	public JLabel					_dialogTitle;
	private String					_lastTemplateAdded;
	
	/**
	 * Constructor creates all relevant data
	 * 
	 * @param app the app in control
	 */
	public AddAssignmentDialog(final GUIApp app) {
		super();
		_app = app;
		
		// Keyboard shortcuts
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl S"), "save");
		getRootPane().getActionMap().put("save", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				addToDatabase();
			}
			
		});
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
		getRootPane().getActionMap().put("escape", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
				clearContents();
				dispose();
			}
			
		});
		
		Utils.themeComponent(this);
		Utils.themeComponent(getRootPane());
		Utils.padComponent(getRootPane(), 15, 15);
		setMinimumSize(getMinimumSize()); // Silly but required
		
		_dialogTitle = createDialogTitle();
		final JScrollPane scroller = new JScrollPane();
		final JPanel centerPane = createFieldsAndLabels(scroller);
		scroller.setViewportView(centerPane);
		final JPanel bottom = createButtonsAndStatusPane();
		
		// Addition of all items to dialog
		add(_dialogTitle, BorderLayout.NORTH);
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
		_statusLabel.setFont(Utils.getFont(Font.PLAIN, 12));
		
		// Simply deletes contents and closes
		_cancelButton = new CButton("Cancel");
		_cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
				clearContents();
				dispose();
			}
			
		});
		
		// Adds a new assignment through the hub
		_addButton = new CButton("Add");
		_addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				addToDatabase();
			}
			
		});
		
		// Saves the template
		_templateButton = new CButton("Save Template");
		_templateButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				addTemplateToDatabase();
			}
		});
		_templateButton.setEnabled(false);
		
		final JPanel pane = new JPanel(new GridLayout(2, 1));
		pane.setAlignmentX(SwingConstants.CENTER);
		pane.setAlignmentY(SwingConstants.CENTER);
		final JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		buttons.add(_templateButton);
		buttons.add(Box.createHorizontalStrut(5));
		buttons.add(_cancelButton);
		buttons.add(Box.createHorizontalStrut(5));
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
	 * Adds the template represented by the custom template to database
	 */
	protected void addTemplateToDatabase() {
		// Error checking on title of template
		final String titleTemplate = _titleField.getText();
		if (titleTemplate == null || titleTemplate.isEmpty()) {
			_statusLabel.setText("Couldn't save: need a name to create");
			return;
		}
		
		/**
		 * If the last template string is null, get by name, otherwise get by id so as not to add the same thing with a
		 * slightly different name
		 */
		ITemplate t = (_lastTemplateAdded == null) ? StorageService.getTemplateByName(titleTemplate) : StorageService
				.getTemplate(_lastTemplateAdded);
		try {
			if (t == null) {
				
				// Template doesn't exist, so create it, add all steps, and add to database
				t = new Template(titleTemplate);
				updateSteps(t);
				StorageService.addTemplate(t);
			} else {
				// Template already exists, so update the name, add all steps, and update in database
				t.setTitle(titleTemplate);
				updateSteps(t);
				StorageService.updateTemplate(t);
			}
		} catch (final StorageServiceException | IllegalArgumentException e) {
			_statusLabel.setText("Couldn't save: " + e.getMessage());
			_lastTemplateAdded = null;
			return;
		}
		
		// At this point, all is well so set status and save ID
		_lastTemplateAdded = t.getID();
		_statusLabel.setText(String.format("Template saved with title \"%s\"", titleTemplate.length() > 20
				? titleTemplate.substring(0, 20) + "..." : titleTemplate));
	}
	
	/**
	 * Takes a template, clears its steps, and updates from the step list
	 * 
	 * @param t a given template
	 * @throws IllegalArgumentException a message if something parsed wrong
	 */
	private void updateSteps(final ITemplate t) {
		t.clearSteps();
		double total = 0;
		for (int i = 0; i < _stepModel.getRowCount() - 1; i++) { // -1 handles last blank row
			final String stepName = _stepModel.getValueAt(i, 0).toString();
			
			// Error checking on name
			if (stepName == null || stepName.isEmpty()) {
				throw new IllegalArgumentException("step name not a string");
			}
			double stepPercent;
			try {
				final String s = _stepModel.getValueAt(i, 1).toString();
				if (s == null || s.isEmpty()) {
					throw new IllegalArgumentException("percent of total not a number");
				}
				stepPercent = Double.parseDouble(s);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("percent of total not a number");
			}
			total += stepPercent / 100.0;
			t.addStep(new TemplateStep(stepName, stepPercent / 100.0, i));
		}
		if (total < 0.999999999 || total > 1.0000000001) {
			throw new IllegalArgumentException("total percent doesn't sum to 100");
		}
	}
	
	/**
	 * Adds the given assignment to database
	 */
	protected void addToDatabase() {
		try {
			final Assignment a = parseFields();
			HubController.addAssignmentToCalendar(a);
			clearContents();
			dispose();
		} catch (final IllegalArgumentException e1) {
			_statusLabel.setText("Oops! " + e1.getMessage());
		}
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
			throw new IllegalArgumentException("Your total task % is not 100%...");
		}
		
		// Get the ID of the template and make a series of steps
		ITemplate t = StorageService.getTemplate(((ITemplate) _templatePicker.getSelectedItem()).getID());
		if (t == null && _lastTemplateAdded != null) {
			t = StorageService.getTemplate(_lastTemplateAdded);
		}
		if (t == null) {
			t = new Template("Custom");
			final Set<String> stepNames = new HashSet<>();
			
			for (final ITemplateStep st : steps) {
				if (stepNames.contains(st.getName())) {
					throw new IllegalArgumentException("Tasks can't have duplicate names!");
				}
				stepNames.add(st.getName());
				t.addStep(st);
			}
		}
		
		// Get expected num hours
		double exHours;
		try {
			exHours = Double.parseDouble(_numHours.getText());
			if (exHours < 0) {
				throw new IllegalArgumentException("Your number of hours is negative.");
			}
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Your number of hours is not a valid number.");
		}
		return new Assignment(titleText, due, t, exHours);
	}
	
	/**
	 * Creates all fields and labels in a center panel
	 * 
	 * @param scroller the scroll panel to encapsulate the result
	 * @return a new JPanel for the center full of labels and fields
	 */
	private JPanel createFieldsAndLabels(final JScrollPane scroller) {
		final JPanel pane = new ScrollablePanel(scroller);
		final GridBagConstraints c = new GridBagConstraints();
		pane.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
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
		_titleField.setColumns(6);
		c.weightx = 1;
		c.gridx = 1;
		c.gridwidth = 1;
		pane.add(_titleField, c);
		
		// Time and Date label
		final JLabel timeDateLabel = new JLabel("Due Date: ");
		Utils.themeComponent(timeDateLabel);
		Utils.setFont(timeDateLabel, 14);
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridwidth = 1;
		pane.add(timeDateLabel, c);
		
		// Date and time spinner
		_dateTimeField = new JSpinner(new SpinnerDateModel());
		final JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(_dateTimeField, DATE_FORMAT_STRING);
		_dateTimeField.setEditor(timeEditor);
		_dateTimeField.setValue(new Date(new Date().getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)));
		Utils.themeComponent(_dateTimeField);
		c.weightx = 1;
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(_dateTimeField, c);
		
		// Expected # hours to complete in total
		final JLabel exLabel = new JLabel("Expected Hours: ");
		exLabel.setToolTipText("The total number of hours you think it will take you to complete the assignment");
		Utils.themeComponent(exLabel);
		Utils.setFont(exLabel, 14);
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 0;
		c.gridx = 0;
		c.gridwidth = 1;
		pane.add(exLabel, c);
		
		// Expected field
		_numHours = new JTextField();
		_numHours.setColumns(6);
		c.weightx = 1;
		c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(_numHours, c);
		
		// Template label
		final JLabel templateLabel = new JLabel("Template: ");
		Utils.themeComponent(templateLabel);
		Utils.setFont(templateLabel, 14);
		c.weightx = 0;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridwidth = 1;
		pane.add(templateLabel, c);
		
		// Template picker or own - if they pick Custom (which must be an option), we know it's custom
		_templatePicker = new JComboBox<>();
		_templatePicker.addItemListener(new ItemListener() {
			
			/**
			 * Updates steps
			 */
			@Override
			public void itemStateChanged(final ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED && !_saveTable) {
					final ITemplate item = (ITemplate) event.getItem();
					_stepModel.clear();
					for (final ITemplateStep step : item.getAllSteps()) {
						_stepModel.addItem(step);
					}
					_templateButton.setEnabled(false);
					if (event.getItem().toString().equals("Custom")) {
						_stepModel.addItem(new TemplateStep("Part 1", 1.0));
						_templateButton.setEnabled(true);
					}
					_stepModel.addBlankItem();
					_stepList.revalidate();
					_stepList.repaint();
				}
			}
		});
		c.gridx = 1;
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(_templatePicker, c);
		
		// Tasks label
		final JLabel taskLabel = new JLabel("Steps: ");
		Utils.themeComponent(taskLabel);
		Utils.setFont(taskLabel, 14);
		c.gridx = 0;
		c.weightx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridwidth = 1;
		pane.add(taskLabel, c);
		
		// Tasks view
		final Object dataValues[][] = { { "", "" } };
		final String colNames[] = { "Step Name", "% of Total" };
		_stepModel = new StepModel(dataValues, colNames);
		_stepModel.addTableModelListener(this);
		_stepList = new StepViewTable(_stepModel, false);
		Utils.padComponent(_stepList, 10, 30);
		c.gridx = 1;
		c.weightx = 1;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		final JTableHeader header = _stepList.getTableHeader();
		pane.add(header, c);
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;
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
		if (_templatePicker.getSelectedIndex() != 0) {
			_saveTable = true;
			_templatePicker.setSelectedIndex(0);
			_templateButton.setEnabled(true);
			_saveTable = false;
		}
		revalidate();
		repaint();
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
	protected void clearContents() {
		_statusLabel.setText(DEFAULT_LABEL);
		_lastTemplateAdded = null;
		_titleField.setText("");
		_numHours.setText("");
		_dateTimeField.setValue(new Date(new Date().getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)));
		_stepModel.clear();
		_stepModel.addItem(new TemplateStep("Part 1", 1.0));
		_stepModel.addBlankItem();
		_cancelButton.reset();
		_addButton.reset();
		_templateButton.reset();
	}
	
	@Override
	public void setVisible(final boolean b) {
		_templatePicker.removeAllItems();
		_templatePicker.addItem(new Template("Custom"));
		final List<ITemplate> temps = StorageService.getAllTemplates();
		if (temps != null) {
			for (final ITemplate temp : temps) {
				if (!temp.getName().equals("Custom")) {
					_templatePicker.addItem(temp);
				}
			}
		}
		super.setVisible(b);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(400, 500);
	}
	
}