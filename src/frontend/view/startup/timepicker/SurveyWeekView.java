package frontend.view.startup.timepicker;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;

import data.UnavailableBlock;
import data.Vec2d;
import frontend.Utils;

public class SurveyWeekView extends JPanel {
	
	// TODO: BUG: Sometimes drags selected over selected, and unselected over unselected
		// Trigger by dragging some blocks and trying to deselect them by dragging a subgroup of those blocks
	// TODO:  Upper left corner border of first box!
	// TODO: When you drag, the first box does not get selected
		// TODO: Drag between boxes; select any between the start and end drag

	private static final long serialVersionUID = 888327935955233878L;
	
	/* Dimensional vals */
	private static final Dimension size = new Dimension(450, 400);
	private static final double numCols = 7;
	private static final double numRows = 48;	// every half hour
	// public vals (accessed by SurveyTimeBlock)
	public static final double COL_WIDTH =  size.width/numCols;
	public static final double ROW_HEIGHT = size.height/numRows;
	
	/* Instance vars */
	private List<SurveyTimeBlock> _blocks;
	private SurveyTimeBlock _currBlock;
	private boolean _currSelectedVal;
	private boolean _initialMouseDown;
	
	public SurveyWeekView() {
		super();
		Utils.themeComponentLight(this);
		setPreferredSize(size);
		// Initialize vars
		_blocks = createBlocks();
		_currBlock = _blocks.get(0);
		_currSelectedVal = true;
		_initialMouseDown = true;
		// Add listeners for user input
		this.addMouseListener(new BlockMouseListener());
		this.addMouseMotionListener(new BlockDragListener());
		repaint();
		
		// TODO: Add a 'Clear' button
	}
	
	/* Private util methods */
	
	/**
	 * Creates a SurveyTimeBlock for each half hour of
	 * each day of the week. Location is based on the
	 * block's row and col, then passed into constructor.
	 * 
	 * @return list of all SurveyTimeBlocks in week
	 */
	private static List<SurveyTimeBlock> createBlocks() {
		List<SurveyTimeBlock> blocks = new ArrayList<>();
		// for each day
		for (int i=0; i<numCols; i++) {
			// for each half hour
			for (int j=0; j<numRows; j++) {
				// params: whether block starts on the half hour, whether upper-left first block
				blocks.add(new SurveyTimeBlock(i*COL_WIDTH, j*ROW_HEIGHT, !isEven(j), i==0 && j==0));
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
	private static boolean isEven(int num) {
		return num%2==0;
	}
	
	/**
	 * Finds the SurveyTimeBlock in _blocks that contains
	 * the specified point, stored as a Vec2d. <br> 
	 * Runs in O(n)
	 * 
	 * @param loc, location of cursor/point
	 * @return block containing loc
	 */
	private SurveyTimeBlock findBlockAt(Vec2d loc) {
		Point2D p = new Point2D.Double(loc.x, loc.y);
		for (SurveyTimeBlock block: _blocks) {
			if (block.contains(p)) {
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
	 * Creates <code>UnavailableBlock</code> for each 
	 * unselected <code>SurveyTimeBlock</code>.
	 * 
	 * @return list of <code>UnavailableBlocks</code> 
	 * 		for any <code>SurveyTimeBlocks</code> not selected
	 */
	public List<UnavailableBlock> getUnavailableBlocks() {
		List<UnavailableBlock> ublockslist = new ArrayList<>();
		for (SurveyTimeBlock b: _blocks) {
			// If not selected
			if (b.isSelected()) {
				Date[] times = b.getRange();
				// Create UnavailableBlock and add to returned list
				ublockslist.add(new UnavailableBlock(times[0], times[1], null));
			}
		}
		return ublockslist;
	}
	
	/* Paint methods */
	
	/**
	 * Paints every SurveyTimeBlock within _blocks.
	 * Color and border stroke details are handled
	 * in SurveyTimeBlock's <code>draw(Graphics2D)</code>.
	 */
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		// Draw all blocks
		for (SurveyTimeBlock b: _blocks) {
			b.draw(g2);
		}
	}
	
	/* Handle user input methods */
	
	/**
	 * Gets SurveyTimeBlock that contains the specified
	 * Vec2d loc coordinates, and then sets that block's
	 * <code> _selected </code> boolean. <br>
	 * 
	 * Only sets selected if the loc is at a new block,
	 * so blocks values are not toggled when user drags 
	 * within its boundaries. <br>
	 * 
	 * On user's initial mouse down when dragging, indicated
	 * by <code>_initialMouseDown</code>, stores selected boolean.
	 * All subsequent blocks in this drag are then set to the same 
	 * <code>selected</code> value as the initial block.
	 * 
	 * @param loc of mouse click Vec2d 
	 */
	private void handleMouse(Vec2d loc) {
		SurveyTimeBlock block = findBlockAt(loc);
		if (block != _currBlock) {
			if (block != null) {
				if (_initialMouseDown)
					_currSelectedVal = !(block.isSelected());
				block.setSelected(_currSelectedVal);
				repaint();
			}
			_currBlock = block;
		}
	}
	
	// TODO Complete and comment
	private void handleHover(Vec2d loc) {
		Point2D p = new Point2D.Double(loc.x, loc.y);
		for (SurveyTimeBlock block: _blocks) {
			if (block.contains(p)) {
				if (block != _currBlock) {
					if (block != null) {
						block.hover(true);
						repaint();
					}
					_currBlock = block;
				} 
			} 
			else block.hover(false);
		}
				
	}
	
	/* Private inner classes (for user input) */
	
	private class BlockDragListener implements MouseMotionListener {
		/**
		 * After handling mouse click once, set _initialMouseDown 
		 * to false to indicate that subsequent calls are not
		 * on the user's initial mouse down for this drag. 
		 * Reset on release.
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			handleMouse(new Vec2d(e.getX(), e.getY()));
			_initialMouseDown = false;
		}
		@Override
		public void mouseMoved(MouseEvent e) { 
			// TODO: Block hover response
			handleHover(new Vec2d(e.getX(), e.getY()));
		}
	}
	
	/**
	 * Private inner class, handles a single click by user
	 * on the SurveyWeekView panel.
	 */
	private class BlockMouseListener implements MouseListener {
		@Override
		public void mousePressed(MouseEvent e) {
			handleMouse(new Vec2d(e.getX(), e.getY()));
		}
		/**
		 * Indicate that the next click is an initial 
		 * mouse down click in the drag. 
		 */
		@Override
		public void mouseReleased(MouseEvent e) { 
			_initialMouseDown= true;
		}
		@Override
		public void mouseClicked(MouseEvent e) { }
		@Override
		public void mouseEntered(MouseEvent e) { }
		@Override
		public void mouseExited(MouseEvent e) { }
	}
}
