package frontend.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import frontend.Utilities;

/**
 * This is the front-end class for the Template Wizard panel
 * that is on the Settings page (not the Dialogue View).
 * 
 * @author miweinst
 *
 */

public class TemplateWizardView extends JPanel {

	private static final long serialVersionUID = 4215933185975151935L;
	
	private static final int title_size = 19;
	private static final int padding = 10;

	public TemplateWizardView(SettingsView settings) {
		super();
		// Set theme and layout of wizard
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utilities.themeComponentInverse(this);
		Utilities.addBorderFull(this);
		Utilities.padComponentWithBorder(this, padding, padding);

		// Template Wizard label
		JLabel title = new JLabel("Template Wizard");
		Utilities.setFont(title, title_size);
		Utilities.themeComponent(title);
		
//TODO: List current templates and resize when grabbing on far left
	// OR should I put that in Settings, and TemplateWizard is just for new templates??

		// "Create new template"
		JButton createTemplateBtn = new JButton("Create new template");
		createTemplateBtn.setBorderPainted(false);
		createTemplateBtn.setFocusPainted(false);
		Utilities.themeComponent(createTemplateBtn);		
		final JPanel templateDiv = templateDiv();
		createTemplateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {			
				// Sets templateDiv to visible and renders
				templateDiv.setVisible(true);
				templateDiv.repaint();
				TemplateWizardView.this.repaint();
			}
		});
		
		//Add label and spacing
		this.add(title);
		this.add(Box.createVerticalStrut(10));
		this.add(createTemplateBtn);
		this.add(Box.createVerticalStrut(10));
	
		// Add templateDiv; starts with visible(false)
		this.add(Box.createHorizontalStrut(10));
		this.add(templateDiv);
			
//		this.add(Box.createVerticalStrut(10));
//		this.add(createStepBtn);
	}
	
	/**
	 * Makes and returns the panel that
	 * is only displayed when user presses
	 * "Create new template" button. Contains
	 * the TemplateWizard's input fields
	 * @return JPanel
	 */
	private JPanel templateDiv() {
		// Set up encompassing div
		final JPanel templateDiv = new JPanel();
		templateDiv.setLayout(new BoxLayout(templateDiv, BoxLayout.Y_AXIS));
		Utilities.themeComponentInverse(templateDiv);
		templateDiv.setPreferredSize(new Dimension(120, 100));
		//Starts not visible; set visible in actionlistener in constructor
		templateDiv.setVisible(false);
		
		// Name of new Template
		final JPanel namePanel = new JPanel();
		Utilities.themeComponentInverse(namePanel);
		Utilities.addBorderLeft(namePanel);
		final JLabel nameLabel = new JLabel("Name: ");
		final JTextField nameInput = new JTextField();
		nameInput.setPreferredSize(new Dimension(100, 20));
		namePanel.add(nameLabel);
		namePanel.add(nameInput);

		// "Create new step" button
		JButton createStepBtn = new JButton("Create new step");
		createStepBtn.setBorderPainted(false);
		createStepBtn.setFocusPainted(false);
		Utilities.themeComponent(createStepBtn);
		createStepBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
// TODO
			}
		});
		
		// Add elements to panel
		templateDiv.add(namePanel);
		templateDiv.add(createStepBtn);
		
		return templateDiv;
	}
	
	/**
	 * Makes anre turns the panel displayed
	 * when user presses "Create new step"
	 * @return JPanel
	 */
	private JPanel stepDiv() {
// TODO
		//Name of subtask
		//Percent of total task
		return null;
	}
}
