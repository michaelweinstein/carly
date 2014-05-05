package backend.database.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.Assignment;
import data.ITemplate;
import data.Task;
import data.Template;
import data.TemplateStep;
import data.TimeOfDay;
import backend.database.StorageService;
import backend.database.StorageServiceException;

public class TemplateStepStorageTest {
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(true);
	}
	
	@After
	public void cleanUp() {
		StorageService.cleanup();
	}
	
	/*
	 * Testing Template related functionality 
	 */
	
	@Test
	public void addTemplate() {
		Template template = new Template("Template 1"); 		
		template.addStep(new TemplateStep("Step 1", 0.25));
		template.addStep(new TemplateStep("Step 2", 0.25)); 
		template.addStep(new TemplateStep("Step 3", 0.25));
		template.addStep(new TemplateStep("Step 4", 0.25));
		try {
			StorageService.addTemplate(template);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		 
		assertEquals(template.fullString(), StorageService.getTemplate(template.getID()).fullString()); 
	}
	
	@Test
	public void getTemplateByName() {
		Template template = new Template("Template 1"); 		
		template.addStep(new TemplateStep("Step 1", 0.25));
		template.addStep(new TemplateStep("Step 2", 0.25)); 
		template.addStep(new TemplateStep("Step 3", 0.25));
		template.addStep(new TemplateStep("Step 4", 0.25));
		try {
			StorageService.addTemplate(template);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		 
		assertEquals(template.fullString(), StorageService.getTemplateByName(template.getName()).fullString()); 
	}

	@Test
	public void updateTemplate() {
		Template template = new Template("Template 1"); 		
		template.addStep(new TemplateStep("Step 1", 0.25, 1));
		template.addStep(new TemplateStep("Step 2", 0.25, 2));
		TemplateStep step = new TemplateStep("Step 3", 0.25, 3); 
		template.addStep(step);
		template.addStep(new TemplateStep("Step 4", 0.25, 4));
		try {
			StorageService.addTemplate(template);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		assertEquals(template.fullString(), StorageService.getTemplate(template.getID()).fullString());
	
		template.removeStep(step);
		template.getStepByName("Step 4").setStepNumber(3);
		try {
			StorageService.updateTemplate(template);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		assertEquals(template.fullString(), StorageService.getTemplate(template.getID()).fullString());
	}
	
	@Test
	public void removeTemplate() {
		Template template = new Template("Template 1"); 		
		template.addStep(new TemplateStep("Step 1", 0.25));
		template.addStep(new TemplateStep("Step 2", 0.25)); 
		template.addStep(new TemplateStep("Step 3", 0.25));
		template.addStep(new TemplateStep("Step 4", 0.25));
		try {
			StorageService.addTemplate(template);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		 
		StorageService.removeTemplate(template);
		assertTrue(StorageService.getTemplate(template.getID()) == null);
	}
	
	@Test
	public void getAllTemplates() {
		ArrayList<Template> templates = new ArrayList<>(); 
		
		Template template1 = new Template("Template 1"); 		
		template1.addStep(new TemplateStep("Step 1", 0.25));
		template1.addStep(new TemplateStep("Step 2", 0.25)); 
		template1.addStep(new TemplateStep("Step 3", 0.25));
		template1.addStep(new TemplateStep("Step 4", 0.25));
		
		Template template2 = new Template("Template 2"); 		
		template2.addStep(new TemplateStep("Step 1", 0.5));
		template2.addStep(new TemplateStep("Step 2", 0.5)); 
		
		Template template3 = new Template("Template 3"); 		
		template3.addStep(new TemplateStep("Step 1", 1));
		
		Template template4 = new Template("Template 4"); 		
		template4.addStep(new TemplateStep("Step 1", 0.2));
		template4.addStep(new TemplateStep("Step 2", 0.2)); 
		template4.addStep(new TemplateStep("Step 3", 0.2));
		template4.addStep(new TemplateStep("Step 4", 0.2));
		template4.addStep(new TemplateStep("Step 5", 0.2));
		
		templates.add(template1);
		templates.add(template2); 
		templates.add(template3); 
		templates.add(template4); 
		
		try {
			StorageService.addTemplate(template1);
			StorageService.addTemplate(template2);
			StorageService.addTemplate(template3);
			StorageService.addTemplate(template4);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		List<ITemplate> templateList = StorageService.getAllTemplates(); 
		assertTrue(templateList.size() == 4); 
		for (ITemplate template : templateList) {
			assertTrue(templates.contains(template));
			templates.remove(template); 
		}
	}
	
	@Test
	public void learnTemplateStepTimeOfDay() {
		Template template1 = new Template("Template 1"); 		
		template1.addStep(new TemplateStep("Step 1", 0.25, 1));
		template1.addStep(new TemplateStep("Step 2", 0.25, 2)); 
		template1.addStep(new TemplateStep("Step 3", 0.25, 3));
		template1.addStep(new TemplateStep("Step 4", 0.25, 4));
		
		final Assignment assignment1 = new Assignment("Assignment 1", new Date(), template1);
		
		try {
			StorageService.addTemplate(template1);
			StorageService.addAssignment(assignment1);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		}
		
		StorageService.learnTemplateStepTimeOfDay(assignment1.getTasks().get(0), TimeOfDay.MORNING.name(), 20.0);		
		assertEquals(StorageService.getAssignment(assignment1.getID()).getTemplate().getStepByName("Step 1")
				.getBestTimeToWork().name(), TimeOfDay.MORNING.name());
		
		StorageService.learnTemplateStepTimeOfDay(assignment1.getTasks().get(0), TimeOfDay.AFTERNOON.name(), 25.0);		
		assertEquals(StorageService.getAssignment(assignment1.getID()).getTemplate().getStepByName("Step 1")
				.getBestTimeToWork().name(), TimeOfDay.AFTERNOON.name());
		
		StorageService.learnTemplateStepTimeOfDay(assignment1.getTasks().get(0), TimeOfDay.AFTERNOON.name(), -10.0);		
		assertEquals(StorageService.getAssignment(assignment1.getID()).getTemplate().getStepByName("Step 1")
				.getBestTimeToWork().name(), TimeOfDay.MORNING.name());
	}
	
	@Test
	public void learnTemplateConsecutiveHours() {
		Template template1 = new Template("Template 1", 5); 	
		template1.addStep(new TemplateStep("Step 1", 0.25, 1));
		template1.addStep(new TemplateStep("Step 2", 0.25, 2)); 
		template1.addStep(new TemplateStep("Step 3", 0.25, 3));
		template1.addStep(new TemplateStep("Step 4", 0.25, 4));
		
		final Assignment assignment1 = new Assignment("Assignment 1", new Date(), template1);
		
		try {
			StorageService.addTemplate(template1);
			StorageService.addAssignment(assignment1);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		}
		
		StorageService.learnTemplateConsecutiveHours(assignment1.getTasks().get(0), 10);
		assertTrue(StorageService.getTemplate(template1.getID()).getPreferredConsecutiveHours() == 
				((5.0 + 10.0) / 2));
		
		StorageService.learnTemplateConsecutiveHours(assignment1.getTasks().get(0), 2);
		assertTrue(StorageService.getTemplate(template1.getID()).getPreferredConsecutiveHours() == 
				((5.0 + 10.0 + 2.0) / 3));
		
		StorageService.learnTemplateConsecutiveHours(assignment1.getTasks().get(0), 15);
		assertTrue(StorageService.getTemplate(template1.getID()).getPreferredConsecutiveHours() == 
				((5.0 + 10.0 + 2.0 + 15.0) / 4));
	}
}
