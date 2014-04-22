package backend.database.tests;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.Template;
import data.TemplateStep;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import backend.database.Utilities;


public class TemplateStepStorageTest {
	
	Connection _con; 
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(false);
		try {
			_con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			Class.forName("org.h2.Driver");
		}
		catch (ClassNotFoundException e) {
			fail("StorageServiceTest: setUp: db drive class not found: " + e.getMessage()); 
		}
	}
	
	@After
	public void tearDown() throws Exception {
		_con.close(); 
	}
	
	/*
	 * Testing Template related functionality 
	 */
	
	@Test
	public void addAndGetTemplate() {
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
		
		String templateId = template.getID(); 
		Template afterTemplate = StorageService.getTemplate(templateId); 
		
		assertEquals(template.fullString(), afterTemplate.fullString()); 
		System.out.println("BEFORE Template: " + template.fullString());
		System.out.println("AFTER Template: " + afterTemplate.fullString());
	}

	@Test
	public void updateTemplate() {
		Template template = new Template("Template 1"); 		
		template.addStep(new TemplateStep("Step 1", 0.25));
		template.addStep(new TemplateStep("Step 2", 0.25));
		TemplateStep step = new TemplateStep("Step 3", 0.25); 
		template.addStep(step);
		template.addStep(new TemplateStep("Step 4", 0.25));
		try {
			StorageService.addTemplate(template);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		template.removeStep(step); 
		Template afterTemplate = (Template)StorageService.updateTemplate(template);
		
		System.out.println("BEFORE Template: " + template.fullString());
		System.out.println("AFTER Template: " + afterTemplate.fullString());
		
		//TODO: check to see if template steps are correctly reconstructed. 
	}
	
	@Test
	public void getAllTemplates() {
		
	}
	
}
