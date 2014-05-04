package frontend.view.startup;

import java.awt.Color;
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

public class SurveyView extends JDialog {
	
	// TODO Don't let user close until data has been submitted.
	
	private static final long			serialVersionUID	= -3311099840458252581L;
	
	private static final Dimension		minimum_size		= new Dimension(650, 750);
	private static final Insets			insets				= new Insets(10, 5, 10, 5);
	private static final int			padding				= 15;
	private static final float			title_size			= 52.0f;
	private static final String			title_label			= "Welcome to Carly!";
	private static final String			hours_label			= "When do you prefer to work during the day?";
	private static final String			learner_label		= "Would you like settings to be adjusted based on your behavior?";
	private static final String			time_label			= "Please drag for the times you are unavailable during the average week.";
	private static final String			submit_label		= "Let's get started!";
	
	/* Input fields */
	private final JComboBox<TimeOfDay>	_todPicker;
	private final JCheckBox				_learnerCheck;
	private final SurveyWeekView		_timeView;
	
	public SurveyView() {
		super();
		setMinimumSize(minimum_size);
		setPreferredSize(minimum_size);
		Utils.themeComponent(getRootPane());
		Utils.padComponent(getRootPane(), padding, padding);
		Utils.themeComponent(getContentPane());
		
		// Layout initialization
		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = insets;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.VERTICAL;
		
		/* === Fields and labels === */
		
		int ycount = 0;
		
		/* "Welcome to Carly!" */
		final JLabel titleLabel = new JLabel(title_label);
		Utils.themeComponent(titleLabel);
		titleLabel.setFont(titleLabel.getFont().deriveFont(title_size));
		titleLabel.setForeground(Color.ORANGE);
		c.gridx = 0;
		c.gridy = ycount += 1; // 0
		c.gridwidth = GridBagConstraints.REMAINDER;
		this.add(titleLabel, c);
		
		/* "When do you prefer to work during the day?" */
		final JLabel todLabel = new JLabel(hours_label);
		_todPicker = new JComboBox<>(TimeOfDay.values());
		_todPicker.setEditable(false); // TOD values never modified
		Utils.themeComponent(todLabel);
		c.gridx = 0;
		c.gridy = ycount += 1; // 2
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		this.add(todLabel, c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		this.add(_todPicker, c);
		
		/* "Would you like settings to be adjusted based on your behavior?" */
		final JLabel learnerLabel = new JLabel(learner_label);
		_learnerCheck = new JCheckBox();
		_learnerCheck.setSelected(true); // Starts as true
		Utils.themeComponent(learnerLabel);
		c.gridx = 0;
		c.gridy = ycount += 1; // 3
		c.anchor = GridBagConstraints.EAST;
		this.add(learnerLabel, c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		this.add(_learnerCheck, c);
		
		/* "Please drag for unavailable times during average week" */
		final JLabel timeLabel = new JLabel(time_label);
		_timeView = new SurveyWeekView();
		Utils.themeComponent(timeLabel);
		c.gridx = 0;
		c.gridy = ycount += 1; // 4
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.EAST;
		this.add(timeLabel, c);
		c.gridx = 0;
		c.gridy = ycount += 1; // 5
		c.gridwidth = 2;
		c.gridheight = 2;
		this.add(_timeView, c);
		
		// 'Submit survey' button
		final CButton submitBtn = new CButton(submit_label);
		submitBtn.setPreferredSize(new Dimension(300, 30));
		submitBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				submitSurvey();
			}
		});
		c.gridx = 0;
		c.gridy = ycount += 2; // 7
		c.gridwidth = 2;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.CENTER;
		this.add(submitBtn, c);
		
		// TODO User should not be able to close unless survey is completed
	}
	
	/**
	 * Stores all user-inputted settings by making calls to <code>StorageService</code>.<br>
	 * Called when user submits start-up survey, from 'Submit' button <code>ActionListener</code>. </br> Settings sent
	 * to StorageService:
	 * <ul>
	 * <li> <code>TimeOfDay</code> preference</li>
	 * <li>Learner toggled preference</li>
	 * <li>All <code>UnavailableBlock</code>s specified by user for the week template</li>
	 * </ul>
	 */
	private void submitSurvey() {
		// Add TOD preference
		final TimeOfDay todPref = (TimeOfDay) (_todPicker.getSelectedItem());
		StorageService.mergeSetting(SettingsConstants.TIMEOFDAY_SETTING, todPref.name());
		
		// Add Learner preference
		final Boolean learnerPref = _learnerCheck.isSelected();
		StorageService.mergeSetting(SettingsConstants.LEARNER_SETTING, learnerPref.toString());
		
		// Add UnavailableBlocks to StorageService
		final List<UnavailableBlock> uBlocks = _timeView.getUnavailableBlocks();
		StorageService.addAllDefaultUnavailableBlocks(uBlocks);
		
		// Close window on Submit
		dispose();
	}
}
