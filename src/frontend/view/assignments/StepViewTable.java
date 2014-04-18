package frontend.view.assignments;

import java.awt.Color;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import frontend.Utils;

public class StepViewTable extends JTable {
	
	private static final long	serialVersionUID	= 4252084989397140385L;
	
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
	
	@Override
	public JTableHeader getTableHeader() {
		final JTableHeader ret = super.getTableHeader();
		Utils.themeComponent(ret);
		return ret;
	}
}
