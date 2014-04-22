package backend.database.tests;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.Assignment;
import data.AssignmentBlock;
import data.DataUtil;
import data.ITask;
import data.ITemplate;
import data.ITemplateStep;
import data.Task;
import data.Template;
import data.TemplateStep;
import data.UnavailableBlock;

import backend.database.AssignmentTaskStorage;
import backend.database.StorageService;
import backend.database.StorageServiceException;
import backend.database.Utilities;


public class TimeBlockStorageTest {
	
	Connection _con; 
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(true);
		try {
			_con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			Class.forName("org.h2.Driver");
		}
		catch (ClassNotFoundException e) {
			fail("TimeBlockStorageTest: setUp: db drive class not found: " + e.getMessage()); 
		}
	}
	
	@After
	public void tearDown() throws Exception {
		_con.close(); 
	}
	
	/*
	 * Testing Time Block (generic block) related functionality 
	 */
	
//	@Test
//	public void addTimeBlock() {
//		
//	}
//	
//	@Test
//	public void addAllTimeBlocks() {
//		
//	}
	
	/*
	 * Testing Assignment Block retrieval 
	 */
	
	@Test
	public void getAllAssignmentBlocksWithinRange() {
		//Create needed objects
		ArrayList<ITemplateStep> templateSteps = new ArrayList<>(); 
		templateSteps.add(new TemplateStep("Step", 1.0, 1)); 
		Template template = new Template("Template", templateSteps); 
		Assignment assignment = new Assignment("Assignment", new Date(), template); 
		Task task = new Task("Task", 1, assignment.getID()); 
		assignment.addTask(task); 
		
		AssignmentBlock block1 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				task, true); 
		AssignmentBlock block2 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task, true); 
		AssignmentBlock block3 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task, true); 
		AssignmentBlock block4 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 10), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), 
				task, true); 
		
		//Add objects in correct order to the db
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(assignment); 
			StorageService.addTimeBlock(block1);
			StorageService.addTimeBlock(block2);
			StorageService.addTimeBlock(block3); 
			StorageService.addTimeBlock(block4);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertTrue(StorageService.getAllAssignmentBlocksWithinRange(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 1), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 5)).size() == 3);
	}
	
	/*
	 * Testing Unavailable Block retrieval 
	 */
	
	@Test
	public void getAllUnavailableBlocksWithinRange() {
		//Create needed objects
		ArrayList<ITemplateStep> templateSteps = new ArrayList<>(); 
		templateSteps.add(new TemplateStep("Step", 1.0, 1)); 
		Template template = new Template("Template", templateSteps); 
		Assignment assignment = new Assignment("Assignment", new Date(), template); 
		Task task = new Task("Task", 1, assignment.getID()); 
		assignment.addTask(task); 
		
		AssignmentBlock block1 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				task, true); 
		AssignmentBlock block2 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task, true); 
		AssignmentBlock block3 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task, true); 
		AssignmentBlock block4 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 10), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), 
				task, true); 
		
		UnavailableBlock unavailable1 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				null, false); 
		UnavailableBlock unavailable2 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				null, false); 
		UnavailableBlock unavailable3 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 11), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 13), 
				null, false); 
		
		
		//Add objects in correct order to the db
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(assignment); 
			StorageService.addTimeBlock(block1);
			StorageService.addTimeBlock(block2);
			StorageService.addTimeBlock(block3); 
			StorageService.addTimeBlock(block4);
			StorageService.addTimeBlock(unavailable1);
			StorageService.addTimeBlock(unavailable2);
			StorageService.addTimeBlock(unavailable3);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assert(StorageService.getAllUnavailableBlocksWithinRange(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 1), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 5)).size() == 2);
	}
	
}
