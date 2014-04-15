package frontend.view;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import frontend.Utils;

public class SettingsView extends JDialog {
	
	private static final long	serialVersionUID	= 836555231204678487L;
	
	public SettingsView() {
		super();
		
		final JPanel mainPane = new JPanel();
		// Set theme of page
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		Utils.themeComponent(mainPane);
		Utils.padComponent(mainPane, 50, 50);
		
		// Make a scroll pane
		final JScrollPane scroller = new JScrollPane();
		scroller.setBorder(null);
		Utils.themeComponent(scroller);
		Utils.themeComponent(scroller.getViewport());
		
		// Make title of Settings
		final JLabel title = new JLabel("Settings");
		Utils.setFont(title, 30);
		Utils.themeComponent(title);
		
		// Add title label
		mainPane.add(Box.createVerticalStrut(20));
		mainPane.add(title);
		
		// Add templateWizard
		mainPane.add(Box.createVerticalStrut(10));
		mainPane.add(new TemplateWizardView(this));
		
		this.add(mainPane);
		// Add scroll pane
		// this.add(Box.createVerticalStrut(15));
		// this.add(scroller);
	}
}
