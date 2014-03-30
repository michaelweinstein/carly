package frontend.view;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import frontend.FrontUtilities;

/**
 * Represents the panel holding the line and week views for the calendar
 * 
 * @author dgattey
 */
public class CalendarView extends JPanel {
	
	private final LineView		lineView;
	private final WeekView		weekView;
	
	private static final long	serialVersionUID	= -1015403696639767751L;
	
	public CalendarView() {
		lineView = new LineView();
		weekView = new WeekView();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		FrontUtilities.themeComponentInverse(this);
		
		add(lineView);
		add(Box.createVerticalStrut(10));
		add(weekView);
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
