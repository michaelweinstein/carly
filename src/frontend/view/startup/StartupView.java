package frontend.view.startup;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import backend.database.StorageService;
import data.TimeOfDay;
import data.UnavailableBlock;
import frontend.Utils;
import frontend.view.settings.SettingsConstants;
import frontend.view.startup.timepicker.SurveyWeekView;

public class StartupView extends JDialog {

	private static final long serialVersionUID = -3311099840458252581L;
	
	/* Styling vars */
	private static final Dimension minimum_size = new Dimension(600, 800);
	// @params: top, left, bottom, right
	private static final Insets insets = new Insets(10, 5, 10, 5);	
	private static final int padding = 15;
	
	/* Text constants */
	private static final String hours_label = 
				"When do you prefer to work during the day?";
	private static final String learner_label = 
				"Would you like settings to be adjusted based on your behavior?";
	private static final String time_label =
				"Please drag for the times you are available during the average week." + 
						"This template is final!";
	private static final String submit_label = "Submit survey";
	
	/* Input fields */
	private JComboBox<TimeOfDay> _todPicker;
	private JCheckBox _learnerCheck;
	private SurveyWeekView _timeView;
	
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
		
		/* "When do you prefer to work during the day?" */
		JLabel todLabel = new JLabel(hours_label);
		_todPicker = new JComboBox<>(TimeOfDay.values());
		_todPicker.setEditable(false);	// TOD values never modified
		Utils.themeComponent(todLabel);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		this.add(todLabel, c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		this.add(_todPicker, c);
		
		/* "Would you like settings to be adjusted based on your behavior?" */
		JLabel learnerLabel = new JLabel(learner_label);
		_learnerCheck = new JCheckBox();
		Utils.themeComponent(learnerLabel);
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		this.add(learnerLabel, c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		this.add(_learnerCheck, c);
		
		/* "Set available times during average week" */
		JLabel timeLabel = new JLabel(time_label);
		_timeView = new SurveyWeekView();
		Utils.themeComponent(timeLabel);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		this.add(timeLabel, c);
		c.gridx = 0;
		c.gridy = 3;
		this.add(_timeView, c);
		
		JButton submitBtn = new JButton(submit_label);
		submitBtn.setPreferredSize(new Dimension(20, 200));
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
//		c.anchor = GridBagConstraints.WEST;
//		this.add(submitBtn, c);
		
		// TODO User cannot close unless survey is completed
	}
	
	// TODO: Complete and comment
	private void submitSurvey() {
		// Add TOD preference
		TimeOfDay todPref = (TimeOfDay) (_todPicker.getSelectedItem());
		StorageService.mergeSetting(SettingsConstants.TIMEOFDAY_SETTING, todPref.name());
		
		// Add Learner preference
		Boolean learnerPref = _learnerCheck.isSelected();
		StorageService.mergeSetting(SettingsConstants.LEARNER_SETTING, learnerPref.toString());
		
		// Add UnavailableBlocks to StorageService
		List<UnavailableBlock> uBlocks = _timeView.getUnavailableBlocks();
		for (UnavailableBlock b: uBlocks) {
/*			try {
				StorageService.addTimeBlock(b);
			} catch (StorageServiceException e) {
				System.out.println("ERROR: StorageServiceException when adding " + 
						"time blocks (StartupView.submitSurvey)");
				e.printStackTrace();
			}*/
		}
	}
}
