package frontend.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

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
		final JButton today = new CButton("Today");
		final AbstractAction todayAction = new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				app.getCalendarView().shiftWeekToToday();
			}
		};
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl T"), "today");
		getActionMap().put("today", todayAction);
		today.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				todayAction.actionPerformed(e);
			}
		});
		
		// Assignment adding
		final JDialog dialog = new AddAssignmentDialog(app);
		final JButton addAssignmentButton = new CButton("New Assignment");
		final AbstractAction addAction = new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.pack();
				dialog.setLocationRelativeTo(ToolbarView.this.getParent());
				dialog.setVisible(true);
			}
		};
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl N"), "add");
		getActionMap().put("add", addAction);
		addAssignmentButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				addAction.actionPerformed(e);
			}
		});
		
		// Settings showing
		final JDialog settings = new SettingsView();
		final JButton settingsButton = new CButton("Settings");
		final AbstractAction settingsAction = new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				settings.pack();
				settings.setLocationRelativeTo(ToolbarView.this.getParent());
				settings.setVisible(true);
			}
		};
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl S"), "settings");
		getActionMap().put("settings", settingsAction);
		settingsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				settingsAction.actionPerformed(e);
			}
		});
		
		// Title
		final JLabel title = new JLabel(Utils.APP_NAME);
		Utils.setFont(title, 26);
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
