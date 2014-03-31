package frontend.view;

import java.awt.Dimension;

import javax.swing.JPanel;

import frontend.Utilities;

/**
 * Represents a continuous line of calendar data
 * 
 * @author dgattey
 */
public class LineView extends JPanel {
	
	private static final long	serialVersionUID	= 8788849553807412908L;
	
	public LineView() {
		Utilities.themeComponent(this);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(300, 200);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(300, 100);
	}
}
