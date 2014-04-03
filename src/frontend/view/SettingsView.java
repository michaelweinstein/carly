package frontend.view;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import frontend.Utilities;

public class SettingsView extends JPanel {

	private static final long serialVersionUID = 836555231204678487L;

	public SettingsView() {
		super();
		// Set theme of page
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utilities.themeComponent(this);
		Utilities.padComponent(this, 50, 50);
		
		// Make a scroll pane
		JScrollPane scroller = new JScrollPane();
		scroller.setBorder(null);
		Utilities.themeComponent(scroller);
		Utilities.themeComponent(scroller.getViewport());
		
		// Make title of Settings
		JLabel title = new JLabel("Settings");
		Utilities.setFont(title, 30);
		Utilities.themeComponent(title);
		
		// TemplateWizard panel
		TemplateWizardView templateWizard = new TemplateWizardView(this);
		
		//Add title label
		this.add(Box.createVerticalStrut(20));
		this.add(title);
		// Add templateWizard
		this.add(Box.createVerticalStrut(10));
		this.add(templateWizard);
		// Add scroll pane
//		this.add(Box.createVerticalStrut(15));
//		this.add(scroller);
	}
}
