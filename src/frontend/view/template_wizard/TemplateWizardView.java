package frontend.view.template_wizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

import data.ITemplate;
import data.ITemplateStep;
import data.Template;
import data.TemplateStep;
import frontend.Utils;
import frontend.view.assignments.StepModel;
import frontend.view.assignments.StepViewTable;

/**
 * This is the front-end class for the Template Wizard panel that is on the Settings page (not the Dialogue View).
 * 
 * @author miweinst
 */

public class TemplateWizardView extends JPanel {
	
	private static final long	serialVersionUID	= 4215933185975151935L;
	
	/* Styling Vars */
//	private static final int	title_size			= 19;
	private static final int	padding				= 10;
	// Button colors
	private static final Color	submitNewColor 		= Color.RED;
	private static final Color 	submitUpdatedColor	= Color.BLUE;
	
	/* User Input Label Strings */
	private static final String new_template 		= "Custom";
	private static final String edit_template		= "Edit";
	private static final String hide_template 		= "Hide";
	private static final String template_name 		= "Name";
	private static final String template_hours 		= "Consecutive hours you want to work";
	private static final String submit_new_template = "Submit template";
	private static final String submit_updated_template = "Update template";
	// Steps Panel
	private static final String step_name 			= "Step name";
	private static final String step_percent 		= "% of total";
	private static final String steps_list 			= "Steps";
	
	/* Input Elements */
	private final JComboBox<ITemplate> _templatePicker;
	private StepModel _tableModel;
	// Top Buttons ('Hide', 'Show') 
	private final JButton _editBtn = new JButton(edit_template);
	private final JButton _hideBtn = new JButton(hide_template);
	// Panels (Show/Hide block) 
	private JPanel _namePanel = new JPanel();
	private JPanel _hoursPanel = new JPanel();
	private JPanel _stepPanel = new JPanel();
	private final JButton _submitTemplateBtn = new JButton(submit_updated_template);
	
	// Easy access to TextComponent input fields ('name', 'preferred consecutive hours')
	private Map<String, JTextComponent> _inputMap = new HashMap<>();
	
