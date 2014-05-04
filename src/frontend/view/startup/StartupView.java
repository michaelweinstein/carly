package frontend.view.startup;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import backend.database.StorageService;
import data.TimeOfDay;
import data.UnavailableBlock;
import frontend.Utils;
import frontend.view.CButton;
import frontend.view.settings.SettingsConstants;
import frontend.view.startup.timepicker.SurveyWeekView;

public class StartupView extends JDialog {

	private static final long serialVersionUID = -3311099840458252581L;
	
	/* Styling vars */
	private static final Dimension minimum_size = new Dimension(650, 750);
	// @params: top, left, bottom, right
	private static final Insets insets = new Insets(10, 5, 10, 5);	
	private static final int padding = 15;
	
	/* Text constants */
	private static final String hours_label = 
				"When do you prefer to work during the day?";
	private static final String learner_label = 
				"Would you like settings to be adjusted based on your behavior?";
	private static final String time_label =
				"Please drag for the times you are available during the average week. " + 
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
		_learnerCheck.setSelected(true);	// Starts as true
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
		c.gridwidth = 2;
		c.gridheight = 2;
		this.add(_timeView, c);
		
		// 'Submit survey' button
		CButton submitBtn = new CButton(submit_label);
		submitBtn.setPreferredSize(new Dimension(300, 30));
		submitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				submitSurvey();
			}
		});
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 2;
		this.add(submitBtn, c);
		
		// TODO User should not be able to close unless survey is completed
	}
	
	/**
	 * Stores all user-inputted settings by making calls to <code>StorageService</code>.<br>
	 * Called when user submits start-up survey, from 'Submit' button <code>ActionListener</code>. </br>
	 * Settings sent to StorageService:
	 * <ul>
	 * 	<li> <code>TimeOfDay</code> preference </li>
	 * 	<li> Learner toggled preference </li>
	 * 	<li> All <code>UnavailableBlock</code>s specified by user for the week template </li>
	 * </ul>
	 */
	private void submitSurvey() {
		// TODO: Delete printlines; Make calls to Storage Service
		// Add TOD preference
		TimeOfDay todPref = (TimeOfDay) (_todPicker.getSelectedItem());
//		StorageService.mergeSetting(SettingsConstants.TIMEOFDAY_SETTING, todPref.name());
		
		// Add Learner preference
		Boolean learnerPref = _learnerCheck.isSelected();
//		StorageService.mergeSetting(SettingsConstants.LEARNER_SETTING, learnerPref.toString());
		
		// Add UnavailableBlocks to StorageService
		List<UnavailableBlock> uBlocks = _timeView.getUnavailableBlocks();
//		StorageService.addAllDefaultUnavailableBlocks(uBlocks);
	}
}
