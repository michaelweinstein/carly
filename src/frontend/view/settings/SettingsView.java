package frontend.view.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import backend.database.StorageService;
import data.TimeOfDay;
import frontend.Utils;
import frontend.view.settings.template_wizard.TemplateWizardView;

public class SettingsView extends JDialog {
	
	private static final long		serialVersionUID	= 836555231204678487L;
	
	/* Styling vars */
	private static final Dimension	minimum_size		= new Dimension(400, 500);
	private static final int		title_padding		= 10;
	private static final int		padding				= 15;
	private static final int		title_font_size		= 30;
	
	/* Strings */
	private static final String		page_title			= "Settings";
	private static final String		toggle_learning		= "Toggle Learning Algorithm";
	private static final String		preferred_timeofday	= "When do you prefer to work?";
	
	// private static final String template_wizard = "Template Wizard";
	
	/* Data Structure vars */
	private static JComboBox<TimeOfDay> _todPicker;
	private static JCheckBox _learnerToggle;
	
	public SettingsView() {
		super();
		setPreferredSize(minimum_size);
		setMinimumSize(minimum_size);
		Utils.themeComponent(getRootPane());
		Utils.padComponent(getRootPane(), 15, 15);
		
		// Keyboard shortcut
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
		getRootPane().getActionMap().put("escape", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
				dispose();
			}
			
		});
		
		// TODO: Switch back to BorderLayout
		setLayout(new BorderLayout());
		
		final JLabel titlePanel = createSettingsTitle();
		final JPanel inputPanel = createInputPanel();
		final JPanel templateWizardPanel = new TemplateWizardView();
		// final JPanel bottomPanel = createBottomPanel();
		
		/* Adding Elements to MainPanel */
		
		this.add(titlePanel, BorderLayout.NORTH);
		this.add(templateWizardPanel, BorderLayout.CENTER);
		this.add(inputPanel, BorderLayout.SOUTH);
		
		/* Window Listener */
		this.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
				// Populates settings whenever Settings Dialog is opened
				populateSettings();
			}
			public void windowClosing(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
		});
		
		// TODO: Add scroll pane (using CScrollPane)
	}
	
	/**
	 * Creates small input panel with GridBagLayout to 
	 * be added after TemplateWizardView. <br>
	 * Consists of the 'Toggle Learning Algorithm' JCheckBox
	 * and the 'TimeOfDay Preference' JComboBox.
	 * Input field listeners trigger data submission
	 * to StorageService. <br>
	 * Fields are initialized to the values stored in database
	 * from previous settings and/or startup survey.
	 * 
	 * @return input JPanel with user input fields included
	 */
	private JPanel createInputPanel() {
		final JPanel inputPanel = new JPanel();
		/* Styling */
		Utils.themeComponent(inputPanel);
		// Utils.addBorderTop(inputPanel);
		Utils.addBorderBottom(inputPanel);
		Utils.padComponentWithBorder(inputPanel, padding, padding);
		/* Grid Bag Layout */
		inputPanel.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(5, 0, 5, 0); // top left bottom right
		
		/* User Input Elements */			
		// TimeOfDay: Label, JComboBox
		final JLabel timeOfDayLabel = new JLabel(preferred_timeofday);
		Utils.themeComponent(timeOfDayLabel);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		inputPanel.add(timeOfDayLabel);
		_todPicker = new JComboBox<>(getTimesOfDay());
		_todPicker.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Test StorageService submission
				TimeOfDay item = (TimeOfDay) _todPicker.getSelectedItem();
				StorageService.mergeSetting(SettingsConstants.TIMEOFDAY_SETTING, item.name());
			}
		});
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		inputPanel.add(_todPicker);
		
		// Toggle learning algorithm Check Box
		final JLabel learningLabel = new JLabel(toggle_learning);
		Utils.themeComponent(learningLabel);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		inputPanel.add(learningLabel, c);
		_learnerToggle = new JCheckBox();
		_learnerToggle.setSelected(true);
		Utils.themeComponent(_learnerToggle);
		Utils.padComponent(_learnerToggle, 5, 5);
		_learnerToggle.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				// TODO Test StorageService submission
				Boolean sel = _learnerToggle.isSelected();
				StorageService.mergeSetting(SettingsConstants.LEARNER_SETTING, sel.toString());
			}	
		});
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		inputPanel.add(_learnerToggle, c);
		
		return inputPanel;
	}
	
	/**
	 * Populates TimeOfDay JComboBox and Learner's on/off checkbox
	 * with the values previously stored in database. Ensures
	 * persistence of Settings, and data retrieval from initial
	 * start-up survey. Called at end of constructor.
	 * 
	 * @param todPicker TimeOfDay JComboBox
	 * @param learningToggle JCheckBox
	 */
	private void populateSettings() {
		// Set TimeOfDay item
		// TODO
		String todString = StorageService.getSetting(SettingsConstants.TIMEOFDAY_SETTING);
		TimeOfDay tod = TimeOfDay.valueOf(todString);
		_todPicker.setSelectedItem(tod);
		// Set Learning Toggle state
		String learningString = StorageService.getSetting(SettingsConstants.LEARNER_SETTING);
		_learnerToggle.setSelected(Boolean.parseBoolean(learningString));
	}
	
	/**
	 * Returns the formatted JLabel of the title
	 * 'Settings' at top of Settings dialog box.
	 * Includes border on bottom, custom size
	 * and bold font. <br>
	 * Values set as constants in class.
	 * 
	 * @return Settings title JLabel
	 */
	private static JLabel createSettingsTitle() {
		final JLabel title = new JLabel(page_title);
		title.setOpaque(true);
		title.setFont(Utils.getFont(Font.BOLD, title_font_size));
		Utils.themeComponent(title);
		Utils.addBorderBottom(title);
		Utils.padComponentWithBorder(title, 0, title_padding);
		return title;
	}
	
	/**	 
	 * @return array of all possible values of 
	 * 		the <code>TimeOfDay</code> enum.
	 */
	private static TimeOfDay[] getTimesOfDay() {
		return TimeOfDay.values();
	}
	
}
