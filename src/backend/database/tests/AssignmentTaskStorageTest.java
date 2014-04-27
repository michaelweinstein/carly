package backend.database.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import data.Assignment;
import data.ITask;
import data.ITemplateStep;
import data.Task;
import data.Template;
import data.TemplateStep;
import backend.database.StorageService;
import backend.database.StorageServiceException;

public class AssignmentTaskStorageTest {
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(true);
	}

	/*
	 * Testing Assignment related functionality 
	 */
	
	@Test
	public void addAssignment() {
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
		
		Assignment afterAsgn = StorageService.getAssignment(asgnId); 		
		assertEquals(asgn.fullString(), afterAsgn.fullString()); 
	}
	
	@Test
	public void addAssignments() {
		ArrayList<ITemplateStep> templateSteps = new ArrayList<>(); 
		templateSteps.add(new TemplateStep("Step", 1.0, 1)); 
		Template template = new Template("Template", templateSteps); 
		
		Assignment assignment1 = new Assignment("Assignment 1", new Date(), template); 
		Task task11 = new Task("Task1 in " + assignment1.getName(), 0.5, assignment1.getID()); 
		Task task12 = new Task("Task2 in " + assignment1.getName(), 0.25, assignment1.getID());
		Task task13 = new Task("Task3 in " + assignment1.getName(), 0.25, assignment1.getID());
		assignment1.addTask(task11);
		assignment1.addTask(task12);
		assignment1.addTask(task13);
		
		Assignment assignment2 = new Assignment("Assignment 2", new Date(), template); 
		Task task21 = new Task("Task1 in " + assignment2.getName(), 0.5, assignment2.getID()); 
		Task task22 = new Task("Task2 in " + assignment2.getName(), 0.5, assignment2.getID());
		assignment2.addTask(task21);
		assignment2.addTask(task22);
		
		Assignment assignment3 = new Assignment("Assignment 3", new Date(), template); 
		Task task31 = new Task("Task1 in " + assignment3.getName(), 0.25, assignment3.getID()); 
		Task task32 = new Task("Task2 in " + assignment3.getName(), 0.25, assignment3.getID());
		Task task33 = new Task("Task3 in " + assignment3.getName(), 0.25, assignment3.getID());
		Task task34 = new Task("Task3 in " + assignment3.getName(), 0.25, assignment3.getID());
		assignment3.addTask(task31);
		assignment3.addTask(task32);
		assignment3.addTask(task33);
		assignment3.addTask(task34);
		
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(assignment1);
			StorageService.addAssignment(assignment2);
			StorageService.addAssignment(assignment3);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		Assignment assignment1After = StorageService.getAssignment(assignment1.getID());
		Assignment assignment2After = StorageService.getAssignment(assignment2.getID());
		Assignment assignment3After = StorageService.getAssignment(assignment3.getID());
		
		assertEquals(assignment1.fullString(), assignment1After.fullString());
		assertEquals(assignment2.fullString(), assignment2After.fullString());
		assertEquals(assignment3.fullString(), assignment3After.fullString());
	}
	
	@Test
	public void updateAssignments() {
		ArrayList<ITemplateStep> templateSteps1 = new ArrayList<>(); 
		templateSteps1.add(new TemplateStep("Step 1", 1.0, 1)); 
		Template template1 = new Template("Template 1", templateSteps1); 
		
		ArrayList<ITemplateStep> templateSteps2 = new ArrayList<>(); 
		templateSteps2.add(new TemplateStep("Step 1", 0.5, 1));
		templateSteps2.add(new TemplateStep("Step 2", 0.5, 2));
		Template template2 = new Template("Template 2", templateSteps2); 
		
		Assignment assignment1 = new Assignment("Assignment 1", new Date(), template1); 
		Task task11 = new Task("Task1 in " + assignment1.getName(), 0.5, assignment1.getID()); 
		Task task12 = new Task("Task2 in " + assignment1.getName(), 0.25, assignment1.getID());
		Task task13 = new Task("Task3 in " + assignment1.getName(), 0.25, assignment1.getID());
		assignment1.addTask(task11);
		assignment1.addTask(task12);
		assignment1.addTask(task13);
		
		Assignment assignment2 = new Assignment("Assignment 2", new Date(), template1); 
		Task task21 = new Task("Task1 in " + assignment2.getName(), 0.5, assignment2.getID()); 
		Task task22v1 = new Task("Task2v1 in " + assignment2.getName(), 0.25, assignment2.getID());
		Task task22v2 = new Task("Task2v2 in " + assignment2.getName(), 0.25, assignment2.getID());
		Task task23 = new Task("Task3 in " + assignment2.getName(), 0.25, assignment2.getID());
		assignment2.addTask(task21);
		assignment2.addTask(task22v1);
		
		Assignment assignment3 = new Assignment("Assignment 3", new Date(), template1); 
		Task task31 = new Task("Task1 in " + assignment3.getName(), 0.25, assignment3.getID()); 
		Task task32 = new Task("Task2 in " + assignment3.getName(), 0.25, assignment3.getID());
		Task task33 = new Task("Task3 in " + assignment3.getName(), 0.25, assignment3.getID());
		Task task34 = new Task("Task3 in " + assignment3.getName(), 0.25, assignment3.getID());
		assignment3.addTask(task31);
		assignment3.addTask(task32);
		assignment3.addTask(task33);
		assignment3.addTask(task34);
		
		try {
			StorageService.addTemplate(template1);
			StorageService.addTemplate(template2); 
			StorageService.addAssignment(assignment1);
			StorageService.addAssignment(assignment2);
			StorageService.addAssignment(assignment3);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		assignment2.setTemplate(template2);
		assignment2.removeTask(task22v1); 
		assignment2.addTask(task22v2);
		assignment2.addTask(task23);
		assignment3.setExpectedHours(100);
		
		try {
			StorageService.updateAssignment(assignment2);
			StorageService.updateAssignment(assignment3);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		}
		
		Assignment assignment1After = StorageService.getAssignment(assignment1.getID());
		Assignment assignment2After = StorageService.getAssignment(assignment2.getID());
		Assignment assignment3After = StorageService.getAssignment(assignment3.getID());
		
		assertEquals(assignment1.fullString(), assignment1After.fullString());
		assertEquals(assignment2.fullString(), assignment2After.fullString());
		assertEquals(assignment3.fullString(), assignment3After.fullString());
	}
	
	@Test
	public void removeAssignment() {
		ArrayList<ITemplateStep> templateSteps1 = new ArrayList<>(); 
		templateSteps1.add(new TemplateStep("Step 1", 1.0, 1)); 
		Template template1 = new Template("Template 1", templateSteps1); 
		
		ArrayList<ITemplateStep> templateSteps2 = new ArrayList<>(); 
		templateSteps2.add(new TemplateStep("Step 1", 0.5, 1));
		templateSteps2.add(new TemplateStep("Step 2", 0.5, 2));
		Template template2 = new Template("Template 2", templateSteps2); 
		
		Assignment assignment1 = new Assignment("Assignment 1", new Date(), template1); 
		Task task11 = new Task("Task1 in " + assignment1.getName(), 0.5, assignment1.getID()); 
		Task task12 = new Task("Task2 in " + assignment1.getName(), 0.25, assignment1.getID());
		Task task13 = new Task("Task3 in " + assignment1.getName(), 0.25, assignment1.getID());
		assignment1.addTask(task11);
		assignment1.addTask(task12);
		assignment1.addTask(task13);
		
		try {
			StorageService.addTemplate(template1);
			StorageService.addTemplate(template2); 
			StorageService.addAssignment(assignment1);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		//Check to see it was added properly 
		Assignment assignment1After = StorageService.getAssignment(assignment1.getID());
		assertEquals(assignment1.fullString(), assignment1After.fullString());
		
		//Check to see that the assignment is removed
		StorageService.removeAssignment(assignment1);
		assignment1After = StorageService.getAssignment(assignment1.getID());
		assertTrue(assignment1After == null); 
		
		//Read the assignment after making some changes
		assignment1.setTemplate(template2);
		try {
			StorageService.addAssignment(assignment1);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		//Check to see it was added properly 
		assignment1After = StorageService.getAssignment(assignment1.getID());
		assertEquals(assignment1.fullString(), assignment1After.fullString());
		
		
		//Check to see that the assignment is removed
		StorageService.removeAssignment(assignment1);
		assignment1After = StorageService.getAssignment(assignment1.getID());
		assertTrue(assignment1After == null);
	}
	
	@Test
	public void getAssignmentsWithinRange() {
		ArrayList<ITemplateStep> templateSteps = new ArrayList<>(); 
		templateSteps.add(new TemplateStep("Step", 1.0, 1)); 
		Template template = new Template("Template", templateSteps); 
		
		Assignment assignment1 = new Assignment("Assignment 1", 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), template); 
		Task task11 = new Task("Task1 in " + assignment1.getName(), 0.5, assignment1.getID()); 
		Task task12 = new Task("Task2 in " + assignment1.getName(), 0.25, assignment1.getID());
		Task task13 = new Task("Task3 in " + assignment1.getName(), 0.25, assignment1.getID());
		assignment1.addTask(task11);
		assignment1.addTask(task12);
		assignment1.addTask(task13);
		
		Assignment assignment2 = new Assignment("Assignment 2", 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 5), template); 
		Task task21 = new Task("Task1 in " + assignment2.getName(), 0.5, assignment2.getID()); 
		Task task22 = new Task("Task2 in " + assignment2.getName(), 0.5, assignment2.getID());
		assignment2.addTask(task21);
		assignment2.addTask(task22);
		
		Assignment assignment3 = new Assignment("Assignment 3", 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), template); 
		Task task31 = new Task("Task1 in " + assignment3.getName(), 0.25, assignment3.getID()); 
		Task task32 = new Task("Task2 in " + assignment3.getName(), 0.25, assignment3.getID());
		Task task33 = new Task("Task3 in " + assignment3.getName(), 0.25, assignment3.getID());
		Task task34 = new Task("Task3 in " + assignment3.getName(), 0.25, assignment3.getID());
		assignment3.addTask(task31);
		assignment3.addTask(task32);
		assignment3.addTask(task33);
		assignment3.addTask(task34);
		
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(assignment1);
			StorageService.addAssignment(assignment2);
			StorageService.addAssignment(assignment3);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		List<Assignment> asgnList = StorageService.getAllAssignmentsWithinRange(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 1), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 8)); 
		
		int numMatch = 0; 
		for (Assignment asgn : asgnList) {
			if (asgn.fullString().equals(assignment1.fullString())) {
				numMatch++;
			}
			else if (asgn.fullString().equals(assignment2.fullString())) {
				numMatch++; 
			} 
		}
		
		assertTrue(numMatch == 2); 
	}
	
	/*
	 * Testing Task related functionality
	 */
	
	@Test
	public void getTasksWithinRange() {
		ArrayList<ITemplateStep> templateSteps = new ArrayList<>(); 
		templateSteps.add(new TemplateStep("Step", 1.0, 1)); 
		Template template = new Template("Template", templateSteps); 
		
		Assignment assignment1 = new Assignment("Assignment 1", 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), template); 
		Task task11 = new Task("Task1 in " + assignment1.getName(), 0.5, assignment1.getID()); 
		Task task12 = new Task("Task2 in " + assignment1.getName(), 0.25, assignment1.getID());
		Task task13 = new Task("Task3 in " + assignment1.getName(), 0.25, assignment1.getID());
		assignment1.addTask(task11);
		assignment1.addTask(task12);
		assignment1.addTask(task13);
		
		Assignment assignment2 = new Assignment("Assignment 2", 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 5), template); 
		Task task21 = new Task("Task1 in " + assignment2.getName(), 0.5, assignment2.getID()); 
		Task task22 = new Task("Task2 in " + assignment2.getName(), 0.5, assignment2.getID());
		assignment2.addTask(task21);
		assignment2.addTask(task22);
		
		Assignment assignment3 = new Assignment("Assignment 3", 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), template); 
		Task task31 = new Task("Task1 in " + assignment3.getName(), 0.25, assignment3.getID()); 
		Task task32 = new Task("Task2 in " + assignment3.getName(), 0.25, assignment3.getID());
		Task task33 = new Task("Task3 in " + assignment3.getName(), 0.25, assignment3.getID());
		Task task34 = new Task("Task3 in " + assignment3.getName(), 0.25, assignment3.getID());
		assignment3.addTask(task31);
		assignment3.addTask(task32);
		assignment3.addTask(task33);
		assignment3.addTask(task34);
		
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(assignment1);
			StorageService.addAssignment(assignment2);
			StorageService.addAssignment(assignment3);
		} catch (StorageServiceException e) {
			fail(e.getMessage()); 
		} 
		
		List<ITask> taskList = StorageService.getAllTasksWithinRange(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 1), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 8)); 		
		int numLeft = taskList.size(); 
		for (ITask task : taskList) {
			if (assignment1.fullString().contains(task.fullString())) {
				numLeft--; 
			}
			else if (assignment2.fullString().contains(task.fullString())) {
				numLeft--;  
			} 
		}
		
		assertTrue(numLeft == 0);
	}
}