	/**
	 * Constructor creates all input fields and labels, and 
	 * action listeners for buttons as in-line anonymous classes.
	 * 
	 * @param settings
	 */
	public TemplateWizardView() {
		super();
		// Set theme and layout of wizard
		Utils.themeComponent(this);
		Utils.addBorderTop(this);
		Utils.addBorderBottom(this);
		Utils.padComponentWithBorder(this, padding, padding);
				
		// TODO: Removing Templates -- user needs a way to delete an existing Template!
		
		// ====== Instantiating Elements ======

		// Size of Edit/Hide buttons
		Dimension btnSize = new Dimension(45, 20);	
		
		/* 'Edit' button (Listener below) */
		_editBtn.setFocusPainted(false);
		_editBtn.setPreferredSize(btnSize);
		_editBtn.setVisible(true);
		
		/* 'Hide' button (Listener below) */
		_hideBtn.setFocusPainted(false);
		_hideBtn.setPreferredSize(btnSize);
		_hideBtn.setEnabled(false);	
		_hideBtn.setVisible(true);
		
		/* JComboBox TemplatePicker (ItemListener below) */
		// Populate with any existing templates
		_templatePicker = new JComboBox<>(TemplateDelegate.getExistingTemplates());
		// If no existing Templates in database, _templatePicker starts on 'Custom'
		boolean custom = (_templatePicker.getItemCount() == 0);
		// Add empty Template with name 'Custom' (never actually submitted)
		_templatePicker.addItem(new Template(new_template));
		
		/* Name: (code in newNamePanel()) */
		_namePanel = newNamePanel(template_name);
		
		/* Consecutive Hours: (code in newNumberPanel()) */
		_hoursPanel = newNumberPanel(template_hours);
	
		/* Steps table (code in createStepsPanel()) */
		_stepPanel = createStepsPanel();
		_stepPanel.setVisible(false);
		
		/* "Submit/Update template" button */
		// Boolean show indicates whether starting on 'Custom' template 
		_submitTemplateBtn.setText(custom? submit_new_template : submit_updated_template);
		_submitTemplateBtn.setForeground(custom? submitNewColor : submitUpdatedColor);
		_submitTemplateBtn.setFocusPainted(false);
		_submitTemplateBtn.setPreferredSize(new Dimension(300, 30));
		_submitTemplateBtn.setVisible(false);
		
		// Sets visibility true if _templatePicker starts on 'Custom'
		toggleVisibility(custom);

	// ====== End of Instantiating Elements ======
	
	// ============= Listeners =============
		
		/* --- 'Submit template' Listener === */
		_submitTemplateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// Verify: template data is valid
				if (verifyTemplateData()) {
					// Store data to instantiate new Template with
					String name = _inputMap.get(template_name).getText();
					double hours = Double.parseDouble(_inputMap.get(template_hours).getText());
					List<ITemplateStep> currSteps = getStepsFromTable();
					// Verify: steps of new template are valid
					if (verifyStepData(currSteps)) {
						// Submits Template with new data to StorageService
						submitTemplateToDatabase(name, hours, currSteps);
						// Hides all elements
						toggleVisibility(false);
					}
				}
			}
		});

		/* --- 'Edit' Listener */
		_editBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// Populate fields with selected template data before making visible
				populateFields();		
				// Dislays all elements		
				toggleVisibility(true);
				// Repaint panel
				TemplateWizardView.this.repaint();
			}
		});
		
		/* --- 'Hide' Listener --- */
		_hideBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// Hides all elements
				toggleVisibility(false);
				// If 'Hide' is called on 'Custom' Template
				if (_templatePicker.getSelectedIndex() == _templatePicker.getItemCount()-1) {
					// Set selected index to 0
					_templatePicker.setSelectedIndex(0);
				}
				TemplateWizardView.this.repaint();
			}
		});
		
		/* --- JComboBox Listener --- */
		_templatePicker.addItemListener(new ItemListener() {
			// TODO: Bug: it sometimes doesn't register when you select an item!!
			@Override
			public void itemStateChanged(ItemEvent e) {
				ITemplate t = (ITemplate) _templatePicker.getSelectedItem();				
				// Populate fields data based on current selected item
				populateFields();			
				// 'Custom' Item listener
				if (t.getName().equals(new_template)) {
					// Text: "Submit new template", for custom template
					_submitTemplateBtn.setText(submit_new_template);
					_submitTemplateBtn.setForeground(submitNewColor);
					// Display all elements
					toggleVisibility(true);
				}
				else {
					// Text: "Submit updated template", for existing template
					_submitTemplateBtn.setText(submit_updated_template);
					_submitTemplateBtn.setForeground(submitUpdatedColor);
				}
				// Repaint wizard panel
				TemplateWizardView.this.repaint();
			}	
		});
		
	// ============= End of Listeners =============
		
	// === Adding Elements ===	
		this.add(_editBtn);
		this.add(_hideBtn);	
		this.add(_templatePicker);	
		/* Panels */
		this.add(_namePanel);
		this.add(_hoursPanel);
		this.add(_stepPanel);
		this.add(_submitTemplateBtn);
	// === End of Adding Elements ===
	}
	
	
// ============ START Visibility Methods ===================
	
	/**
	 * Called on 'Edit'/'Hide' buttons, on 'Custom'
	 * Template selection, etc...
	 * Shows/Hides all four UI input elements:
	 * name panel, hours panel, steps table
	 * and "Submit/Update Template" button.
	 * Disables/Enables 'Edit'/'Hide; buttons according
	 * to whether elements are visible or not, because
	 * one of the button's functionality will be pointless.
	 * 
	 * @param show, setVisible(show)
	 */
	private void toggleVisibility(boolean show) {
		// Show/hide four elements
		_namePanel.setVisible(show);
		_hoursPanel.setVisible(show);
		_submitTemplateBtn.setVisible(show);
		_stepPanel.setVisible(show);		
		// Disable 'Edit' when showing, enable when hidden
		_editBtn.setEnabled(!show);			
		_hideBtn.setEnabled(show);
	}

