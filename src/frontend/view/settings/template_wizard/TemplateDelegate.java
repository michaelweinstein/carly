package frontend.view.settings.template_wizard;

import java.util.ArrayList;
import java.util.List;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import data.ITemplate;
import frontend.Utils;

/**
 * This is a simple wrapper class that delegates storing and accessing Templates from the StorageService. Since all
 * StorageService calls throw StorageServiceExceptions, these methods factor out the necessary try/catch blocks from the
 * callers, and do basic validity checks before making the calls to StorageService. All methods are static.
 * TemplateDelegate does not need to be instantiated
 * 
 * @author michaelweinstein
 */

public class TemplateDelegate {
	
	/**
	 * Add Template to the database via StorageService. In case of StorageServiceException, prints error message.
	 * 
	 * @param t, Template to add
	 */
	public static void addTemplate(final ITemplate t) {
		try {
			StorageService.addTemplate(t);
		} catch (final StorageServiceException e) {
			// TODO Handle exception?
			System.out.println("ERROR: StorageServiceException for template " + t + " (TemplateDelegate.addTemplate)");
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates a Template that already exists in the database. It is REQUIRED that updatedTemplate.id == toReplace.id.
	 * Updates Template in StorageService and also in local List. Upon StorageServiceException, prints method to user
	 * and cancels storage. Takes in (1) new Template and (2) old Template, with identical IDs. This method is Called
	 * when user edits and submits existing Template from TemplateWizardView.
	 * 
	 * @param updatedTemplate, updated Template (with same ID as toReplace)
	 * @param toReplace, old Template to overwrite in database
	 */
	public static void updateTemplate(final ITemplate updatedTemplate, final ITemplate toReplace) {
		try {
			// ID of updatedTemplate must match that of Template it wants to replace
			if (updatedTemplate.getID().equals(toReplace.getID())) {
				// Send new Template to StorageService to replace old Template toReplace
				StorageService.updateTemplate(updatedTemplate);
			}
			// If param UIDs don't match, do not send to database, print alert message
			else {
				Utils.printError("Template update failed! Cannot replace Template [toReplace] with "
					+ "another Template [updatedTemplate] unless their IDs match.  (TemplateDelegate.updateTemplate)");
			}
		} catch (final StorageServiceException e) {
			// TODO: Handle exception?
			Utils.printError("Template update failed! Make sure updatedTemplate, " + updatedTemplate.fullString()
				+ ", has same ID as Template to database!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns an array of ITemplate objects. Uses array, instead of List, because it is passed into constructor of
	 * JComboBox in TemplateWizard.
	 * 
	 * @return
	 */
	public static ITemplate[] getExistingTemplates() {
		final List<ITemplate> allTemps = StorageService.getAllTemplates();
		final List<ITemplate> filteredTemps = new ArrayList<>();
		for (final ITemplate t : allTemps) {
			if (!t.getName().equals("Custom")) {
				filteredTemps.add(t);
			}
		}
		ITemplate[] templates = new ITemplate[filteredTemps.size()];
		templates = filteredTemps.toArray(templates);
		
		return templates;
	}
	
}
