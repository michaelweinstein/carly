package frontend.view.startup;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import data.TimeOfDay;
import frontend.Utils;
import frontend.view.startup.timepicker.SurveyWeekView;

public class StartupView extends JDialog {

	private static final long serialVersionUID = -3311099840458252581L;
	
	/* Styling vars */
	private static final Dimension minimum_size = new Dimension(600, 750);
	// @params: top, left, bottom, right
	private static final Insets insets = new Insets(10, 5, 10, 5);	
	private static final int padding = 15;
	
	/* Text constants */
	private static final String hours_label = 
				"When do you prefer to work during the day?";
	private static final String learner_label = 
				"Would you like settings to be adjusted based on your behavior?";
	private static final String time_label =
				"Please drag for the times you are available during the average week:";
	
	public StartupView() {
		super();
		// Lock minimum size, set starting size
		this.setMinimumSize(minimum_size);
		this.setPreferredSize(minimum_size);
		// Theme and pad dialog's root pane
		Utils.themeComponent(getRootPane());
		Utils.padComponent(getRootPane(), padding, padding);
		// TODO: Should I need to theme content pane? I don't in SettingsView
		Utils.themeComponent(getContentPane());
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = insets;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.VERTICAL;
		int currRow = 0;
		
		/* "When do you prefer to work during the day?" */
		JLabel todLabel = new JLabel(hours_label);
		JComboBox<TimeOfDay> todPicker = new JComboBox<>(TimeOfDay.values());
		todPicker.setEditable(false);	// TOD values never modified
		Utils.themeComponent(todLabel);
		c.gridx = 0;
		c.gridy = currRow+=1;
		c.anchor = GridBagConstraints.EAST;
		this.add(todLabel, c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		this.add(todPicker, c);
		
		/* "Would you like settings to be adjusted based on your behavior?" */
		JLabel learnerLabel = new JLabel(learner_label);
		JCheckBox learnerCheck = new JCheckBox();
		Utils.themeComponent(learnerLabel);
		c.gridx = 0;
		c.gridy = currRow+=1;
		c.anchor = GridBagConstraints.EAST;
		this.add(learnerLabel, c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		this.add(learnerCheck, c);
		
		// TODO Week graphic and setting available time template
		JLabel timeLabel = new JLabel(time_label);
		SurveyWeekView timeView = new SurveyWeekView();
		Utils.themeComponent(timeLabel);
		c.gridx = 0;
		c.gridy = currRow+=1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		this.add(timeLabel, c);
		c.gridx = 0;
		c.gridy = currRow+=1;
		this.add(timeView, c);
		
		
		
		// TODO User cannot close unless survey is completed
	}
}
