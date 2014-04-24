package frontend.view.calendar;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

import frontend.Utils;

/**
 * Represents a continuous line of calendar data
 * 
 * @author dgattey
 */
public class LineView extends JPanel {
	
	private final CalendarView	cv;
	private static final long	serialVersionUID	= 8788849553807412908L;
	
	public LineView(final CalendarView cv) {
		this.cv = cv;
		Utils.themeComponent(this);
		
		final JLabel weekView = new JLabel("Week of");
		
		add(weekView, BorderLayout.WEST);
		add(new LineCanvas(), BorderLayout.CENTER);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(300, 100);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(300, 100);
	}
}
