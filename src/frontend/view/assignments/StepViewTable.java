package frontend.view.assignments;

import hub.HubController;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
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
	private Integer				_hoveredRow;
	private boolean				_moveWithMouse;
	private Point				_mousePoint;
	
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
		canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		canvas.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
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
		
		// If being hovered, paint row cover
		if (_hoveredRow != null) {
			drawHoveredRow(canvas);
		}
	}
	
	/**
	 * Draws the hover on a given row
	 * 
	 * @param canvas the canvas object
	 */
	private void drawHoveredRow(final Graphics2D canvas) {
		final int h = getRowHeight();
		final int w = getWidth();
		final int y = _hoveredRow * h;
		final int endY = y + h - 1;
		canvas.setColor(getBackground());
		canvas.fillRect(0, y, w, h);
		
		// Completed bar
		final double perc = ((ITask) getValueAt(_hoveredRow, 0)).getPercentComplete();
		canvas.setColor(Utils.COLOR_ALTERNATE.brighter());
		canvas.fillRect(0, y, (int) (perc * w), h);
		
		// Calculations for the text
		int textX = (int) (perc * w) - 30;
		Color c = Utils.COLOR_FOREGROUND;
		String content = (int) (perc * 100) + "%";
		if (perc == 0) {
			textX = 0;
			content = "Click to set % complete";
			c = Utils.COLOR_ACCENT;
		}
		
		// Draw hover
		if (_moveWithMouse) {
			final double hover = _mousePoint.getX();
			canvas.setColor(Utils.COLOR_ACCENT);
			canvas.fillRect(0, y, (int) hover, h);
			textX = (int) hover - 30;
			content = (int) ((hover / w) * 100) + "%";
			c = Utils.COLOR_BACKGROUND;
		}
		canvas.setFont(Utils.getFont(Font.BOLD, 11));
		canvas.setColor(c);
		canvas.drawString(content, textX, endY - 5);
		
		// Line at bottom
		canvas.setColor(Utils.COLOR_FOREGROUND);
		canvas.drawLine(0, endY, w, endY);
	}
	
	/**
	 * Gets the row that corresponds to a point (0 indexed
	 * 
	 * @param point the point in 2D space
	 * @return an int representing the row
	 */
	private int getRowForPoint(final Point point) {
		return (int) (point.getY() / getHeight() * getRowCount());
	}
	
	@Override
	public void mouseDragged(final MouseEvent e) {}
	
	@Override
	public void mouseMoved(final MouseEvent e) {
		_mousePoint = e.getPoint();
		final int row = getRowForPoint(_mousePoint);
		if (_hoveredRow == null || row != _hoveredRow) {
			_hoveredRow = row;
			_moveWithMouse = false;
		}
		repaint();
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {
		if (_moveWithMouse && _mousePoint != null) {
			final double percent = _mousePoint.getX() / getWidth();
			System.out.println(percent);
			((ITask) getValueAt(_hoveredRow, 0)).setPercentComplete(percent);
			
			//TODO: EVAN ADDED THIS
			HubController.changeTask(_assignment.getTasks().get(_hoveredRow), percent);
		}
		_moveWithMouse = !_moveWithMouse;
		repaint();
		
	}
	
	@Override
	public void mousePressed(final MouseEvent e) {}
	
	@Override
	public void mouseReleased(final MouseEvent e) {}
	
	@Override
	public void mouseEntered(final MouseEvent e) {}
	
	@Override
	public void mouseExited(final MouseEvent e) {
		_hoveredRow = null;
		repaint();
	}
}
