package frontend.view;

import java.awt.Dimension;

import javax.swing.JPanel;

import frontend.FrontUtilities;

/**
 * Represents the toolbar at top of the screen
 * 
 * @author dgattey
 */
public class ToolbarView extends JPanel {
	
	private static final long	serialVersionUID	= -2158045975284361590L;
	
	public ToolbarView() {
		FrontUtilities.themeComponent(this);
		FrontUtilities.addBorderBottom(this);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(300, 80);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(300, 80);
	}
}
