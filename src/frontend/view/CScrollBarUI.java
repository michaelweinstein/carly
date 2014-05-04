package frontend.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

import frontend.Utils;

/**
 * Custom scroll bar
 * 
 * @author dgattey
 */
public class CScrollBarUI extends BasicScrollBarUI {
	
	@Override
	protected JButton createDecreaseButton(final int orientation) {
		return new JButton() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			protected void paintComponent(final Graphics g) {
				final Graphics2D canvas = (Graphics2D) g;
				drawTriangle(canvas, orientation, getWidth(), getHeight());
			}
			
		};
	}
	
	@Override
	protected JButton createIncreaseButton(final int orientation) {
		return new JButton() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			protected void paintComponent(final Graphics g) {
				final Graphics2D canvas = (Graphics2D) g;
				drawTriangle(canvas, orientation, getWidth(), getHeight());
			}
			
		};
	}
	
	/**
	 * Draws a triangle to canvas
	 * 
	 * @param canvas the graphics object to draw on
	 * @param orientation an orientation direction
	 * @param width the component width
	 * @param height the component height
	 */
	protected void drawTriangle(final Graphics2D canvas, final int orientation, final int width, final int height) {
		canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		canvas.setBackground(Utils.COLOR_BACKGROUND);
		canvas.clearRect(0, 0, width, height);
		canvas.setColor(Utils.COLOR_ACCENT);
		
		final int[] xPoints = new int[3];
		final int[] yPoints = new int[3];
		switch (orientation) {
		case NORTH:
			xPoints[0] = 2;
			xPoints[1] = width / 2;
			xPoints[2] = width - 2;
			yPoints[0] = height - 8;
			yPoints[1] = height - width - 6;
			yPoints[2] = height - 8;
			break;
		case SOUTH:
			xPoints[0] = 2;
			xPoints[1] = width / 2;
			xPoints[2] = width - 2;
			yPoints[0] = 8;
			yPoints[1] = width + 6;
			yPoints[2] = 8;
			break;
		case WEST:
			break;
		case EAST:
			break;
		default:
			break;
		}
		canvas.fillPolygon(xPoints, yPoints, 3);
	}
	
	@Override
	protected void paintTrack(final Graphics g, final JComponent c, final Rectangle trackBounds) {
		g.setColor(Utils.COLOR_BACKGROUND);
		g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
		
		if (trackHighlight == DECREASE_HIGHLIGHT) {
			paintDecreaseHighlight(g);
		} else if (trackHighlight == INCREASE_HIGHLIGHT) {
			paintIncreaseHighlight(g);
		}
	}
	
	@Override
	protected void paintThumb(final Graphics g, final JComponent c, final Rectangle thumbBounds) {
		final Graphics2D canvas = (Graphics2D) g;
		canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		canvas.setColor(Utils.COLOR_LIGHT_BG);
		final RoundRectangle2D.Double rect = new RoundRectangle2D.Double(thumbBounds.x + 2, thumbBounds.y,
				thumbBounds.width - 4, thumbBounds.height, 20, 20);
		canvas.fill(rect);
	}
}