package frontend.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;

import frontend.Utils;

/**
 * Custom button that paints differently
 * 
 * @author dgattey
 */
public class CButton extends JButton {
	
	private static final long	serialVersionUID	= 1L;
	
	/**
	 * Constructor with title
	 * 
	 * @param name a title
	 */
	public CButton(final String name) {
		super(name);
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D canvas = (Graphics2D) g;
		canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		canvas.setStroke(new BasicStroke(1));
		canvas.setFont(Utils.getFont(Font.BOLD, 13));
		
		// Draw the background
		final RoundRectangle2D.Double back = new RoundRectangle2D.Double(3, 3, getWidth() - 4, getHeight() - 4, 10, 10);
		canvas.setColor(new Color(30, 30, 30));
		canvas.fill(back);
		canvas.setColor(Utils.COLOR_ACCENT);
		canvas.draw(back);
		
		// Draw text
		canvas.setColor(Utils.COLOR_FOREGROUND);
		final String s = getText();
		final int w = getWidth();
		final int x = (int) ((w - canvas.getFontMetrics().getStringBounds(s, g).getWidth()) / 2);
		canvas.drawString(s, x, getHeight() / 2 + 6);
	};
}
