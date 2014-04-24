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
import frontend.view.SettingsView;
import frontend.view.assignments.StepModel;
import frontend.view.assignments.StepViewTable;

/**
 * This is the front-end class for the Template Wizard panel that is on the Settings page (not the Dialogue View).
 * 
 * @author miweinst
 */

public class TemplateWizardView extends JPanel {
	
	private static final long	serialVersionUID	= 4215933185975151935L;
	
//	private static final int	title_size			= 19;
	private static final int	padding				= 10;
	
	/* User Input Label Strings */
	private static final String new_template 		= "Custom";
	private static final String edit_template		= "Edit";
	private static final String hide_template 		= "Hide";
	private static final String template_name 		= "Name";
	private static final String template_hours 		= "Consecutive hours you want to work";
	private static final String submit_template 	= "Submit template";	
	// Steps Panel
	private static final String step_name 			= "Step name";
	private static final String step_percent 		= "% of total";
	private static final String steps_list 			= "Steps";
	
	// Easy access to input fields, getText inputted by user
	private Map<String, JTextComponent> _inputMap = new HashMap<>();
	// Stores TemplateSteps inputted by user, passes in to new Template on submit
//	private List<ITemplateStep> _currSteps = new ArrayList<>();

	/* Data structures */
	private final JComboBox<ITemplate> _templatePicker;
	/* Inputs */
	private StepModel _tableModel;
	/* Top Buttons ('Hide', 'Show') */
	private final JButton _editBtn = new JButton(edit_template);
	private final JButton _hideBtn = new JButton(hide_template);
	/* Panels (Show/Hide block) */
	private JPanel _namePanel = new JPanel();
	private JPanel _hoursPanel = new JPanel();
	private JPanel _stepPanel = new JPanel();
	private final JButton _submitTemplateBtn = new JButton(submit_template);
	
	
	
	/**
	 * Constructor creates all input fields and labels, and 
	 * action listeners for buttons as in-line anonymous classes.
	 * 
	 * @param settings
	 */
	public TemplateWizardView(final SettingsView settings) {
		super();
		// Set theme and layout of wizard
		Utils.themeComponent(this);
		Utils.addBorderFull(this);
		Utils.padComponentWithBorder(this, padding, padding);
				
		// Construct list of current Templates in db
		new TemplateList();		// all methods static
		
		// Size of 'Edit' and 'Hide' buttons
		Dimension btnSize = new Dimension(45, 20);
		
	// ====== Instantiating Elements ======
		
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
		boolean show = false;
		_templatePicker = new JComboBox<>(TemplateList.getAllTemplates());
		if (_templatePicker.getItemCount() == 0) 
			show = true;
		_templatePicker.addItem(new Template(new_template));
		
		/* Name: (code in newNamePanel()) */
		_namePanel = newNamePanel(template_name);
		
		/* Consecutive Hours: (code in newNumberPanel()) */
		_hoursPanel = newNumberPanel(template_hours);
	
		/* Steps table (code in createStepsPanel()) */
		_stepPanel = createStepsPanel();
		_stepPanel.setVisible(false);
		
		/* "Submit template" button */
//		final JButton submitTemplateBtn = new JButton(submit_template);
		_submitTemplateBtn.setForeground(Color.RED);
		_submitTemplateBtn.setFocusPainted(false);
		_submitTemplateBtn.setPreferredSize(new Dimension(300, 30));
		_submitTemplateBtn.setVisible(false);
		
	// ====== End of Instantiating Elements ======
		// Start on 'Custom', make visible
		if (show) 
			showAll();
	// ============= Listeners =============
		
		/* --- 'Submit template' Listener === */
		_submitTemplateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// Verify form data
				boolean verified = verifyTemplateData();	
				if (verified) {
					// Create and store new instance of Template
					String name = _inputMap.get(template_name).getText();
					double hours = Double.parseDouble(_inputMap.get(template_hours).getText());
					
					
					// Get all TemplateSteps from table and pass list to new Template
					List<ITemplateStep> currSteps = getStepsFromTable();
					// Check that Steps are not empty, and valid
					if (verifyStepData(currSteps)) {
						// Instantiate Template based on name, hours, steps data
						Template t = new Template(name, hours, currSteps);
											
						// 'Custom' template, addTemplate (Custom always last element in ComboBox)
						if (_templatePicker.getSelectedIndex() == _templatePicker.getItemCount()-1) {								
							// Add Template to StorageService and local list
							TemplateList.addTemplate(t);
							// Add item to JComboBox
							addItemToTemplatePicker(t);
						}
						// Pre-Existing template, updateTemplate
						else {
/////////////////////
							// TODO: Remove this block and printline
							// 	TEMPORARY
							TemplateList.addTemplate(t);
							addItemToTemplatePicker(t);
/////////////
							System.out.println("TEMPORARILY Adds new Template instead of " + 
									"updating Existing Template, still debugging. " + 
									"(TemplateWizard().submitBtn.ActionListener)");
/////////////////^^^^^^
							
							// TODO: Update existing template
								//////Getting errors on TemplateList.updateTemplate()
							
/*							// Update template in StorageService
							TemplateList.updateTemplate(t);
							// Remove current item from _templatePicker JComboBox
							_templatePicker.removeItem(_templatePicker.getSelectedIndex());
							// Add updated Template to _templatePicker JComboBox
							addItemToTemplatePicker(t);*/
						}
						// Hides all elements
						hideAll();
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
				showAll();
				// Repaint panel
				TemplateWizardView.this.repaint();
			}
		});
		
