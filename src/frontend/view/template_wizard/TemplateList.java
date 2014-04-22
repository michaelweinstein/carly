package frontend.view.template_wizard;

import java.util.ArrayList;
import java.util.List;

import backend.StorageService;
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
		StorageService.addTemplate(t);
		_templates.add(t);
	}
	
	/**
	 * 
	 * @return
	 */
	public static List<ITemplate> getAllTemplates() {
		return _templates;
	}
	
}
