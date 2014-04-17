package frontend.view.template_wizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import backend.StorageService;

import data.Template;
import frontend.Utils;
import frontend.view.SettingsView;

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
	private static final String new_template = "New template";
	private static final String new_step = "New step";
	private static final String template_name = "Name";
	private static final String template_hours = "Consecutive hours you want to work";
	private static final String step_name = "Step name";
	private static final String step_percent = "Percent of total assignment";
	private static final String submit_step = "Add step";
	private static final String submit_template = "Submit template";
	
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
		
		
		/* TemplateStep wizard */

		//TODO List current TemplateSteps and grab on left to move around

		/* "Create new step" button (listener below) */
		final JButton createStepBtn = new JButton(new_step);
		createStepBtn.setFocusPainted(false);
		createStepBtn.setPreferredSize(new Dimension(130, 20));
		createStepBtn.setVisible(false);
		
		/* "Step name: " */
		final JPanel stepNamePanel = newNamePanel(step_name);	
		/* "Percent of assignment: " */
		final JPanel stepPercentPanel = newNumberPanel(step_percent);
		
		/* Submit step ("Add step" button) */
		final JButton submitStepBtn = new JButton(submit_step);
		submitStepBtn.setPreferredSize(new Dimension(140, 20));
		submitStepBtn.setForeground(Color.RED);
		submitStepBtn.setFocusPainted(false);
		submitStepBtn.setVisible(false);
		submitStepBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO
				boolean verified = verifyStepData();
				if (verified) {
					//TODO 
					// Store step data from _inputMap
					// Create new Step and store in StepList
					// Clear step data from _inputMap
					
					// Set input fields invisible
					stepNamePanel.setVisible(false);
					stepPercentPanel.setVisible(false);
					submitStepBtn.setVisible(false);
				}
			}
		});
		
		/* TOP "Create new step" button listener */
		createStepBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO Set step inputs visible
				stepNamePanel.setVisible(true);
				stepPercentPanel.setVisible(true);
				submitStepBtn.setVisible(true);
			}
		});
		
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
					StorageService.addTemplate(t);
					// Add Template to local list too for responsiveness
					TemplateList.addTemplate(t);
					
					// GUI Response if template successfully created
					
					newTemplateBtn.setEnabled(true);
					// Hides all elements
					namePanel.setVisible(false);
					hoursPanel.setVisible(false);
					createStepBtn.setVisible(false);
					submitTemplateBtn.setVisible(false);
					// Hide step inputs if they are still showing
					stepNamePanel.setVisible(false);
					stepPercentPanel.setVisible(false);
					submitStepBtn.setVisible(false);
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
				createStepBtn.setVisible(true);
				submitTemplateBtn.setVisible(true);
				
				// Disable "New template" btn while form is open
				newTemplateBtn.setEnabled(false);
				// Repaint panel
				TemplateWizardView.this.repaint();
			}
		});
		
		/* --- End of Main Listener --- */
		
		/* Adds all elements of Template Wizard form */
	
		this.add(newTemplateBtn);
		this.add(Box.createVerticalStrut(10));
		
		this.add(namePanel);
		this.add(hoursPanel);
		this.add(createStepBtn);
		// Step inputs
		this.add(stepNamePanel);
		this.add(stepPercentPanel);
		this.add(submitStepBtn);
		
		this.add(submitTemplateBtn);
	}
	
// ============ START Visibility Methods ===================
	// TODO setStepsVisible(boolean), setAllVisible(boolean)
		
// ============ START Create Panel Methods ==================
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
	 * Checks that step data is valid.
	 * That Name field is not empty.
	 * That Percent field can be parsed as double.
	 * 
	 * @return
	 */
	private boolean verifyStepData() {
		// Set up validity booleans
		boolean validName = false;
		boolean validPercent = false;
		// Check step name
		if (_inputMap.containsKey(step_name)) {
			// TODO: Check that name is valid
			String stepName = _inputMap.get(step_name).getText();
			// Is not empty string
			if (!stepName.isEmpty()) {
				validName = true;
			}
		}
		// Check step percent of total
		if (_inputMap.containsKey(step_percent)) {
			// TODO: Check that percent is within range, and Double.parseDouble works
			String percent = _inputMap.get(step_percent).getText();
			if (!percent.isEmpty()) {
				// Is valid as double
				try {
					Double.parseDouble(percent);
					validPercent = true;
				} catch (NumberFormatException e) {
					validPercent = false;
				}
			}
		}
		// TODO Display specific error messages
		return validName && validPercent;
	}
	
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
