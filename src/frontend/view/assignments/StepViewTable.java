package frontend.view.assignments;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import data.IAssignment;
import data.ITask;
import frontend.Utils;
import frontend.view.CanvasUtils;

public class StepViewTable extends JTable {
	
	private static final long	serialVersionUID	= 1L;
	private IAssignment			_assignment;
	
	/**
	 * Just sets up the look
	 * 
	 * @param model the model to run this view
	 */
	public StepViewTable(final StepModel model) {
		super(model);
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
	 */
	public StepViewTable(final StepModel model, final IAssignment assignment) {
		this(model);
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
}
