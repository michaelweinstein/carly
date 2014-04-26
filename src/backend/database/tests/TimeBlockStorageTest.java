package backend.database.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
import data.ITimeBlockable;
import data.Task;
import data.Template;
import data.TemplateStep;
import data.UnavailableBlock;
import backend.database.AssignmentTaskStorage;
import backend.database.StorageService;
import backend.database.StorageServiceException;
import backend.database.Utilities;


public class TimeBlockStorageTest {
	
	private Connection _con; 
	private final ByteArrayOutputStream errorFd = new ByteArrayOutputStream();
	private PrintStream _oldStdErr; 
	
	@Before
	public void setUp() throws Exception {
		_oldStdErr = System.err; 
		System.setErr(new PrintStream(errorFd));
		
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
		System.setErr(_oldStdErr);
		_con.close(); 
	}
	
	/*
	 * Testing Time Block (generic block) related functionality 
	 */
	
	@Test
	public void addAssignmentBlock() {
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
				task); 
		
		//Add objects in correct order to the db
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(assignment); 
			StorageService.addTimeBlock(block1);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertEquals(StorageService.getAssignmentBlock(block1.getId()).fullString(), block1.fullString());
	}
	
	@Test
	public void addUnavailableBlock() {
		//Create needed objects
		UnavailableBlock unavailable1 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				null, false); 
		
		//Add objects in correct order to the db
		try {
			StorageService.addTimeBlock(unavailable1);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertEquals(StorageService.getUnavailableBlock(unavailable1.getId()).fullString(), unavailable1.fullString());
	}

	@Test
	public void addTwoUnavailableBlocks() {
		//Create needed objects
		//Add some unavailable blocks to the db
		Date start = new Date();
		UnavailableBlock unavailable1 = new UnavailableBlock((Date) start.clone(), 
				new Date(start.getTime() + 14400000), null, false);
		UnavailableBlock unavailable2 = new UnavailableBlock(new Date(start.getTime() + 86400000),
				new Date(start.getTime() + 104400000), null, false);
		
		//Add objects in correct order to the db
		try {
			StorageService.addTimeBlock(unavailable1);
			StorageService.addTimeBlock(unavailable2);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertEquals(StorageService.getUnavailableBlock(unavailable1.getId()).fullString(), unavailable1.fullString());
		assertEquals(StorageService.getUnavailableBlock(unavailable2.getId()).fullString(), unavailable2.fullString());
	}
	
	@Test
	public void updateAssignmentBlock() {
		//Create needed objects
		ArrayList<ITemplateStep> templateSteps = new ArrayList<>(); 
		templateSteps.add(new TemplateStep("Step", 1.0, 1)); 
		Template template = new Template("Template", templateSteps); 
		Assignment assignment = new Assignment("Assignment", new Date(), template); 
		Task task1 = new Task("Task1", 0.5, assignment.getID()); 
		assignment.addTask(task1);
		Task task2 = new Task("Task2", 0.5, assignment.getID()); 
		assignment.addTask(task2);
		
		AssignmentBlock block1 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				task1); 
		
		//Add objects in correct order to the db
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(assignment); 
			StorageService.addTimeBlock(block1);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		block1.setEnd(new Date(System.currentTimeMillis() + (86400 * 1000) * 10));
		block1.setTask(task2);
		
		try {
			StorageService.updateTimeBlock(block1);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertEquals(StorageService.getAssignmentBlock(block1.getId()).fullString(), block1.fullString());
	}
	
	@Test
	public void updateUnavailableBlock() {
		//Create needed objects
		UnavailableBlock unavailable1 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				null, false); 
		
		//Add objects in correct order to the db
		try {
			StorageService.addTimeBlock(unavailable1);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		unavailable1.setEnd(new Date(System.currentTimeMillis() + (86400 * 1000) * 10));
		
		try {
			StorageService.updateTimeBlock(unavailable1);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertEquals(StorageService.getUnavailableBlock(unavailable1.getId()).fullString(), unavailable1.fullString());	
	}
	
	@Test
	public void removeAssignmentBlock() {
		//Create needed objects
		ArrayList<ITemplateStep> templateSteps = new ArrayList<>(); 
		templateSteps.add(new TemplateStep("Step", 1.0, 1)); 
		Template template = new Template("Template", templateSteps); 
		Assignment assignment = new Assignment("Assignment", new Date(), template); 
		Task task1 = new Task("Task1", 0.5, assignment.getID()); 
		assignment.addTask(task1);
		Task task2 = new Task("Task2", 0.5, assignment.getID()); 
		assignment.addTask(task2);
		
		AssignmentBlock block1 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				task1); 
		AssignmentBlock block2 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task1); 
		
		//Add objects in correct order to the db
		try {
			StorageService.addTemplate(template); 
			StorageService.addAssignment(assignment); 
			StorageService.addTimeBlock(block1);
			StorageService.addTimeBlock(block2);
			
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertEquals(StorageService.getAssignmentBlock(block1.getId()).fullString(), block1.fullString());
		
		StorageService.removeTimeBlock(block1); 
		
		assertTrue(StorageService.getAssignmentBlock(block1.getId()) == null);
		assertTrue(errorFd.toString().contains("ERROR:")); 
	}
	
	@Test
	public void removeUnavailableBlock() {
		//Create needed objects
		UnavailableBlock unavailable1 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				null, false); 
		
		//Add objects in correct order to the db
		try {
			StorageService.addTimeBlock(unavailable1);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertEquals(StorageService.getUnavailableBlock(unavailable1.getId()).fullString(), unavailable1.fullString());
		
		StorageService.removeTimeBlock(unavailable1); 
		
		assertTrue(StorageService.getAssignmentBlock(unavailable1.getId()) == null);
		assertTrue(errorFd.toString().contains("ERROR:")); 
	}
	
	@Test
	public void mergeAllTimeBlocks() {
		List<ITimeBlockable> blockList = new ArrayList<>(); 
		
		//Create needed objects
		ArrayList<ITemplateStep> templateSteps = new ArrayList<>(); 
		templateSteps.add(new TemplateStep("Step", 1.0, 1)); 
		Template template = new Template("Template", templateSteps); 
		Assignment assignment = new Assignment("Assignment", new Date(), template); 
		Task task1 = new Task("Task1", 0.5, assignment.getID()); 
		assignment.addTask(task1);
		Task task2 = new Task("Task2", 0.5, assignment.getID()); 
		assignment.addTask(task2);
		Task task3 = new Task("Task3", 1, assignment.getID()); 
		
		AssignmentBlock block1 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				task1); 
		AssignmentBlock block2 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task1); 
		AssignmentBlock block3 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task1); 
		AssignmentBlock block4 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 10), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), 
				task1); 
		
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
		
		
		blockList.add(block2); 
		blockList.add(block3); 
		blockList.add(block4); 
		blockList.add(unavailable1); 
		blockList.add(unavailable2); 
		blockList.add(unavailable3); 
		
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
		
		//Creating two new blocks
		AssignmentBlock block5 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 10), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), 
				task1);
		UnavailableBlock unavailable4 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 11), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 13), 
				null, false); 
				
		blockList.add(block5); 
		blockList.add(unavailable4);
		
		//And modifying two existing ones in the blockList
		block2.setTask(task2);
		block3.setTask(task2);
		
		//And creating an assignment block with an invalid ID to see if this block is rejected
		AssignmentBlock block6 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 10), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), 
				task3);
		blockList.add(block6); 
		
		//See if the merge added the nonexistent blocks, updated the existing blocks
		//and did not change unchanged existing blocks
		List<ITimeBlockable> blocksNotAdded = StorageService.mergeAllTimeBlocks(blockList);
		
		assertTrue(blocksNotAdded.size() == 1);
		assertEquals(((AssignmentBlock) blocksNotAdded.get(0)).fullString(), block6.fullString());
		
		assertEquals(StorageService.getAssignmentBlock(block5.getId()).fullString(), block5.fullString());
		assertEquals(StorageService.getUnavailableBlock(unavailable4.getId()).fullString(), unavailable4.fullString());
		
		assertEquals(StorageService.getAssignmentBlock(block2.getId()).fullString(), block2.fullString());
		assertEquals(StorageService.getAssignmentBlock(block3.getId()).fullString(), block3.fullString());
	}
	
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
				task); 
		AssignmentBlock block2 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task); 
		AssignmentBlock block3 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task); 
		AssignmentBlock block4 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 10), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), 
				task); 
		AssignmentBlock block5 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 6), 
				task); 
		
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
			StorageService.addTimeBlock(block5);
			StorageService.addTimeBlock(unavailable1);
			StorageService.addTimeBlock(unavailable2);
			StorageService.addTimeBlock(unavailable3);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assertTrue(StorageService.getAllAssignmentBlocksWithinRange(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 5)).size() == 4);
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
				task); 
		AssignmentBlock block2 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task); 
		AssignmentBlock block3 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				task); 
		AssignmentBlock block4 = new AssignmentBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 10), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), 
				task); 
		
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
		UnavailableBlock unavailable4 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 1), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), 
				null, false); 
		UnavailableBlock unavailable5 = new UnavailableBlock(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 6), 
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
			StorageService.addTimeBlock(unavailable4);
			StorageService.addTimeBlock(unavailable5);
		} 
		catch (StorageServiceException e) {
			fail(e.getMessage());
		} 
		
		assert(StorageService.getAllUnavailableBlocksWithinRange(
				new Date(System.currentTimeMillis() + (86400 * 1000) * 2), 
				new Date(System.currentTimeMillis() + (86400 * 1000) * 5)).size() == 4);
	}
}
