package frontend.view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

		// "Create new template"
		JButton createTemplateBtn = new JButton("Create new template");
		createTemplateBtn.setBorderPainted(false);
		createTemplateBtn.setFocusPainted(false);
		Utilities.themeComponent(createTemplateBtn);
		createTemplateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
// TODO 				
			}
		});
		
		// "Create new step"
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
		
		//Add label and spacing
		this.add(title);
		this.add(Box.createVerticalStrut(10));
		this.add(createTemplateBtn);
		this.add(Box.createVerticalStrut(10));
		this.add(createStepBtn);
		
//TODO: Guts of wizard
	}
}
