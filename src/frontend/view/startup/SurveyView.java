package frontend.view.startup;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import backend.Learner;
import backend.database.StorageService;
import data.TimeOfDay;
import data.UnavailableBlock;
import frontend.Utils;
import frontend.view.CButton;
import frontend.view.settings.SettingsConstants;
import frontend.view.startup.timepicker.SurveyWeekView;

public class SurveyView extends JDialog {
	
	private static final long			serialVersionUID	= -3311099840458252581L;
	
	/* Styling vars */
	private static final Dimension		minimum_size		= new Dimension(675, 670);
	private static final Insets			insets				= new Insets(6, 2, 6, 2);
	private static final Insets			leftInsets			= new Insets(8, 30, 8, 0);
	private static final Insets			rightInsets			= new Insets(8, 0, 8, 30);
	private static final int			padding				= 15;
	private static final int			title_size			= 46;
	
	/* String vars */
	private static final String			title_label			= "Welcome to Carly!";
	private static final String			hours_label			= "When do you prefer to work during the day?";
	private static final String			learner_label		= "Would you like settings to be adjusted based on your behavior?";
	private static final String			time_label			= "Please drag for the times you are unavailable during the average week.";
	private static final String			submit_label		= "Let's get started!";
	
	/* Input fields */
	private final JComboBox<TimeOfDay>	_todPicker;
	private final JCheckBox				_learnerCheck;
	private final SurveyWeekView		_timeView;
	
	/* Boolean vars */
	private boolean						_submitted			= false;
	
	public SurveyView() {
		super();
		setMinimumSize(minimum_size);
		setResizable(false);
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
		titleLabel.setFont(Utils.getFont(Font.BOLD, title_size));
		titleLabel.setForeground(Utils.COLOR_ACCENT);
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
		c.anchor = GridBagConstraints.WEST;
		c.insets = leftInsets;
		this.add(todLabel, c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = rightInsets;
		this.add(_todPicker, c);
		
		/* "Would you like settings to be adjusted based on your behavior?" */
		final JLabel learnerLabel = new JLabel(learner_label);
		_learnerCheck = new JCheckBox();
		_learnerCheck.setSelected(true); // Starts as true
		Utils.themeComponent(learnerLabel);
		c.gridx = 0;
		c.gridy = ycount += 1; // 3
		c.anchor = GridBagConstraints.WEST;
		c.insets = leftInsets;
		this.add(learnerLabel, c);
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = rightInsets;
		this.add(_learnerCheck, c);
		
		/* "Please drag for unavailable times during average week" */
		final JLabel timeLabel = new JLabel(time_label);
		_timeView = new SurveyWeekView();
		Utils.themeComponent(timeLabel);
		c.gridx = 0;
		c.gridy = ycount += 1; // 4
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = leftInsets;
		this.add(timeLabel, c);
		c.gridx = 0;
		c.gridy = ycount += 1; // 5
		c.gridwidth = 2;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = insets;
		this.add(_timeView, c);
		
		/* 'Submit survey' button */
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
		
		/* === WindowListener: Handles case in which user closes without submitting === */
		
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(final WindowEvent e) {}
			
			// Triggered on 'close' or on 'submit' (submitSurvey's dispose() call)
			@Override
			public void windowClosing(final WindowEvent e) {
				// User closes without submitting
				if (!_submitted) {
					// Delete database and quit program
					StorageService.dropTables();
					System.exit(0);
				}
			}
			
			@Override
			public void windowClosed(final WindowEvent e) {}
			
			@Override
			public void windowIconified(final WindowEvent e) {}
			
			@Override
			public void windowDeiconified(final WindowEvent e) {}
			
			@Override
			public void windowActivated(final WindowEvent e) {}
			
			@Override
			public void windowDeactivated(final WindowEvent e) {}
		});
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
		// Set boolean of Learner
		Learner.setEnabled(learnerPref);
		
		// Add UnavailableBlocks to StorageService
		final List<UnavailableBlock> uBlocks = _timeView.getUnavailableBlocks();
		StorageService.addAllDefaultUnavailableBlocks(uBlocks);
		
		// Indicate to WindowListener that submission was successful
		_submitted = true;
		
		// Close window without quitting program
		dispose();
	}
}
