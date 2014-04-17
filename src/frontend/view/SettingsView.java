package frontend.view;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import frontend.Utils;
import frontend.view.template_wizard.TemplateWizardView;

public class SettingsView extends JDialog {
	
	private static final long	serialVersionUID	= 836555231204678487L;
	
	public SettingsView() {
		super();
		
		this.setPreferredSize(new Dimension(500, 500));
		
		final JPanel mainPanel = new JPanel();
		// Set theme of page
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		Utils.themeComponent(mainPanel);
		Utils.padComponent(mainPanel, 50, 50);

		// Make title of Settings
		final JLabel title = new JLabel("Settings");
		Utils.setFont(title, 30);
		Utils.themeComponent(title);
		
		// Add title label
		mainPanel.add(Box.createVerticalStrut(20));
		mainPanel.add(title);
		
		// Add templateWizard
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(new TemplateWizardView(this));

		this.add(mainPanel);
		// Add scroll pane
		// this.add(Box.createVerticalStrut(15));
		// this.add(scroller);
	}
}
