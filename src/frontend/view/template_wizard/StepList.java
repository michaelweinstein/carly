package frontend.view.template_wizard;

import java.util.List;

import data.TemplateStep;

// TODO
public class StepList {
	
	private static List<TemplateStep> _steps;
	
	public StepList() {
		
	}
	
	public static int size() {
		return _steps.size();
	}
	
}
