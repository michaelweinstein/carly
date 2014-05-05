package frontend.view;

import java.awt.Color;
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
 * Custom scroll bar UI to match rest of UI
 * 
 * @author dgattey
 */
public class CScrollBarUI extends BasicScrollBarUI {
	
	private final Color	_strokeColor;
	
	/**
	 * Constructor that sets a default stroke color
	 */
	public CScrollBarUI() {
		_strokeColor = Utils.COLOR_BACKGROUND;
	}
	
	/**
	 * Constructor that sets a custom stroke color
	 * 
	 * @param c the stroke color to use
	 */
	public CScrollBarUI(final Color c) {
		_strokeColor = c;
	}
	
	@Override
	protected JButton createDecreaseButton(final int orientation) {
		return new ScrollButton(orientation);
	}
	
	@Override
	protected JButton createIncreaseButton(final int orientation) {
		return new ScrollButton(orientation);
	}
	
	@Override
	protected void paintTrack(final Graphics g, final JComponent c, final Rectangle trackBounds) {
		
		// Draws the background always in background color, then a stroke in the given color
		g.setColor(Utils.COLOR_BACKGROUND);
		g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
		g.setColor(_strokeColor);
		g.drawRect(trackBounds.x, trackBounds.y, trackBounds.width - 1, trackBounds.height - 1);
		
		// Draws standard track highlights
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
		
		// Draw a round rect for the thumb
		final RoundRectangle2D.Double rect = new RoundRectangle2D.Double(thumbBounds.x + 2, thumbBounds.y,
				thumbBounds.width - 4, thumbBounds.height, 16, 16);
		canvas.fill(rect);
	}
	
	/**
	 * Class for the scroll buttons at either end of the scroll bar - draws triangles in the wanted colors
	 * 
	 * @author dgattey
	 */
	private class ScrollButton extends JButton {
		
		private static final long	serialVersionUID	= 1L;
		private final int			_orientation;
		
		/**
		 * Creates a button with the given orientation
		 * 
		 * @param orientation the int orientation (NORTH, SOUTH, EAST, or WEST)
		 */
		public ScrollButton(final int orientation) {
			_orientation = orientation;
		}
		
		@Override
		protected void paintComponent(final Graphics g) {
			final Graphics2D canvas = (Graphics2D) g;
			drawTriangle(canvas, _orientation, getWidth(), getHeight());
		}
		
		@Override
		protected void paintBorder(final Graphics g) {
			// Fixes Linux drawing bug to not paint
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
			
			// Paint a standard background
			canvas.setBackground(_strokeColor);
			canvas.clearRect(0, 0, width, height);
			canvas.setColor(Utils.COLOR_ACCENT);
			
			final int[] xPoints = new int[3];
			final int[] yPoints = new int[3];
			int offset = (int) (height * 0.2);
			switch (orientation) {
			case NORTH:
				xPoints[0] = 2;
				xPoints[1] = width / 2;
				xPoints[2] = width - 2;
				yPoints[0] = height - offset;
				yPoints[1] = height - width - offset;
				yPoints[2] = height - offset;
				break;
			case SOUTH:
				xPoints[0] = 2;
				xPoints[1] = width / 2;
				xPoints[2] = width - 2;
				yPoints[0] = offset;
				yPoints[1] = width + offset;
				yPoints[2] = offset;
				break;
			case WEST:
				offset = (int) (width * 0.2);
				xPoints[0] = width - offset;
				xPoints[1] = width - height - offset;
				xPoints[2] = width - offset;
				yPoints[0] = 2;
				yPoints[1] = height / 2;
				yPoints[2] = height - 2;
				break;
			case EAST:
				offset = (int) (width * 0.2);
				xPoints[0] = offset;
				xPoints[1] = height + offset;
				xPoints[2] = offset;
				yPoints[0] = 2;
				yPoints[1] = height / 2;
				yPoints[2] = height - 2;
				break;
			default:
				break;
			}
			canvas.fillPolygon(xPoints, yPoints, 3);
		}
	}
}