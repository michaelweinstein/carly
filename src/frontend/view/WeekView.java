package frontend.view;

import java.awt.Dimension;

import javax.swing.JPanel;

import frontend.FrontUtilities;

/**
 * Represents a week in the calendar with events throughout the day, every day
 * 
 * @author dgattey
 */
public class WeekView extends JPanel {
	
	private static final long	serialVersionUID	= -4485951680510823881L;
	
	public WeekView() {
		FrontUtilities.themeComponent(this);
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
