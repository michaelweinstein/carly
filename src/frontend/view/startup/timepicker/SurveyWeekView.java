package frontend.view.startup.timepicker;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;

import data.UnavailableBlock;
import frontend.Utils;

public class SurveyWeekView extends JPanel {
	
	// TODO: Drag between boxes; select any between the start and end drag
	
	private static final long			serialVersionUID	= 888327935955233878L;
	
	/* Dimensional vals */
	private static final Dimension		size				= new Dimension(540, 380);
	private static final int			X_INSET				= 35;
	private static final int			Y_INSET				= 40;
	private static final double			NUM_COLS			= 7.0;
	private static final double			NUM_ROWS			= 48.0;								// every half hour
	public static final double			COL_WIDTH			= (size.width - X_INSET) / NUM_COLS;
	public static final double			ROW_HEIGHT			= (size.height - Y_INSET) / NUM_ROWS;
	
	/* Instance vars */
	private final List<SurveyTimeBlock>	_blocks;
	private SurveyTimeBlock				_currBlock;
	private Boolean						_currSelectedVal;
	
	public SurveyWeekView() {
		super();
		Utils.themeComponentLight(this);
		setPreferredSize(size);
		_blocks = createBlocks();
		_currBlock = _blocks.get(0);
		_currSelectedVal = true;
		addMouseListener(new BlockMouseListener());
		addMouseMotionListener(new BlockDragListener());
		repaint();
		
		// TODO: Add a 'Clear' button
	}
	
	/* Private util methods */
	
	/**
	 * Creates a SurveyTimeBlock for each half hour of each day of the week. Location is based on the block's row and
	 * col, then passed into constructor.
	 * 
	 * @return list of all SurveyTimeBlocks in week
	 */
	private static List<SurveyTimeBlock> createBlocks() {
		final List<SurveyTimeBlock> blocks = new ArrayList<>();
		// for each day
		for (int i = 0; i < NUM_COLS; i++) {
			// for each half hour
			for (int j = 0; j < NUM_ROWS; j++) {
				// params: whether block starts on the half hour
				blocks.add(new SurveyTimeBlock(i * COL_WIDTH + X_INSET, j * ROW_HEIGHT + Y_INSET / 2, !isEven(j)));
			}
		}
		return blocks;
	}
	
	/**
	 * Returns whether specified int is even.
	 * 
	 * @param num to check parity of
	 * @return whether or not num is even
	 */
	private static boolean isEven(final double num) {
		return num % 2 == 0;
	}
	
	/**
	 * Finds the SurveyTimeBlock in _blocks that contains the specified point, stored as a Vec2d. <br>
	 * Runs in O(n)
	 * 
	 * @param double1 location of cursor/point
	 * @return block containing loc
	 */
	private SurveyTimeBlock findBlockAt(final Point2D.Double double1) {
		for (final SurveyTimeBlock block : _blocks) {
			if (block.contains(double1)) {
				return block;
			}
		}
		return null;
	}
	
	/* Data Access methods */
	
	/**
	 * Returns every block (48 * 7)
	 * 
	 * @return List, all <code>SurveyTimeBlock</code> instances; selected && unselected
	 */
	public List<SurveyTimeBlock> getAllBlocks() {
		return _blocks;
	}
	
	/**
	 * Returns list of all blocks not selected by user. <br>
	 * Creates <code>UnavailableBlock</code> for each unselected <code>SurveyTimeBlock</code>.
	 * 
	 * @return list of <code>UnavailableBlocks</code> for any <code>SurveyTimeBlocks</code> not selected
	 */
	public List<UnavailableBlock> getUnavailableBlocks() {
		final List<UnavailableBlock> ublockslist = new ArrayList<>();
		for (final SurveyTimeBlock b : _blocks) {
			// If not selected
			if (b.isSelected()) {
				final Date[] times = b.getRange();
				// Create UnavailableBlock and add to returned list
				ublockslist.add(new UnavailableBlock(times[0], times[1]));
			}
		}
		return ublockslist;
	}
	
	/* Paint methods */
	
