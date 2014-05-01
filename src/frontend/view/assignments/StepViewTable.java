package frontend.view.assignments;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import data.IAssignment;
import data.ITask;
import frontend.Utils;
import frontend.view.CanvasUtils;

public class StepViewTable extends JTable implements MouseListener, MouseMotionListener {
	
	private static final long	serialVersionUID	= 1L;
	private IAssignment			_assignment;
	
	/**
	 * Just sets up the look
	 * 
	 * @param model the model to run this view
	 * @param showProgress whether the view should show progress on hover
	 */
	public StepViewTable(final StepModel model, final boolean showProgress) {
		super(model);
		
		// Hover
		if (showProgress) {
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		
		// Look and feel
		setShowHorizontalLines(true);
		setShowVerticalLines(false);
		setAutoCreateColumnsFromModel(true);
		setRowMargin(5);
		setRowHeight(24);
		setGridColor(Color.white);
		setSelectionForeground(Utils.COLOR_FOREGROUND);
		setSelectionBackground(Utils.COLOR_BACKGROUND);
		Utils.themeComponent(this);
	}
	
	/**
	 * Sets up the look and includes an assignment to use in drawing colors later
	 * 
	 * @param model the model to run this view
	 * @param assignment the assignment this represents
	 * @param showProgress whether the view should show progress on hover
	 */
	public StepViewTable(final StepModel model, final IAssignment assignment, final boolean showProgress) {
		this(model, showProgress);
		_assignment = assignment;
	}
	
	@Override
	public JTableHeader getTableHeader() {
		final JTableHeader ret = super.getTableHeader();
		Utils.themeComponent(ret);
		return ret;
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Graphics2D canvas = (Graphics2D) g;
		if (_assignment != null) {
			for (int i = 0; i < _assignment.getTasks().size(); i++) {
				final ITask t = _assignment.getTasks().get(i);
				final Color c = CanvasUtils.getColor(t);
				final Rectangle2D.Double rect = new Rectangle2D.Double(getWidth() - 17, Math.round(i * 24) + 5, 15, 15);
				canvas.setColor(c);
				canvas.fill(rect);
				canvas.setColor(Utils.COLOR_LIGHT_BG);
				canvas.draw(rect);
			}
		}
	}
	
	/**
	 * Gets the row that corresponds to a point (0 indexed
	 * 
	 * @param point the point in 2D space
	 * @return an int representing the row
	 */
	private int getRowForPoint(final Point point) {
		return getHeight() / getRowCount() / (getRowHeight() + getRowMargin());
	}
	
	@Override
	public void mouseDragged(final MouseEvent e) {}
	
	@Override
	public void mouseMoved(final MouseEvent e) {
		System.out.println("Index: " + getRowForPoint(e.getPoint()));
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {}
	
	@Override
	public void mousePressed(final MouseEvent e) {}
	
	@Override
	public void mouseReleased(final MouseEvent e) {}
	
	@Override
	public void mouseEntered(final MouseEvent e) {}
	
	@Override
	public void mouseExited(final MouseEvent e) {}
}