// ============ END Visibility Methods ====================
		
	
// ============ START Create Panel Methods ==================
	/**
	 * Creates and returns a JPanel with JTable
	 * for user to input and view the TemplateSteps
	 * of a Template. Includes 'Steps' label, 
	 * 'Step Name, % total' header, and rows of steps. 
	 * Adds an empty row to the end if one is not there.
	 * 
	 * @return JPanel ( {steps, % of total} list )
	 */
	private JPanel createStepsPanel() {
		JPanel innerPanel = new JPanel();
		// Create Layout and Constraints, main components
		final GridBagConstraints c = new GridBagConstraints();
		innerPanel.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(0, 0, 10, 0);
		Utils.themeComponent(innerPanel);
		
		// Create TableModel
		final Object dataValues[][] = { { "", ""} };
		final String colNames[] = {step_name, step_percent};
		// Create TableModel to pass into StepViewTable
		final StepModel model = new StepModel(dataValues, colNames);
		// Step data verified automatically
		model.addTableModelListener(new TableModelListener() {
			// --- Step Table Listener ---
			@Override
			public void tableChanged(TableModelEvent e) {
				// Create new row if last row is entirely filled
				if (e.getLastRow() == model.getRowCount() -1) 
					if (model.getRowAt(e.getLastRow()).length == 2) 
						model.addBlankItem();
				// Remove any empty rows
				model.deleteRowsIfEmpty(e.getFirstRow(), e.getLastRow());
				// TemplateWizardView panel calls
				revalidate();
				TemplateWizardView.this.repaint();
			}
			// --- End of Step Table Listener ---
		});
		// Assign _tableModel 
		_tableModel = model;
		
		// 'Steps: ' label
		final JLabel stepLabel = new JLabel(steps_list + ": ");
		Utils.themeComponent(stepLabel);
		Utils.padComponent(stepLabel, 11, 5);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		innerPanel.add(stepLabel, c);
		
		// TODO: Table needs to have max height and start scrolling
		
		// Instantiate StepViewTable 
		final StepViewTable stepTable = new StepViewTable(model);		
		// Create Header
		final JTableHeader header = stepTable.getTableHeader();
		// Style Header
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		innerPanel.add(header, c);
		// Style StepViewTable list
		Utils.padComponent(stepTable, 10, 30);
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.gridwidth = GridBagConstraints.REMAINDER;
		innerPanel.add(stepTable, c);

		return innerPanel;
	}
	
	/**
	 * Factored out code to create a panel with
	 * a label and text field for inputting the
	 * name. Called for both Template Name and
	 * the TemplateStep Name.
	 * @param name
	 * @return
	 */
	private JPanel newNamePanel(String name) {
		final JPanel namePanel = new JPanel();
		Utils.themeComponent(namePanel);
		namePanel.setPreferredSize(new Dimension(300, 25));		
		final JLabel nameLabel = new JLabel(name + ": ");
		Utils.themeComponentInverse(nameLabel);
		final JTextField nameInput = new JTextField();
		nameInput.setPreferredSize(new Dimension(100, 20));
		namePanel.add(nameLabel);
		namePanel.add(nameInput);
		namePanel.setVisible(false);
		// Add to input map
		_inputMap.put(name, nameInput);
		return namePanel;
	}
	
	private JPanel newNumberPanel(String name) {
		final JPanel hoursPanel = new JPanel();
		Utils.themeComponent(hoursPanel);
		hoursPanel.setPreferredSize(new Dimension(350, 25));
		final JLabel hoursLabel = new JLabel(name + ": ");
		Utils.themeComponentInverse(hoursLabel);
		final JTextField hoursInput = new JTextField();
		hoursInput.setPreferredSize(new Dimension(40, 20));
		hoursPanel.add(hoursLabel);
		hoursPanel.add(hoursInput);
		hoursPanel.setVisible(false);
		// Add to input map
		_inputMap.put(name, hoursInput);
		return hoursPanel;
	}
