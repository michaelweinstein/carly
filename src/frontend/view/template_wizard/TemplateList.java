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
	}
	
	/**
	 * 
	 * @param t
	 */
	public static void addTemplate(Template t) {
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
