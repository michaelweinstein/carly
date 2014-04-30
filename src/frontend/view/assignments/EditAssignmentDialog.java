package frontend.view.assignments;

import hub.HubController;
import backend.database.StorageService;
import data.Assignment;
import data.IAssignment;
import data.ITask;
import data.ITemplateStep;
import data.TemplateStep;
import frontend.app.GUIApp;

/**
 * Just like the AddAssignmentDialog but has editing capabilities
 * 
 * @author dgattey
 */
public class EditAssignmentDialog extends AddAssignmentDialog {
	
	private static final long	serialVersionUID	= 1L;
	private IAssignment			_assignment;
	
	/**
	 * Public constructor
	 * 
	 * @param app the app that runs it
	 */
	public EditAssignmentDialog(final GUIApp app) {
		super(app);
	}
	
	/**
	 * Adds only after removing old one
	 */
	@Override
	protected void addToDatabase() {
		try {
			final Assignment a = parseFields();
			StorageService.removeAssignment(_assignment);
			HubController.addAssignmentToCalendar(a);
			_app.reload();
			clearContents();
			dispose();
			_assignment = a;
		} catch (final IllegalArgumentException e1) {
			_statusLabel.setText("Oops! " + e1.getMessage());
		}
	}
	
	/**
	 * Sets the assignment and recreates info
	 * 
	 * @param a the assignmnet related
	 */
	public void setAssignment(final IAssignment a) {
		_assignment = a;
		_dialogTitle.setText("Edit Assignment");
		_dateTimeField.setValue(a.getDueDate());
		_numHours.setText(String.valueOf(a.getExpectedHours()));
		_titleField.setText(a.getName());
		_addButton.setText("Edit");
	}
	
	@Override
	public void setVisible(final boolean b) {
		pack();
		super.setVisible(b);
		
		// Adds the assignment's items as the current steps just in case it wasn't a template
		if (_assignment != null) {
			_templatePicker.setSelectedItem(_assignment.getTemplate());
			_stepModel.clear();
			for (int i = 0; i < _assignment.getTasks().size(); i++) {
				final ITask s = _assignment.getTasks().get(i);
				final String name = s.getName();
				final ITemplateStep st = new TemplateStep(name, s.getPercentOfTotal(), i, s.getPreferredTimeOfDay());
				_stepModel.addItem(st);
			}
			_stepModel.addBlankItem();
			_stepList.revalidate();
			_stepList.repaint();
			revalidate();
			repaint();
			requestFocusInWindow();
		}
	}
}