	/**
	 * Paints every SurveyTimeBlock within _blocks. Color and border stroke details are handled in SurveyTimeBlock's
	 * <code>draw(Graphics2D)</code>.
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// Clears background and draws full border
		g2.setBackground(Utils.COLOR_BACKGROUND);
		g2.clearRect(0, 0, getWidth(), getHeight());
		g2.setColor(SurveyTimeBlock.BORDER_COLOR);
		g2.drawRect(X_INSET, Y_INSET / 2, getWidth() - X_INSET - 1, getHeight() - Y_INSET - 1);
		
		// Draws num on left
		g2.setFont(Utils.getFont(Font.BOLD, 10));
		g2.setColor(Utils.COLOR_FOREGROUND);
		for (int i = 0; i <= NUM_ROWS; i += 4) {
			g2.drawString(Utils.getHourString(i / 2), 0, Y_INSET / 2 + (int) (i * ROW_HEIGHT) + 4);
		}
		
		// Draws day of week on top
		g2.setFont(Utils.getFont(Font.BOLD, 12));
		final String[] days = { "Sun", "Mon", "Tues", "Wed", "Thu", "Fri", "Sat" };
		for (int i = 0; i < days.length; i++) {
			g2.drawString(days[i], X_INSET + 5 + (int) (i * COL_WIDTH), 15);
		}
		
		// Draw all blocks
		for (final SurveyTimeBlock b : _blocks) {
			b.draw(g2, getVisibleRect());
		}
	}
	
	/* Handle user input methods */
	
	/**
	 * Gets SurveyTimeBlock that contains the specified Vec2d loc coordinates, and then sets that block's
	 * <code> _selected </code> boolean. <br>
	 * Only sets selected if the loc is at a new block, so blocks values are not toggled when user drags within its
	 * boundaries. <br>
	 * On user's initial mouse down when dragging, indicated by <code>_initialMouseDown</code>, stores selected boolean.
	 * All subsequent blocks in this drag are then set to the same <code>selected</code> value as the initial block.
	 * 
	 * @param p point of mouse click
	 */
	private void handleMouse(final Point2D.Double p) {
		final SurveyTimeBlock block = findBlockAt(p);
		if (block != null) {
			if (_currBlock == null) {
				_currBlock = block;
				_currSelectedVal = !_currBlock.isSelected();
				_currBlock.hover(false);
			} else {
				
				// Deals with movements that pass through blocks accidentally
				if (Math.abs(block.y - _currBlock.y) > 1) {
					double start = block.y;
					double end = _currBlock.y;
					if (start > end) {
						start = end;
						end = block.y;
					}
					for (double i = start; i <= end; i += ROW_HEIGHT) {
						final SurveyTimeBlock b = findBlockAt(new Point2D.Double(_currBlock.x + 1, i));
						if (b != null) {
							b.setSelected(_currSelectedVal);
						}
					}
				}
			}
			block.setSelected(_currSelectedVal);
			_currBlock = block;
			repaint();
		}
	}
	
	/**
	 * Hovers the given block
	 * 
	 * @param p the point where the block should be done
	 */
	private void handleHover(final Point p) {
		for (final SurveyTimeBlock block : _blocks) {
			if (block.contains(p)) {
				if (block != _currBlock) {
					block.hover(true);
					_currBlock = block;
				}
			} else {
				block.hover(false);
			}
		}
		repaint();
	}
	
	/* Private inner classes (for user input) */
	
	private class BlockDragListener implements MouseMotionListener {
		
		@Override
		public void mouseDragged(final MouseEvent e) {
			handleMouse(new Point2D.Double(e.getX(), e.getY()));
		}
		
		@Override
		public void mouseMoved(final MouseEvent e) {
			handleHover(e.getPoint());
		}
	}
	
	/**
	 * Private inner class, handles a single click by user on the SurveyWeekView panel.
	 */
	private class BlockMouseListener implements MouseListener {
		
		@Override
		public void mousePressed(final MouseEvent e) {
			_currSelectedVal = null;
			_currBlock = null;
			handleMouse(new Point2D.Double(e.getX(), e.getY()));
		}
		
		@Override
		public void mouseReleased(final MouseEvent e) {}
		
		@Override
		public void mouseClicked(final MouseEvent e) {}
		
		@Override
		public void mouseEntered(final MouseEvent e) {}
		
		@Override
		public void mouseExited(final MouseEvent e) {
			if (_currBlock != null) {
				_currBlock.hover(false);
				repaint();
			}
		}
	}
}
