package frontend.view.settings;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import frontend.Utils;
import frontend.view.settings.template_wizard.TemplateWizardView;

public class SettingsView extends JDialog {
	
	private static final long	serialVersionUID	= 836555231204678487L;
	
	public SettingsView() {
		super();
		
		this.setPreferredSize(new Dimension(500, 500));
		
		final JPanel mainPanel = new JPanel();
		/* Theme/Styling Main */
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		Utils.themeComponent(mainPanel);
		Utils.padComponent(mainPanel, 50, 50);

		
		/* Instantiate Elements */
		
		// Make title of Settings
		final JLabel title = new JLabel("Settings");
		Utils.setFont(title, 30);
		Utils.themeComponent(title);
		
		// Make TemplateWizard
		TemplateWizardView templateWizard = new TemplateWizardView();
		
		
		/* Adding Elements to MainPanel*/
		
		mainPanel.add(title);
		mainPanel.add(templateWizard);

		
		/* Add MainPanel to Settings */
		
		this.add(mainPanel);
		
		// TODO: Add scroll pane?
	}
}
