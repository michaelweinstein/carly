package frontend.view.calendar;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import frontend.Utils;

/**
 * Button that makes an arrow pointing either right or left
 * 
 * @author dgattey
 */
public abstract class ArrowButton extends JComponent implements MouseListener {
	
	private static final long		serialVersionUID	= 1L;
	private static final Dimension	SIZE_D				= new Dimension(30, 40);
	private final Direction			_direction;
	
	/**
	 * For the direction the button is pointing
	 * 
	 * @author dgattey
	 */
	public enum Direction {
		LEFT_BUTTON, RIGHT_BUTTON;
	}
	
	/**
	 * Creates a button pointing in a direction
	 * 
	 * @param dir the direction
	 */
	public ArrowButton(final Direction dir) {
		super();
		enableInputMethods(true);
		addMouseListener(this);
		_direction = dir;
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Graphics2D canvas = (Graphics2D) g;
		canvas.setColor(Utils.COLOR_ACCENT);
		canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		final int[] xPoints = { 0, 24, 30, 12, 30, 24 };
		final int[] yPoints = { 20, 0, 5, 20, 35, 40 };
		if (_direction == Direction.RIGHT_BUTTON) {
			for (int i = 0; i < xPoints.length; i++) {
				final int item = xPoints[i];
				xPoints[i] = (item * -1) + 30;
			}
		}
		canvas.fill(new Polygon(xPoints, yPoints, 6));
	}
	
	@Override
	public void mousePressed(final MouseEvent e) {}
	
	@Override
	public void mouseReleased(final MouseEvent e) {}
	
	@Override
	public void mouseEntered(final MouseEvent e) {}
	
	@Override
	public void mouseExited(final MouseEvent e) {}
	
	@Override
	public Dimension getPreferredSize() {
		return SIZE_D;
	}
	
	@Override
	public Dimension getMinimumSize() {
		return SIZE_D;
	}
	
	@Override
	public Dimension getMaximumSize() {
		return SIZE_D;
	}
}
