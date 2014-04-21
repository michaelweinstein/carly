package frontend.view.template_wizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

import data.Template;
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
	
	// Easy access to input fields, getText inputted by user
	private Map<String, JTextComponent> _inputMap = new HashMap<>();
	
	/* User Input Label Strings */
	private static final String new_template 		= "New template";
	private static final String template_name 		= "Name";
	private static final String template_hours 		= "Consecutive hours you want to work";
	private static final String submit_template 	= "Submit template";	
	// Steps Panel
	private static final String step_name 			= "Step name";
	private static final String step_percent 		= "% of total";
	private static final String steps_list 			= "Steps";
	
	// TODO Store panels/buttons as instance variables, factor out some code (change order of constructor listeners)
	
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
				
////////
		// Get list of Templates and store it in TemplateList (in constructor)
		new TemplateList();

		// TODO: List current templates and resize when grabbing on far left
		
		/* "New template" button (Listener below) */
		final JButton newTemplateBtn = new JButton(new_template);
		newTemplateBtn.setFocusPainted(false);
		newTemplateBtn.setPreferredSize(new Dimension(170, 20));
		newTemplateBtn.setVisible(true);
		
		/* Name: (code in newNamePanel()) */
		final JPanel namePanel = newNamePanel(template_name);
		
		/* Consecutive Hours: */
		final JPanel hoursPanel = newNumberPanel(template_hours);
		
		
		//TODO List current TemplateSteps and grab on left to move around
		
		final JPanel stepPanel = createStepsPanel();
		stepPanel.setVisible(false);
		
		/* --- End of TemplateStep Wizard --- */

				
		/* "Submit template" button */
		final JButton submitTemplateBtn = new JButton(submit_template);
		submitTemplateBtn.setForeground(Color.RED);
		submitTemplateBtn.setFocusPainted(false);
		submitTemplateBtn.setPreferredSize(new Dimension(300, 30));
		submitTemplateBtn.setVisible(false);
		submitTemplateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// Verify form data
				boolean verified = verifyTemplateData();	
				if (verified) {
					// Create and store new instance of Template
					
					String name = _inputMap.get(template_name).getText();
					double hours = Double.parseDouble(_inputMap.get(template_hours).getText());
					Template t = new Template(name, hours);
					// Add Template to database
//					StorageService.addTemplate(t);
					// Add Template to StorageService and local list
					TemplateList.addTemplate(t);
					
					// GUI Response if template successfully created
					
					newTemplateBtn.setEnabled(true);
					// Hides all elements
					namePanel.setVisible(false);
					hoursPanel.setVisible(false);
					
//					createStepBtn.setVisible(false);					
					
					submitTemplateBtn.setVisible(false);
					// Hide step inputs if they are still showing
					stepPanel.setVisible(false);
				}
			}
		});

		/* TOP: "New template" Listener */
		newTemplateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				
				// Dislays all elements		
				namePanel.setVisible(true);
				hoursPanel.setVisible(true);
				submitTemplateBtn.setVisible(true);
				stepPanel.setVisible(true);
				
				// Disable "New template" btn while form is open
				newTemplateBtn.setEnabled(false);
				// Repaint panel
				TemplateWizardView.this.repaint();
			}
		});
		
		/* --- End of Main Listener --- */
		
		/* Adds all elements of Template Wizard form */
////////////////////////////////////////////////	
		this.add(newTemplateBtn);
//		this.add(Box.createVerticalStrut(10));
/////^^^^^^^^^^
		
		this.add(namePanel);
		this.add(hoursPanel);
		
//		this.add(createStepBtn);
		// Step inputs
//		this.add(stepNamePanel);
//		this.add(stepPercentPanel);
//		this.add(submitStepBtn);
		
//////////////////StepViewTable
		this.add(stepPanel);
		
		this.add(submitTemplateBtn);
	}
	
// ============ START Visibility Methods ===================
	// TODO setStepsVisible(boolean), setAllVisible(boolean)
		
// ============ START Create Panel Methods ==================
	
	/**
	 * Creates and returns a JPanel with JTable
	 * for user to input and view the TemplateSteps
	 * of a Template.
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
			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO User-edited Step Table
				if (e.getLastRow() == model.getRowCount() -1) 
					model.addBlankItem();
				model.deleteRowsIfEmpty(e.getFirstRow(), e.getLastRow());
				revalidate();
				TemplateWizardView.this.repaint();
			}
		});
		
		// 'Steps: ' label
		final JLabel stepLabel = new JLabel(steps_list + ": ");
		Utils.themeComponent(stepLabel);
		Utils.padComponent(stepLabel, 11, 5);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
//		c.anchor = GridBagConstraints.CENTER;
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
		hoursInput.setPreferredSize(new Dimension(30, 20));
		hoursPanel.add(hoursLabel);
		hoursPanel.add(hoursInput);
		hoursPanel.setVisible(false);
		// Add to input map
		_inputMap.put(name, hoursInput);
		return hoursPanel;
	}
// ============ END Create Panel Methods ==================
	
	
// ============ START Verify Data Methods ==================

	/**
	 * Verifies that all user input fields have valid input, and that no
	 * mandatory fields have been left blank -- returns true. 
	 * Alerts user if a field is invalid or blank, and returns false.
	 * 
	 * @return true if all fields valid, else false
	 */
	private boolean verifyTemplateData() {
		// Set up validity booleans
		boolean validName = false;
		boolean validHours = false;
		// Check name
		if (_inputMap.containsKey(template_name)) {
			// TODO Check that name is valid
			String name = _inputMap.get(template_name).getText();
			// Is not empty string
			if (!name.isEmpty()) {
				validName = true;
			}
		}
		// Check consecutive hours
		if (_inputMap.containsKey(template_hours)) {
			// TODO Check hours is within range, and Double.parseDouble works
			String hours = _inputMap.get(template_hours).getText();		
			if (!hours.isEmpty()) {
				// Is valid format; can be parsed as double
				try {
					Double.parseDouble(hours);
					validHours = true;
				} catch (NumberFormatException e) {
					validHours = false;
				}
			}
		}
		// TODO Display specific error message to user if something is not valid
		return validName && validHours;
	}
	
// ============ END Verify Data Methods ==================
}
