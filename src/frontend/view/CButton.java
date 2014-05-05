package frontend.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;

import frontend.Utils;

/**
 * Custom button that paints differently
 * 
 * @author dgattey
 */
public class CButton extends JButton implements MouseListener {
	
	private static final long	serialVersionUID	= 1L;
	private boolean				_hovered;
	private boolean				_pressed;
	
	/**
	 * Constructor with title
	 * 
	 * @param name a title
	 */
	public CButton(final String name) {
		super(name);
		addMouseListener(this);
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
		final Color def = new Color(30, 30, 30);
		canvas.setColor(!isEnabled() ? def : _pressed ? Utils.COLOR_ALTERNATE.brighter() : _hovered ? new Color(50, 50,
				50) : def);
		canvas.fill(back);
		canvas.setColor(isEnabled() ? Utils.COLOR_ACCENT : Utils.COLOR_ALTERNATE);
		canvas.draw(back);
		
		// Draw text
		canvas.setColor(isEnabled() ? Utils.COLOR_FOREGROUND : Utils.COLOR_FOREGROUND.darker());
		final String s = getText();
		final int w = getWidth();
		final int x = (int) ((w - canvas.getFontMetrics().getStringBounds(s, g).getWidth()) / 2);
		canvas.drawString(s, x, getHeight() / 2 + 6);
	}
	
	@Override
	protected void paintBorder(final Graphics g) {
		// Don't draw border (resolves Linux bug)
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {}
	
	@Override
	public void mousePressed(final MouseEvent e) {
		_pressed = true;
		repaint();
	}
	
	@Override
	public void mouseReleased(final MouseEvent e) {
		_pressed = false;
		repaint();
	}
	
	@Override
	public void mouseEntered(final MouseEvent e) {
		_hovered = true;
		repaint();
	}
	
	@Override
	public void mouseExited(final MouseEvent e) {
		_hovered = false;
		repaint();
	}
	
	/**
	 * Resets colors
	 */
	public void reset() {
		_hovered = false;
		_pressed = false;
		repaint();
	}
}
