package frontend.view;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;

import frontend.Utils;

/**
 * Just a simple class that actually implements scrollable so it doesn't move by just one tick
 * 
 * @author dgattey
 */
public class ScrollablePanel extends JPanel implements Scrollable {
	
	private static final long	serialVersionUID	= 1L;
	
	/**
	 * Constructor sets keyboard shortcuts plus UI
	 * 
	 * @param scroll the panel that this will be added to to scroll
	 */
	public ScrollablePanel(final JScrollPane scroll) {
		
		// Themeing
		scroll.getVerticalScrollBar().setUI(new CScrollBarUI());
		scroll.getHorizontalScrollBar().setUI(new CScrollBarUI());
		Utils.themeComponent(this);
		Utils.padComponent(this, 0, 0);
		Utils.themeComponent(scroll);
		Utils.padComponent(scroll, 0, 0);
		Utils.themeComponent(scroll.getViewport());
		scroll.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		
		// Scrolls the view up and down with arrows
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "up");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "down");
		getActionMap().put("up", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Rectangle newR = getVisibleRect();
				newR.translate(0, -30);
				scrollRectToVisible(newR);
			}
		});
		getActionMap().put("down", new AbstractAction() {
			
			private static final long	serialVersionUID	= 1L;
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Rectangle newR = getVisibleRect();
				newR.translate(0, 30);
				scrollRectToVisible(newR);
			}
		});
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}
	
	@Override
	public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
		return 10;
	}
	
	@Override
	public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
		return 10;
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
