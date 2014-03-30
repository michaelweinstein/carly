package frontend.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

/**
 * Represents a continuous line of calendar data
 * 
 * @author dgattey
 */
public class LineView extends JPanel {
	
	private static final long	serialVersionUID	= 8788849553807412908L;
	
	public LineView() {
		setForeground(Color.BLUE);
		setBackground(Color.BLUE);
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
