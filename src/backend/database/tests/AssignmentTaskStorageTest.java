package backend.database.tests;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.Assignment;
import data.Task;
import data.Template;
import data.TemplateStep;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import backend.database.Utilities;


public class AssignmentTaskStorageTest {
	
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
	 * Testing Assignment related functionality 
	 */
	
	@Test
	public void storeAndRetrieveOneAssignment() {
		Date dueDate = new Date(); 
		Template template = new Template("Template 1"); 		
		template.addStep(new TemplateStep("Step 1", 1.0)); 
		Assignment asgn = new Assignment("Assignment 1", dueDate, template); 
		asgn.addTask(new Task("Task 1", 1, asgn.getID())); 
		
		String asgnId = asgn.getID(); 
		
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(asgn);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		Assignment afterAsgn = StorageService.getAssignmentById(asgnId); 
		
		System.out.println("\nTemplate Id: " + asgn.getTemplate().getID());
		System.out.println("Template BEFORE: " + template.fullString());
		System.out.println("Template AFTER: " + ((Template)afterAsgn.getTemplate()).fullString());
		
		assertEquals(template.fullString(), ((Template)afterAsgn.getTemplate()).fullString());
		assertTrue(template.equals(afterAsgn.getTemplate())); 
		System.out.println("\nBEFORE: " + asgn.fullString());
		System.out.println("AFTER: " + afterAsgn.fullString()); 
		assertEquals(asgn.fullString(), afterAsgn.fullString()); 
	}
	
	@Test
	public void removeAssignment() {
		//TODO: test for cascading deletes
	}
	
	@Test
	public void addMultipleAssignments() {
		
	}
	
	@Test
	public void updateAssignment() {
		
	}
	
	@Test
	public void getAssignmentsWithinRange() {
		
	}
	
	/*
	 * Testing Task related functionality
	 */
	
	@Test
	public void getTasksInDateRange() {
		
	}
}