// ============ END Create Panel Methods ==================
	
	
// ============ START Data Structures Methods =============
	
	/**
	 * Returns a List of TemplateSteps generated from current
	 * data in StepViewTable _tableModel.
	 * 
	 * @return List of Steps created from current TableModel
	 */
	private List<ITemplateStep> getStepsFromTable() {
		List<ITemplateStep> currSteps = new ArrayList<>();
		for (int i=0; i<_tableModel.getRowCount(); i++) {
			Object row[] = _tableModel.getRowAt(i);						
			if (!row[0].toString().isEmpty() && !row[1].toString().isEmpty()) {
				// Creates ITemplateStep from Object arr[2] {name, %}
				ITemplateStep s = createStepFromRow(row);
				// Set stepNumber to row number
				s.setStepNumber(i);
				// Add to List
				currSteps.add(s);
			}
		}
		return currSteps;
	}

	/**
	 * Gets Template currently selected in JComboBox _templatePicker.
	 * Populates the 'Name' field with template.getName().
	 * Populates the 'Hours' field with template.getPreferredConsecutiveHours().
	 * Clears then populates Step View Table with elements in template.getAllSteps().
	 */
	private void populateFields() {
		// Populate based on data from current selected Template
		ITemplate t = (ITemplate) _templatePicker.getSelectedItem();
		// Set name and preferred consecutive hours fields
		String name = t.getName();
		String hours = t.getPreferredConsecutiveHours() + "";
		boolean custom = name.equals(new_template);
		// If 'Custom Template', set fields to blank
		if (custom) {
			name = "";
			hours = "";
		}
		// Set 'Name', 'Hours' fields using _inputMap
		_inputMap.get(template_name).setText(name);
		_inputMap.get(template_hours).setText(hours);

		// Load template data into StepsViewTable; clear all if 'Custom'
		populateStepsViewTable(t, custom);
	}
	
	/**
	 * Populates StepsViewTable with data from specified ITemplate t
	 * in _templatePicker JComboBox. First clear _currSteps List, which is the
	 * List of ITemplateSteps that gets passed to new Template on submit. Also
	 * clears all data from table on resetStepsViewTable, to repopulate from scratch.
	 * If clear is false, it repopulates _currSteps and StepsTableModel with each
	 * step from specified ITemplate t. Then alerts listener that rows have been updated.
	 * Listener automatically deletes empty rows and adds empty row at end.
	 * 
	 * @param ITemplate t, boolean clear
	 */
	private void populateStepsViewTable(ITemplate t, boolean clear) {
		// Clear StepsViewTable for repopulation
		resetStepsViewTable();
		if (_tableModel != null) {
			// Get steps from currently selected Template
			List<ITemplateStep> allSteps = t.getAllSteps();
			// Repopulate with current template data
			if (!clear) {
				// Fill _tableModel data with current data
				int numSteps = allSteps.size();
				for (int i=0; i<numSteps; i++) {
					ITemplateStep s = allSteps.get(i);
					// Populate row with each step
					_tableModel.setValueAt(s.getName(), i, 0);
					_tableModel.setValueAt(s.getPercentOfTotal()*100, i, 1);
				}
				// Fires UDPATE Event: Rows [0, # steps] updated
				_tableModel.fireTableRowsUpdated(0, /*numSteps-1*/ _tableModel.getRowCount()-1);
			}
		}
	}
	
	/**
	 * Clears data from StepsViewTable. Iteratively sets values
	 * to empty strings. Modifies first row (row 0) only, because
	 * the TableModelListener automatically removes empty rows. At
	 * each iteration, removes data from first row and then listener
	 * removes row and shifts other rows up by one. Note: Must have empty row
	 * deletion in TableModelListener for this method to work!
	 * This method is called every time StepsViewTable repopulates.
	 */
	private void resetStepsViewTable() {		
		// Store count value because _tableModel.getRowCount changes during for-loop iterations
		int numRow = _tableModel.getRowCount()-1;
		// For each row in original row count (before modifications)
		for (int i=0; i<numRow; i++) {
			// Clears first row at each iteration (listener removes empty row and shifts the rest)
			_tableModel.setValueAt("", 0, 0);
			_tableModel.setValueAt("", 0, 1);
			// Trigger TableModelListener to remove empty rows (not necessary, but safety net)
			_tableModel.fireTableRowsUpdated(0, 0);
		}
	}
	
	/**
	 * Creates a TemplateStep from a row array in the StepViewTable,
	 * where arr[0] is String name and arr[1] is Double percent of total.
	 * Row passed in as Object array.
	 * 
	 * @param arr Object[name (String), % of total (Double)]
	 * @return new TemplateStep(name, percentOfTotal)
	 */
	private static TemplateStep createStepFromRow(Object arr[]) {
		if (arr.length == 2) 
			if (arr[0] instanceof String && arr[1] instanceof Double) 
				return new TemplateStep((String)arr[0], ((Double)arr[1])/100.0);
		return null;
	}
	
	/**
	 * Submits Template with specified data to the StorageService,
	 * via the TemplateDelegate. Submits Template according to the
	 * item currently selected in JComboBox _templatePicker.
	 * If 'Custom' template, instantiates Template from scratch with 
	 * new data, sends to StorageService and adds to end of JComboBox.
	 * If Existing template, instantiates Template with ID of selected 
	 * Template, sends to StorageService as an 'update', and replaces
	 * selected Template with new Template in JComboBox.
	 * 
	 * @param name, name of template to submit
	 * @param hours, preferredConsecutiveHours of template to submit
	 * @param currSteps, TemplateSteps List of template to submit
	 * @param currIndex, currIndex of _templatePicker
	 */
	private void submitTemplateToDatabase(String name, double hours, List<ITemplateStep> currSteps) {
		// Determine item in _templatePicker user is submitting data for
		int currIndex = _templatePicker.getSelectedIndex();
		// Case: 'Custom' template, add -- (Custom always last element in ComboBox)
		if (currIndex == _templatePicker.getItemCount()-1) {
			// Instantiate new Template based on name, hours, steps data
			Template t = new Template(name, hours, currSteps);
			// Add Template to StorageService and local list
			TemplateDelegate.addTemplate(t);
			// Add item to _templatePicker JComboBox
			addTemplateToPicker(t);
		}
		// Case: Existing template, update
		else {							
			// Get Template to update (curr selected item on submit)
			ITemplate existingTemp = _templatePicker.getItemAt(currIndex);	
			// Instantiate new Template with same ID
			ITemplate t = new Template(existingTemp.getID(), name, 
					currSteps, hours);
			// Update template in StorageService
			TemplateDelegate.updateTemplate(t, existingTemp);
			// Replaces old Template at same index in _templatePicker JComboBox
			replaceTemplateInPickerAt(t, currIndex);
		}
	}
	
	/**
	 * Inserts the specified ITemplate object to JComboBox
	 * at second-to-last index (last index reserved for 'Custom').
	 * Then sets currently selected item to newly added Template.
	 * 
	 * @param t, ITemplate to add and set selected
	 */
	private void addTemplateToPicker(ITemplate t) {
		int index = _templatePicker.getItemCount()-1;
		_templatePicker.insertItemAt(t, index);
		_templatePicker.setSelectedIndex(index);
	}
	
	/**
	 * Replaces the ITemplate stored at specified index
	 * in JComboBox _templatePicker. First removes old
	 * item at that index. Then inserts new ITemplate t
	 * to that index, and sets selected index to newly
	 * added item.
	 * 
	 * @param t, Template to add
	 * @param index of Template to replace
	 */
	private void replaceTemplateInPickerAt(ITemplate t, int index) {
		_templatePicker.removeItemAt(index);
		_templatePicker.insertItemAt(t, index);
		_templatePicker.setSelectedIndex(index);
	}
	
