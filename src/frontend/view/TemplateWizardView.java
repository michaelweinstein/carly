package frontend.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import frontend.Utils;

/**
 * This is the front-end class for the Template Wizard panel that is on the Settings page (not the Dialogue View).
 * 
 * @author miweinst
 */

public class TemplateWizardView extends JPanel {
	
	private static final long	serialVersionUID	= 4215933185975151935L;
	
	private static final int	title_size			= 19;
	private static final int	padding				= 10;
	
	public TemplateWizardView(final SettingsView settings) {
		super();
		// Set theme and layout of wizard
		Utils.themeComponent(this);
		Utils.addBorderFull(this);
		Utils.padComponentWithBorder(this, padding, padding);

		// TODO: List current templates and resize when grabbing on far left
		
		/* "New template" button (Listener below) */
		final JButton newTemplateBtn = new JButton("New template");
		newTemplateBtn.setFocusPainted(false);
		newTemplateBtn.setPreferredSize(new Dimension(170, 20));
		
///////////////
		/* Name: */
		final JPanel namePanel = new JPanel();
		Utils.themeComponent(namePanel);
		namePanel.setPreferredSize(new Dimension(250, 40));		
		final JLabel nameLabel = new JLabel("Name: ");
		Utils.themeComponentInverse(nameLabel);
		final JTextField nameInput = new JTextField();
		nameInput.setPreferredSize(new Dimension(100, 20));
		namePanel.add(nameLabel);
		namePanel.add(nameInput);
		namePanel.setVisible(false);
		
		/* Consecutive Hours: */
		final JPanel hoursPanel = new JPanel();
		Utils.themeComponent(hoursPanel);
		hoursPanel.setPreferredSize(new Dimension(300, 40));
		final JLabel hoursLabel = new JLabel("Consecutive hours you want to work: ");
		Utils.themeComponentInverse(hoursLabel);
		final JTextField hoursInput = new JTextField();
		hoursInput.setPreferredSize(new Dimension(30, 20));
		hoursPanel.add(hoursLabel);
		hoursPanel.add(hoursInput);
		hoursPanel.setVisible(false);
		
		//TODO List current TemplateSteps and grab on left to move around

		/* "Create new step" button */
		final JButton createStepBtn = new JButton("Add step");
		createStepBtn.setFocusPainted(false);
		createStepBtn.setPreferredSize(new Dimension(70, 20));
		createStepBtn.setVisible(false);
		createStepBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO Set step inputs visible
			}
		});
		
		// TODO Add step inputs
		
		/* "Create template" button; submits new template */
		final JButton submitTemplateBtn = new JButton("Submit template");
		submitTemplateBtn.setForeground(Color.RED);
		submitTemplateBtn.setFocusPainted(false);
		submitTemplateBtn.setPreferredSize(new Dimension(300, 30));
		submitTemplateBtn.setVisible(false);
		submitTemplateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// TODO Check that all data is valid and entered, then create and send Template to database
				boolean verified = verifyData();
				if (verified) {
					// Re-enable "New template" button
					newTemplateBtn.setEnabled(true);
					
					// Hides all elements
					namePanel.setVisible(false);
					hoursPanel.setVisible(false);
					createStepBtn.setVisible(false);
					submitTemplateBtn.setVisible(false);
				}
			}
		});
////////^^^^^^^^^^		
		
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
		
		/* Adds all elements of Template Wizard form */
	
		this.add(newTemplateBtn);
		this.add(Box.createVerticalStrut(10));
		
		this.add(namePanel);
		this.add(hoursPanel);
		this.add(createStepBtn);
		this.add(Box.createVerticalStrut(10));
		
		this.add(submitTemplateBtn);
	}
	
	/**
	 * Verifies that all user input fields have valid input, and that no
	 * mandatory fields have been left blank -- returns true. 
	 * Alerts user if a field is invalid or blank, and returns false.
	 * 
	 * @return true if all fields valid, else false
	 */
	private boolean verifyData() {
		// TODO Verify all fields valid and filled; display message to user if not
		return true;
	}
}
