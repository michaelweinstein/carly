package frontend.view.assignments;

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import frontend.Utils;

public class StepViewTable extends JTable {
	
	private static final long	serialVersionUID	= 4252084989397140385L;
	private final StepModel		_model;
	
	public StepViewTable(final StepModel model) {
		super(model);
		_model = model;
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
	
	@Override
	public JTableHeader getTableHeader() {
		final JTableHeader ret = super.getTableHeader();
		Utils.themeComponent(ret);
		return ret;
	}
	
	/**
	 * Gets at special model
	 * 
	 * @return the model for this table
	 */
	@Override
	public StepModel getModel() {
		return _model;
	}
}
