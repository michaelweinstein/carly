package frontend.view.template_wizard;

import java.util.ArrayList;
import java.util.List;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import data.ITemplate;
import data.Template;

// TODO
public class TemplateList {
	
	private static List<ITemplate> _templates = new ArrayList<>();
	
	public TemplateList() {
		_templates = StorageService.getAllTemplates();
				
//////////////	print liiiiines
/*		System.out.println("_templates.size(): " + _templates.size() + " (TemplateList())");
		for (ITemplate t: _templates) {
			System.out.println("Template t: " + t);
			for (ITemplateStep s: t.getAllSteps()) {
				System.out.println("-- Step s: " + s.getName() + ", " + s.getPercentOfTotal());
			}
		}
		System.out.println();*/
/////////^^^^^^^^^^^^^^
	}
	
	/**
	 * Add Template to local list and to the SQL
	 * database. Storing in a local list saves 
	 * having to query a second time to get all
	 * templates to display to user in TemplateWizardView.
	 * 
	 * @param t
	 */
	public static void addTemplate(Template t) {
		try {
			StorageService.addTemplate(t);
		} catch (StorageServiceException e) {
			// TODO Handle
			System.out.println("StorageServiceException for template " + t + " (TemplateList.addTemplate)");
			e.printStackTrace();
		}

		_templates.add(t);
	}
	
	public static void updateTemplate(Template t) {
		try {
			StorageService.updateTemplate(t);
		} catch (StorageServiceException e) {
			// TODO: Handle
			System.out.println("StorageServiceException for template " + t + " (TemplateList.updateTemplate)");
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns an array of ITemplate objects.
	 * Uses array, instead of List, because it is
	 * passed into constructor of JComboBox in TemplateWizard.
	 * @return
	 */
	public static ITemplate[] getAllTemplates() {
		ITemplate[] templates = new ITemplate[_templates.size()];
		templates = _templates.toArray(templates);
		return templates;
	}
	
}
