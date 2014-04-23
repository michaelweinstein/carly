package frontend.view.calendar;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import frontend.Utils;

/**
 * Represents a week in the calendar with events throughout the day, every day
 * 
 * @author dgattey
 */
public class WeekView extends JScrollPane {
	
	private static final long	serialVersionUID	= -4485951680510823881L;
	private JPanel				_timePanel;
	private JPanel				_centerPanel;
	private final JSplitPane	_parentContainer;
	
	public WeekView() {
		_parentContainer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createLeftPane(), createCenterPane());
		setViewportView(_parentContainer);
		
		themeAll();
	}
	
	/**
	 * Themes everything in scope
	 */
	private void themeAll() {
		
		Utils.themeComponent(this);
		Utils.themeComponent(getViewport());
		Utils.themeComponent(_parentContainer);
		Utils.themeComponent(_centerPanel);
		Utils.themeComponentInverse(_timePanel);
		
		setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		Utils.padComponent(this, 0, 0);
		Utils.padComponent(_parentContainer, 0, 0);
		Utils.padComponent(_centerPanel, 0, 0);
		Utils.padComponent(_timePanel, 0, 0);
	}
	
	/**
	 * Makes the main calendar pane
	 * 
	 * @return a new pane for the week view
	 */
	private JPanel createCenterPane() {
		_centerPanel = new JPanel();
		return _centerPanel;
	}
	
	/**
	 * Makes the time pane on the left
	 * 
	 * @return a new pane for the time list
	 */
	private JPanel createLeftPane() {
		_timePanel = new JPanel();
		_timePanel.setLayout(new BoxLayout(_timePanel, BoxLayout.Y_AXIS));
		_timePanel.setAlignmentX(LEFT_ALIGNMENT);
		_timePanel.setAlignmentY(CENTER_ALIGNMENT);
		final Font f = new Font(Utils.APP_FONT_NAME, Font.BOLD, 10);
		for (int i = 0; i < 24; i++) {
			final JLabel l = new JLabel(i + ":00");
			Utils.themeComponentInverse(l);
			Utils.padComponent(l, 5, 0);
			l.setFont(f);
			_timePanel.add(l);
			if (i != 23) {
				_timePanel.add(Box.createVerticalStrut(100));
			}
		}
		return _timePanel;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(600, 400);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(400, 300);
	}
}
