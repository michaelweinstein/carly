package frontend.view.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import data.TimeOfDay;
import frontend.Utils;
import frontend.view.settings.template_wizard.TemplateWizardView;

public class SettingsView extends JDialog {
	
	private static final long		serialVersionUID	= 836555231204678487L;
	
	/* Styling Vars */
	private static final Dimension	minimum_size		= new Dimension(400, 500);
	private static final int		title_padding		= 10;
	private static final int		padding				= 15;
	private static final int		title_font_size		= 30;
	
	/* Strings */
	private static final String		page_title			= "Settings";
	private static final String		toggle_learning		= "Toggle Learning Algorithm";
	private static final String		preferred_timeofday	= "When do you prefer to work?";
	
	// private static final String template_wizard = "Template Wizard";
	
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
		// this.add(buttonPanel, BorderLayout.SOUTH);
		
		// TODO: Add scroll pane (using CScrollPane)
	}
	
	// TODO: Comment and finish
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
		// TODO: Wire the user input to SettingsDelegate; Create ActionListeners
		
		// TODO: Set initial values: TimeOfDay value stored in database
		// TODO: (does Toggle Learning have an initial value, from start-up survey?)
		
		// TimeOfDay: Label, JComboBox
		final JComboBox<TimeOfDay> timeOfDayPicker = new JComboBox<>(getTimesOfDay());
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		inputPanel.add(timeOfDayPicker);
		final JLabel timeOfDayLabel = new JLabel(preferred_timeofday);
		Utils.themeComponent(timeOfDayLabel);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		inputPanel.add(timeOfDayLabel);
		
		// Toggle learning algorithm Check Box
		final JCheckBox learningCheckBox = new JCheckBox(toggle_learning, true);
		Utils.themeComponent(learningCheckBox);
		Utils.padComponent(learningCheckBox, 5, 5);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		// c.gridwidth = GridBagConstraints.REMAINDER;
		inputPanel.add(learningCheckBox, c);
		
		// TODO: Do I have to add TemplateWizardView to the JDialogue directly!
		// Make TemplateWizard
		/*
		 * TemplateWizardView templateWizard = new TemplateWizardView(); c.gridx = 1; c.gridy = 2; c.gridheight = 8;
		 * inputPanel.add(templateWizard, c);
		 */
		
		return inputPanel;
	}
	
	// TODO: Complete and comment
	private static JLabel createSettingsTitle() {
		final JLabel title = new JLabel(page_title);
		title.setOpaque(true);
		title.setFont(Utils.getFont(Font.BOLD, title_font_size));
		Utils.themeComponent(title);
		Utils.addBorderBottom(title);
		Utils.padComponentWithBorder(title, 0, title_padding);
		return title;
	}
	
	// TODO: Complete and comment
	private static TimeOfDay[] getTimesOfDay() {
		return TimeOfDay.values();
	}
	
}