		/* --- 'Hide' Listener --- */
		_hideBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				hideAll();
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
			@Override
			public void itemStateChanged(ItemEvent e) {
				ITemplate t = (ITemplate) _templatePicker.getSelectedItem();				
				// Populate fields data based on current selected item
				populateFields();			
				// 'Custom' Item listener
				if (t.getName().equals(new_template)) 
					showAll();
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
	 * Called on 'Edit' button, and on 'Custom'
	 * Template selected from _templatePicker etc...
	 * Shows all template field elements:
	 * name panel, hours panel, steps table
	 * and "Submit Template" button.
	 * Disables 'Edit' button because elements
	 * are already visible. Enables 'Hide' button.
	 */
	private void showAll() {
		// Show four elements
		_namePanel.setVisible(true);
		_hoursPanel.setVisible(true);
		_submitTemplateBtn.setVisible(true);
		_stepPanel.setVisible(true);		
		// Enable 'Hide', disable 'Edit' (visible state)
		_editBtn.setEnabled(false);			
		_hideBtn.setEnabled(true);
	}
	
	/**
	 * Called on 'Hide' button, etc...
	 * Hides four template field elements.
	 * Disables 'Hide' button, since panels
	 * are already hidden. Enables 'Edit' button,
	 * to make visible again.
	 */
	private void hideAll() {
		// Hide four elements
		_namePanel.setVisible(false);
		_hoursPanel.setVisible(false);
		_submitTemplateBtn.setVisible(false);
		_stepPanel.setVisible(false);	
		// Enable 'Edit', disable 'Hide' (hidden state)
		_editBtn.setEnabled(true);			
		_hideBtn.setEnabled(false);
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
		JPanel panel = new JPanel();
		// Create Layout and Constraints, main components
		final GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(0, 0, 10, 0);
		Utils.themeComponent(panel);
		
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
				// Get row that was changed by user
/*				int rowNum = e.getLastRow();
				Object row[] = model.getRowAt(rowNum);
				// INSERT or UPDATE
				if (e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.UPDATE) { 
					// Create Step if row completely filled out; set step number to rowNum
					ITemplateStep newStep = createStepFromArray(row);
					if (newStep != null) {
						newStep.setStepNumber(rowNum);
						// Add TemplateStep to _currSteps
						if (_currSteps.size() > rowNum) {
							// 'set' replaces element at that rowNum (step number)
							if (_currSteps.get(rowNum).getStepNumber() == rowNum) 
								_currSteps.set(rowNum, newStep);
							// If stepNumber does not match index, do not replace
							else 
								_currSteps.add(rowNum, newStep);
						} 
						// If currSteps too small for step number created, add at next available index
						else {
							_currSteps.add(newStep);
						}
					}			
				}*/
				// Create new row if last row is entirely filled
				if (e.getLastRow() == model.getRowCount() -1) 
					if (model.getRowAt(e.getLastRow()).length == 2) 
						model.addBlankItem();
				model.deleteRowsIfEmpty(e.getFirstRow(), e.getLastRow());
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
		panel.add(stepLabel, c);
		
		// Instantiate StepViewTable 
		final StepViewTable steplist = new StepViewTable(model);
		// Create Header
		final JTableHeader header = steplist.getTableHeader();
		// Style Header
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(header, c);
		// Style StepViewTable list
		Utils.padComponent(steplist, 10, 30);
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(steplist, c);
		return panel;
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
		Utils.themeComponentAlt(nameLabel);
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
		Utils.themeComponentAlt(hoursLabel);
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
				_tableModel.fireTableRowsUpdated(0, /*numSteps-1*/_tableModel.getRowCount()-1);
			}
		}
	}
	
	/**
	 * Clears data from StepsViewTable. Just sets value
	 * of each cell in each row to empty string "". Alerts
	 * TableModelListener that all rows have been updated.
	 * Then TableModelListener (in createStepsPanel()) automatically
	 * removes empty rows and adds an empty row at the end.
	 * This is called before every time StepsViewTable is repopulated.
	 */
	private void resetStepsViewTable() {
		for (int i=0; i<_tableModel.getRowCount()-1; i++) {
			_tableModel.setValueAt("", i, 0);
			_tableModel.setValueAt("", i, 1);
		}
		_tableModel.fireTableRowsUpdated(0, _tableModel.getRowCount()-1);
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
	 * Inserts the specified ITemplate object to JComboBox
	 * at second-to-last index (last index reserved for 'Custom').
	 * Then sets currently selected item to newly added Template.
	 * 
	 * @param t, ITemplate to add and set selected
	 */
	private void addItemToTemplatePicker(ITemplate t) {
		int index = _templatePicker.getItemCount()-1;
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
		// TODO: Check validity of name, hours and stepNumber also?
		if (steps.isEmpty()) {
			alertUser("Invalid Input: Please add Steps to this Template");
			return false;
		}
		return true;
	}
	
// ============ END Verify Data Methods ==================
}
