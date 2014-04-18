package frontend.view.assignments;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import data.ITemplateStep;

/**
 * An Editable Table Model for use in Steps
 * 
 * @author dgattey
 */
public class StepModel extends AbstractTableModel {
	
	private static final long		serialVersionUID	= -3489264246911414195L;
	public static final int			TITLE_INDEX			= 0;
	public static final int			PERCENT_INDEX		= 1;
	private final String[]			columnNames;
	private final List<Object[]>	rowData;
	private final List<Object[]>	deletes;
	private boolean					editable;
	
	/**
	 * Creates a new table given data
	 * 
	 * @param rowData the row data
	 * @param columnNames names for each column
	 */
	public StepModel(final Object[][] rowData, final String[] columnNames) {
		this.columnNames = columnNames;
		this.rowData = new ArrayList<>();
		for (final Object[] list : rowData) {
			this.rowData.add(list);
		}
		deletes = new ArrayList<>();
		editable = true;
	}
	
	@Override
	public String getColumnName(final int col) {
		return columnNames[col];
	}
	
	@Override
	public int getRowCount() {
		return rowData.size();
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	@Override
	public Object getValueAt(final int row, final int col) {
		return getRowAt(row)[col];
	}
	
	/**
	 * Gets a given row
	 * 
	 * @param row the row itself
	 * @return the contents of the row
	 */
	public Object[] getRowAt(final int row) {
		return rowData.get(row);
	}
	
	@Override
	public boolean isCellEditable(final int row, final int col) {
		return editable;
	}
	
	@Override
	public void setValueAt(final Object value, final int row, final int col) {
		
		// Make sure the percentage is a valid number 1-100 (0 doesn't make sense)
		if (col == PERCENT_INDEX) {
			final String strRep = value.toString();
			double perc = 0;
			
			// If it's empty, just set and get out
			if (strRep.isEmpty()) {
				rowData.get(row)[col] = strRep;
				fireTableCellUpdated(row, col);
				return;
			}
			
			// Otherwise, try parsing it into something!
			try {
				perc = Double.parseDouble(strRep);
				if (perc > 0 && perc <= 100) {
					rowData.get(row)[col] = perc;
					fireTableCellUpdated(row, col);
				}
				return;
			} catch (final NumberFormatException nfe) {
				return; // Oops! NaN value is bad, do nothing
			}
		} else if (col == TITLE_INDEX) {
			rowData.get(row)[col] = value;
			fireTableCellUpdated(row, col);
			return;
		}
		
	}
	
	/**
	 * Adds a new blank item to the end of the list if the last thing isn't empty
	 */
	public void addBlankItem() {
		if (getRowCount() == 0 || !rowData.get(getRowCount() - 1).toString().isEmpty()) {
			rowData.add(new Object[] { "", "" });
		}
	}
	
	/**
	 * Deletes rows if they are empty
	 * 
	 * @param firstRow the first possible row
	 * @param lastRow the last possible row
	 */
	public void deleteRowsIfEmpty(final int firstRow, final int lastRow) {
		if (getRowCount() == 1 || lastRow == getRowCount() - 1) {
			return;
		}
		
		for (int i = firstRow; i <= lastRow; i++) {
			final Object title = getValueAt(i, TITLE_INDEX);
			final Object val = getValueAt(i, PERCENT_INDEX);
			if (title.toString().isEmpty() && val.toString().isEmpty()) {
				deletes.add(getRowAt(i));
			}
		}
		for (final Object o : deletes) {
			rowData.remove(o);
		}
		deletes.clear();
	}
	
	/**
	 * Clears all rows
	 */
	public void clear() {
		rowData.clear();
	}
	
	/**
	 * Adds a given template step as option
	 * 
	 * @param step a step to include
	 */
	public void addItem(final ITemplateStep step) {
		rowData.add(new Object[] { step.getName(), step.getPercentOfTotal() * 100.0 });
	}
	
	/**
	 * Sets the editable parameter of the model
	 * 
	 * @param b the boolean representing editable
	 */
	public void setEditable(final boolean b) {
		editable = b;
	}
}