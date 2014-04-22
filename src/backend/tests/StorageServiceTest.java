package backend.tests;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.Assignment;
import data.ITemplate;
import data.Task;
import data.Template;
import data.TemplateStep;
import backend.StorageService;


public class StorageServiceTest {
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(true);
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void storeAndRetrieveOneAssignment() {
		Date dueDate = new Date(); 
		Template template = new Template("Template 1"); 		
		template.addStep(new TemplateStep("Step 1", 1.0)); 
		Assignment asgn = new Assignment("Assignment 1", dueDate, template); 
		asgn.addTask(new Task("Task 1", 1, asgn.getID())); 
		
		String asgnId = asgn.getID(); 
		
		StorageService.addTemplate(template); //TODO: handle case where you try to add asgn without template, need to catch the error that is thrown! 
		StorageService.addAssignment(asgn); 
		
		Assignment afterAsgn = StorageService.getAssignmentById(asgnId); 
		Date beforeDate = asgn.getDueDate();
		Date afterDate = afterAsgn.getDueDate(); 		
		
		System.out.println("\nTemplate Id: " + asgn.getTemplate().getID());
		System.out.println("Template BEFORE: " + template.fullString());
		System.out.println("Template AFTER: " + ((Template)afterAsgn.getTemplate()).fullString());
		
		assertEquals(template.fullString(), ((Template)afterAsgn.getTemplate()).fullString());
		assertTrue(template.equals(afterAsgn.getTemplate())); 
		System.out.println("\nBEFORE: " + asgn.fullString());
		System.out.println("AFTER: " + afterAsgn.fullString()); 
		
		//TODO: ask if storing time matters
//		assertEquals(asgn.fullString(), afterAsgn.fullString()); 
	}
	
	
}
