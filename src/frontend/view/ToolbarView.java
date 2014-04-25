package frontend.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import frontend.Utils;
import frontend.app.GUIApp;
import frontend.view.assignments.AddAssignmentDialog;
import frontend.view.settings.SettingsView;

/**
 * Represents the toolbar at top of the screen
 * 
 * @author dgattey
 */
public class ToolbarView extends JPanel {
	
	private static final long	serialVersionUID	= -2158045975284361590L;
	
	public ToolbarView(final GUIApp app) {
		Utils.themeComponent(this);
		Utils.addBorderBottom(this);
		Utils.padComponentWithBorder(this, 0, 20);
		
		// Today
		final JButton today = new JButton("Today");
		today.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				app.getCalendarView().shiftWeekToToday();
			}
		});
		
		// Assignment adding
		final JDialog dialog = new AddAssignmentDialog(app);
		final JButton addAssignmentButton = new JButton("New Assignment");
		addAssignmentButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.pack();
				dialog.setVisible(true);
			}
		});
		
		// Settings showing
		final JDialog settings = new SettingsView();
		final JButton settingsButton = new JButton("Settings");
		settingsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				settings.pack();
				settings.setVisible(true);
			}
		});
		
		// Title
		final JLabel title = new JLabel(Utils.APP_NAME);
		title.setFont(new Font(Utils.APP_FONT_NAME, Font.BOLD, 24));
		Utils.themeComponent(title);
		
		// Addition of all things
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(Box.createHorizontalStrut(20));
		add(title);
		add(Box.createGlue());
		add(today);
		add(Box.createHorizontalStrut(10));
		add(addAssignmentButton);
		add(Box.createHorizontalStrut(10));
		add(settingsButton);
		add(Box.createHorizontalStrut(10));
	}
}
