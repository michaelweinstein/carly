package frontend.view.assignments;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JProgressBar;

import frontend.Utils;

/**
 * Class that overrides painting for a progress bar
 * 
 * @author dgattey
 */
public class DProgressBar extends JProgressBar {
	
	private static final long	serialVersionUID	= 1L;
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D canvas = (Graphics2D) g;
		canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		canvas.setColor(Utils.COLOR_ALTERNATE);
		canvas.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
		canvas.setColor(Utils.COLOR_FOREGROUND);
		canvas.fill(new RoundRectangle2D.Double(0, 0, getPercentComplete() * getWidth(), getHeight(), 10, 10));
	}
}
