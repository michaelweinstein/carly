package frontend.view.template_wizard;

import java.util.ArrayList;
import java.util.List;

import backend.StorageService;
import data.ITemplate;
import data.Template;

// TODO
public class TemplateList {
	
	private static List<ITemplate> _templates;
	
	public TemplateList() {
		_templates = StorageService.getAllTemplates();
///// Until StorageService method is written
		if (_templates == null) 
			_templates = new ArrayList<ITemplate>();
		
/////		
		System.out.println("_templates.size(): " + _templates.size());
		for (ITemplate t: _templates) {
			System.out.println("t: " + t);
		}
	}
	
	/**
	 * 
	 * @param t
	 */
	public static void addTemplate(Template t) {
//////
		System.out.println("template: " + t);
		
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
