package frontend.view.calendar;

import java.awt.Dimension;

import javax.swing.JPanel;

import frontend.Utils;

/**
 * Represents a continuous line of calendar data
 * 
 * @author dgattey
 */
public class LineView extends JPanel {
	
	private static final long	serialVersionUID	= 8788849553807412908L;
	
	public LineView() {
		Utils.themeComponent(this);
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