// ============= END Data Structures Methods ==============	
	
	
// ============ START Verify Data Methods ==================
	
	/**
	 * Alerts the user of a message or error
	 * in console.
	 * 
	 * @param message, String to print
	 */
	private void alertUser(String message) {
		// TODO Find a way to alert user in GUI?
		System.out.println("User: " + message);
	}

	/**
	 * Verifies that all user input fields have valid input, and that no
	 * mandatory fields have been left blank -- returns true. 
	 * Alerts user if a field is invalid or blank, and returns false.
	 * Displays specific error messages using alertUser calls.
	 * Does NOT verify StepViewTable data (optimization so that
	 * getStepsFromTable does not have to be called twice on Submit).
	 * 
	 * @return true if all fields valid, else false
	 */
	private boolean verifyTemplateData() {
		// Set up validity booleans
		boolean validName = false;
		boolean validHours = false;
		// Check name
		if (_inputMap.containsKey(template_name)) {
			// TODO: Check that String contains characters
			String name = _inputMap.get(template_name).getText();
			// Is not empty string
			if (!name.isEmpty()) {
				validName = true;
			}
			else
				alertUser("Invalid Input: " + "Please enter a name for the template.");
		}
		// Check consecutive hours
		if (_inputMap.containsKey(template_hours)) {
			// TODO: Check that String contains characters
			String hours = _inputMap.get(template_hours).getText();		
			if (!hours.isEmpty()) {
				// Is valid format; can be parsed as double
				try {
					double h = Double.parseDouble(hours);
					// Is positive number of hours
					if (h > 0) 				
						validHours = true;
					else 
						alertUser("Invalid Input: " + 
								"Please enter a positive number in consecutive hours field.");
				} catch (NumberFormatException e) {
					validHours = false;
					alertUser("Invalid Input: Please enter a valid number in consecutive hours field.");
				}
			}
			else 
				alertUser("Invalid Input: Please enter preferred number " + 
						"of consecutive hours you would like to work on template.");
		}
		return validName && validHours;
	}
	
	/**
	 * Verifies TemplateStep data in specified List steps, which should be
	 * generated from getStepsFromTable. Checks that step data exists.
	 * Has List parameter so getStepsFromTable does not need to be called twice. 
	 * 
	 * @param steps, List of TemplateSteps from StepViewTable
	 * @return boolean, whether or not step data is valid
	 */
	private boolean verifyStepData(List<ITemplateStep> steps) {		
		// Check that steps not empty
		if (steps.isEmpty()) {
			alertUser("Invalid Input: Please add Steps to this Template");
			return false;
		}
		
		// TODO: Check that fields contain characters (i.e. full of spaces)
		
		// Check that % of totals add up to 100%
		List<ITemplateStep> currSteps = getStepsFromTable();
		double total = 0;
		// Tabulate total %
		for (ITemplateStep s: currSteps) 
			total += s.getPercentOfTotal();
		// If less than 100%
		if (total < 1.0) {
			alertUser("Invalid Input: Step percents sum to less than 100%");
			return false;
		} 
		// If greater than 100%
		else if (total > 1.0) {
			alertUser("Invalid Input: Step percents sum to over 100%");
			return false;
		}
		
		return true;
	}
	
// ============ END Verify Data Methods ==================
}
