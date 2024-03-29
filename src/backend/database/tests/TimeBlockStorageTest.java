package backend.database.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backend.database.StorageService;
import backend.database.StorageServiceException;
import backend.database.TimeBlockStorage;
import backend.database.TimeBlockStorage.DateRange;
import data.Assignment;
import data.AssignmentBlock;
import data.ITemplateStep;
import data.ITimeBlockable;
import data.Task;
import data.Template;
import data.TemplateStep;
import data.UnavailableBlock;

public class TimeBlockStorageTest {
	
	private final ByteArrayOutputStream	errorFd	= new ByteArrayOutputStream();
	private PrintStream					_oldStdErr;
	
	@Before
	public void setUp() throws Exception {
		_oldStdErr = System.err;
		System.setErr(new PrintStream(errorFd));
		StorageService.initialize(true);
	}
	
	@After
	public void tearDown() throws Exception {
		System.setErr(_oldStdErr);
		StorageService.cleanup();
	}
	
	 /*
	 * Testing the helper methods
	 */
	
	 @Test
	 public void weekRangeMidnightTwoChunks() {
	
		 Calendar cal = Calendar.getInstance();
		 cal.set(Calendar.YEAR, 2000);
		 cal.set(Calendar.MONTH, Calendar.JANUARY);
		 cal.set(Calendar.DAY_OF_MONTH, 1);
		 cal.set(Calendar.HOUR_OF_DAY, 0);
		 cal.set(Calendar.MINUTE, 0);
		 cal.set(Calendar.SECOND, 0);
		 cal.set(Calendar.MILLISECOND, 0);
		
		 //Set to Monday at midnight
		 cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		 Date earlier = cal.getTime();
		
		 //Set the later date to Monday at midnight one week later
		 cal.add(Calendar.WEEK_OF_YEAR, 1);
		 Date later = cal.getTime();
		
		 ArrayList<DateRange> ranges = TimeBlockStorage.splitIntoWeekRanges(earlier, later);
		
		 assertTrue(ranges.size() == 2);
		 assertEquals(ranges.get(0).start.toString(), earlier.toString());
		 assertEquals(ranges.get(ranges.size() - 1).end.toString(), later.toString());
	 }
	
	 @Test
	 public void weekRangeNotMidnightTwoChunks() {
	
	 Calendar cal = Calendar.getInstance();
	 cal.set(Calendar.YEAR, 2000);
	 cal.set(Calendar.MONTH, Calendar.JANUARY);
	 cal.set(Calendar.DAY_OF_MONTH, 1);
	 cal.set(Calendar.HOUR_OF_DAY, 0);
	 cal.set(Calendar.MINUTE, 0);
	 cal.set(Calendar.SECOND, 0);
	 cal.set(Calendar.MILLISECOND, 0);
	
	 //Set to Monday not at midnight
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	 cal.add(Calendar.HOUR_OF_DAY, 5);
	 Date earlier = cal.getTime();
	
	 //Set the later date to Monday not at midnight one week later
	 cal.add(Calendar.WEEK_OF_YEAR, 1);
	 Date later = cal.getTime();
	
	 ArrayList<DateRange> ranges = TimeBlockStorage.splitIntoWeekRanges(earlier, later);
	
	 assertTrue(ranges.size() == 2);
	 assertEquals(ranges.get(0).start.toString(), earlier.toString());
	 assertEquals(ranges.get(ranges.size() - 1).end.toString(), later.toString());
	 }
	
	 @Test
	 public void weekRangeMidnightMultipleChunks() {
	
	 Calendar cal = Calendar.getInstance();
	 cal.set(Calendar.YEAR, 2000);
	 cal.set(Calendar.MONTH, Calendar.JANUARY);
	 cal.set(Calendar.DAY_OF_MONTH, 1);
	 cal.set(Calendar.HOUR_OF_DAY, 0);
	 cal.set(Calendar.MINUTE, 0);
	 cal.set(Calendar.SECOND, 0);
	 cal.set(Calendar.MILLISECOND, 0);
	
	 //Set to Monday at midnight
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	 Date earlier = cal.getTime();
	
	 //Set the later date to Monday at midnight one week later
	 cal.add(Calendar.WEEK_OF_YEAR, 1);
	 cal.add(Calendar.DAY_OF_YEAR, 6);
	 Date later = cal.getTime();
	
	 ArrayList<DateRange> ranges = TimeBlockStorage.splitIntoWeekRanges(earlier, later);
	
	 assertTrue(ranges.size() == 2);
	 assertEquals(ranges.get(0).start.toString(), earlier.toString());
	 assertEquals(ranges.get(ranges.size() - 1).end.toString(), later.toString());
	 }
	
	 @Test
	 public void weekRangeNotMidnightMultipleChunks() {
	
	 Calendar cal = Calendar.getInstance();
	 cal.set(Calendar.YEAR, 2000);
	 cal.set(Calendar.MONTH, Calendar.JANUARY);
	 cal.set(Calendar.DAY_OF_MONTH, 1);
	 cal.set(Calendar.HOUR_OF_DAY, 0);
	 cal.set(Calendar.MINUTE, 0);
	 cal.set(Calendar.SECOND, 0);
	 cal.set(Calendar.MILLISECOND, 0);
	
	 //Set to Monday not at midnight
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	 cal.add(Calendar.HOUR_OF_DAY, 5);
	 Date earlier = cal.getTime();
	
	 //Set the later date to Monday not at midnight one week later
	 cal.add(Calendar.WEEK_OF_YEAR, 1);
	 cal.add(Calendar.DAY_OF_YEAR, 6);
	 Date later = cal.getTime();
	
	 ArrayList<DateRange> ranges = TimeBlockStorage.splitIntoWeekRanges(earlier, later);
	
	 assertTrue(ranges.size() == 3);
	 assertEquals(ranges.get(0).start.toString(), earlier.toString());
	 assertEquals(ranges.get(ranges.size() - 1).end.toString(), later.toString());
	 }
	
	 @Test
	 public void weekRangeNotMidnightDifferentYear() {
	
	 Calendar cal = Calendar.getInstance();
	 cal.set(Calendar.YEAR, 2000);
	 cal.set(Calendar.MONTH, Calendar.JANUARY);
	 cal.set(Calendar.DAY_OF_MONTH, 1);
	 cal.set(Calendar.HOUR_OF_DAY, 0);
	 cal.set(Calendar.MINUTE, 0);
	 cal.set(Calendar.SECOND, 0);
	 cal.set(Calendar.MILLISECOND, 0);
	
	 //Set to previous year
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	 cal.add(Calendar.WEEK_OF_YEAR, -1);
	 cal.add(Calendar.HOUR_OF_DAY, 5);
	 Date earlier = cal.getTime();
	
	 //Set the later date to Monday not at midnight one week later
	 cal.add(Calendar.WEEK_OF_YEAR, 1);
	 cal.add(Calendar.DAY_OF_YEAR, 6);
	 Date later = cal.getTime();
	
	 ArrayList<DateRange> ranges = TimeBlockStorage.splitIntoWeekRanges(earlier, later);
	
	 assertTrue(ranges.size() == 3);
	 assertEquals(ranges.get(0).start.toString(), earlier.toString());
	 assertEquals(ranges.get(ranges.size() - 1).end.toString(), later.toString());
	 }
	
	 @Test
	 public void weekRangeMidnightSunday() {
	
	 Calendar cal = Calendar.getInstance();
	 cal.set(Calendar.YEAR, 2000);
	 cal.set(Calendar.MONTH, Calendar.JANUARY);
	 cal.set(Calendar.DAY_OF_MONTH, 1);
	 cal.set(Calendar.HOUR_OF_DAY, 0);
	 cal.set(Calendar.MINUTE, 0);
	 cal.set(Calendar.SECOND, 0);
	 cal.set(Calendar.MILLISECOND, 0);
	
	 //Set to Sunday at midnight
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	 Date earlier = cal.getTime();
	
	 //Set the later date to Monday not at midnight one week later
	 cal.add(Calendar.WEEK_OF_YEAR, 1);
	 cal.add(Calendar.DAY_OF_YEAR, 6);
	 Date later = cal.getTime();
	
	 ArrayList<DateRange> ranges = TimeBlockStorage.splitIntoWeekRanges(earlier, later);
	
	 assertTrue(ranges.size() == 2);
	 assertEquals(ranges.get(0).start.toString(), earlier.toString());
	 assertEquals(ranges.get(ranges.size() - 1).end.toString(), later.toString());
	 }
	
	 @Test
	 public void weekRangeMidnightSundaytoMidnightSunday() {
	
	 Calendar cal = Calendar.getInstance();
	 cal.set(Calendar.YEAR, 2000);
	 cal.set(Calendar.MONTH, Calendar.JANUARY);
	 cal.set(Calendar.DAY_OF_MONTH, 1);
	 cal.set(Calendar.HOUR_OF_DAY, 0);
	 cal.set(Calendar.MINUTE, 0);
	 cal.set(Calendar.SECOND, 0);
	 cal.set(Calendar.MILLISECOND, 0);
	
	 //Set to Sunday at midnight
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	 Date earlier = cal.getTime();
	
	 //Set the later date to Monday not at midnight one week later
	 cal.add(Calendar.WEEK_OF_YEAR, 1);
	 Date later = cal.getTime();
	
	 ArrayList<DateRange> ranges = TimeBlockStorage.splitIntoWeekRanges(earlier, later);
	
	 assertTrue(ranges.size() == 1);
	 assertEquals(ranges.get(0).start.toString(), earlier.toString());
	 assertEquals(ranges.get(ranges.size() - 1).end.toString(), later.toString());
	 }
	
	 /*
	 * Testing Time Block (generic block) related functionality
	 */
	
	 @Test
	 public void addAssignmentBlock() {
	 // Create needed objects
	 final ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
	 templateSteps.add(new TemplateStep("Step", 1.0, 1));
	 final Template template = new Template("Template", templateSteps);
	 final Assignment assignment = new Assignment("Assignment", new Date(), template);
	 final Task task = new Task("Task", 1, 1, assignment.getID());
	 assignment.addTask(task);
	
	 final AssignmentBlock block1 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 3), task);
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTemplate(template);
	 StorageService.addAssignment(assignment);
	 StorageService.addTimeBlock(block1);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertEquals(StorageService.getAssignmentBlock(block1.getId()).fullString(), block1.fullString());
	 }
	
	 @Test
	 public void addUnavailableBlock() {
	 // Create needed objects
	 final UnavailableBlock unavailable1 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 2), new Date(System.currentTimeMillis() + (86400 * 1000) * 3));
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTimeBlock(unavailable1);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertEquals(StorageService.getUnavailableBlock(unavailable1.getId()).fullString(), unavailable1.fullString());
	 }
	
	 @Test
	 public void addTwoUnavailableBlocks() {
	 // Create needed objects
	 // Add some unavailable blocks to the db
	 final Date start = new Date();
	 final UnavailableBlock unavailable1 = new UnavailableBlock((Date) start.clone(), new Date(
	 start.getTime() + 14400000));
	 final UnavailableBlock unavailable2 = new UnavailableBlock(new Date(start.getTime() + 86400000), new Date(
	 start.getTime() + 104400000));
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTimeBlock(unavailable1);
	 StorageService.addTimeBlock(unavailable2);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertEquals(StorageService.getUnavailableBlock(unavailable1.getId()).fullString(), unavailable1.fullString());
	 assertEquals(StorageService.getUnavailableBlock(unavailable2.getId()).fullString(), unavailable2.fullString());
	 }
	
	 @Test
	 public void updateAssignmentBlock() {
	 // Create needed objects
	 final ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
	 templateSteps.add(new TemplateStep("Step", 1.0, 1));
	 final Template template = new Template("Template", templateSteps);
	 final Assignment assignment = new Assignment("Assignment", new Date(), template);
	 final Task task1 = new Task("Task1", 0.5, 1, assignment.getID());
	 assignment.addTask(task1);
	 final Task task2 = new Task("Task2", 0.5, 2, assignment.getID());
	 assignment.addTask(task2);
	
	 final AssignmentBlock block1 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 3), task1);
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTemplate(template);
	 StorageService.addAssignment(assignment);
	 StorageService.addTimeBlock(block1);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 block1.setEnd(new Date(System.currentTimeMillis() + (86400 * 1000) * 10));
	 block1.setTask(task2);
	
	 try {
	 StorageService.updateTimeBlock(block1);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertEquals(StorageService.getAssignmentBlock(block1.getId()).fullString(), block1.fullString());
	 }
	
	 @Test
	 public void updateUnavailableBlock() {
	 // Create needed objects
	 final UnavailableBlock unavailable1 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 2), new Date(System.currentTimeMillis() + (86400 * 1000) * 3));
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTimeBlock(unavailable1);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 unavailable1.setEnd(new Date(System.currentTimeMillis() + (86400 * 1000) * 10));
	
	 try {
	 StorageService.updateTimeBlock(unavailable1);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertEquals(StorageService.getUnavailableBlock(unavailable1.getId()).fullString(), unavailable1.fullString());
	 }
	
	 @Test
	 public void removeAssignmentBlock() {
	 // Create needed objects
	 final ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
	 templateSteps.add(new TemplateStep("Step", 1.0, 1));
	 final Template template = new Template("Template", templateSteps);
	 final Assignment assignment = new Assignment("Assignment", new Date(), template);
	 final Task task1 = new Task("Task1", 0.5, 1, assignment.getID());
	 assignment.addTask(task1);
	 final Task task2 = new Task("Task2", 0.5, 2, assignment.getID());
	 assignment.addTask(task2);
	
	 final AssignmentBlock block1 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 3), task1);
	 final AssignmentBlock block2 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 3),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 4), task1);
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTemplate(template);
	 StorageService.addAssignment(assignment);
	 StorageService.addTimeBlock(block1);
	 StorageService.addTimeBlock(block2);
	
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertEquals(StorageService.getAssignmentBlock(block1.getId()).fullString(), block1.fullString());
	
	 StorageService.removeTimeBlock(block1);
	
	 assertTrue(StorageService.getAssignmentBlock(block1.getId()) == null);
	 assertTrue(errorFd.toString().contains("ERROR:"));
	 }
	
	 @Test
	 public void removeUnavailableBlock() {
	 // Create needed objects
	 final UnavailableBlock unavailable1 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 2), new Date(System.currentTimeMillis() + (86400 * 1000) * 3));
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTimeBlock(unavailable1);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertEquals(StorageService.getUnavailableBlock(unavailable1.getId()).fullString(), unavailable1.fullString());
	
	 StorageService.removeTimeBlock(unavailable1);
	
	 assertTrue(StorageService.getAssignmentBlock(unavailable1.getId()) == null);
	 assertTrue(errorFd.toString().contains("ERROR:"));
	 }
	
	 @Test
	 public void mergeAllTimeBlocks() {
	 final List<ITimeBlockable> blockList = new ArrayList<>();
	
	 // Create needed objects
	 final ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
	 templateSteps.add(new TemplateStep("Step", 1.0, 1));
	 final Template template = new Template("Template", templateSteps);
	 final Assignment assignment = new Assignment("Assignment", new Date(), template);
	 final Task task1 = new Task("Task1", 0.5, 1, assignment.getID());
	 assignment.addTask(task1);
	 final Task task2 = new Task("Task2", 0.5, 2, assignment.getID());
	 assignment.addTask(task2);
	 final Task task3 = new Task("Task3", 1, 3, assignment.getID());
	
	 final AssignmentBlock block1 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 3), task1);
	 final AssignmentBlock block2 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 3),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 4), task1);
	 final AssignmentBlock block3 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 4), task1);
	 final AssignmentBlock block4 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 10),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 12), task1);
	
	 final UnavailableBlock unavailable1 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 2), new Date(System.currentTimeMillis() + (86400 * 1000) * 3));
	
	 final UnavailableBlock unavailable2 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 3), new Date(System.currentTimeMillis() + (86400 * 1000) * 4));
	
	 final UnavailableBlock unavailable3 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 11), new Date(System.currentTimeMillis() + (86400 * 1000) * 13));
	
	 blockList.add(block2);
	 blockList.add(block3);
	 blockList.add(block4);
	 blockList.add(unavailable1);
	 blockList.add(unavailable2);
	 blockList.add(unavailable3);
	
	 // Add objects in correct order to the db
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
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 // Creating two new blocks
	 final AssignmentBlock block5 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 10),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 12), task1);
	 final UnavailableBlock unavailable4 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 11), new Date(System.currentTimeMillis() + (86400 * 1000) * 13));
	
	 blockList.add(block5);
	 blockList.add(unavailable4);
	
	 // And modifying two existing ones in the blockList
	 block2.setTask(task2);
	 block3.setTask(task2);
	
	 // And creating an assignment block with an invalid ID to see if this block is rejected
	 final AssignmentBlock block6 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 10),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 12), task3);
	 blockList.add(block6);
	
	 // See if the merge added the nonexistent blocks, updated the existing blocks
	 // and did not change unchanged existing blocks
	 final List<ITimeBlockable> blocksNotAdded = StorageService.mergeAllTimeBlocks(blockList);
	
	 assertTrue(blocksNotAdded.size() == 1);
	 assertEquals(((AssignmentBlock) blocksNotAdded.get(0)).fullString(), block6.fullString());
	
	 assertEquals(StorageService.getAssignmentBlock(block5.getId()).fullString(), block5.fullString());
	 
	 //mergeTimeBlock now ignores all unavailable blocks!
	 assertTrue(StorageService.getUnavailableBlock(unavailable4.getId()) == null);
	
	 assertEquals(StorageService.getAssignmentBlock(block2.getId()).fullString(), block2.fullString());
	 assertEquals(StorageService.getAssignmentBlock(block3.getId()).fullString(), block3.fullString());
	 }
	
	 /*
	 * Testing Assignment Block retrieval
	 */
	
	 @Test
	 public void getAllAssignmentBlocksWithinRange() {
	 // Create needed objects
	 final ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
	 templateSteps.add(new TemplateStep("Step", 1.0, 1));
	 final Template template = new Template("Template", templateSteps);
	 final Assignment assignment = new Assignment("Assignment", new Date(), template);
	 final Task task = new Task("Task", 1, 1, assignment.getID());
	 assignment.addTask(task);
	
	 final AssignmentBlock block1 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 3), task);
	 final AssignmentBlock block2 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 3),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 4), task);
	 final AssignmentBlock block3 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 4), task);
	 final AssignmentBlock block4 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 10),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 12), task);
	 final AssignmentBlock block5 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 4),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 6), task);
	 final AssignmentBlock block6 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 1),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 7), task);
	
	 final UnavailableBlock unavailable1 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 2), new Date(System.currentTimeMillis() + (86400 * 1000) * 3));
	 final UnavailableBlock unavailable2 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 3), new Date(System.currentTimeMillis() + (86400 * 1000) * 4));
	 final UnavailableBlock unavailable3 = new UnavailableBlock(new Date(System.currentTimeMillis() + (86400 * 1000)
	 * 11), new Date(System.currentTimeMillis() + (86400 * 1000) * 13));
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTemplate(template);
	 StorageService.addAssignment(assignment);
	 StorageService.addTimeBlock(block1);
	 StorageService.addTimeBlock(block2);
	 StorageService.addTimeBlock(block3);
	 StorageService.addTimeBlock(block4);
	 StorageService.addTimeBlock(block5);
	 StorageService.addTimeBlock(block6);
	 StorageService.addTimeBlock(unavailable1);
	 StorageService.addTimeBlock(unavailable2);
	 StorageService.addTimeBlock(unavailable3);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertTrue(StorageService.getAllAssignmentBlocksWithinRange(
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
	 new Date(System.currentTimeMillis() + (86400 * 1000) * 5)).size() == 5);
	 }
	
	 /*
	 * Testing Unavailable Block retrieval
	 */
	
	 @Test
	 public void getAllUnavailableBlocksWithinRangeDefaultBlocks() {
	 //Create needed objects
	 ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
	 templateSteps.add(new TemplateStep("Step", 1.0, 1));
	 Template template = new Template("Template", templateSteps);
	 Assignment assignment = new Assignment("Assignment", new Date(), template);
	 Task task = new Task("Task", 1, 1, assignment.getID());
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
	
	 //We need to the date of all of the default timeBlocks to this week
	 Calendar cal = Calendar.getInstance();
	 cal.set(Calendar.YEAR, 1970);
	 cal.set(Calendar.MONTH, Calendar.JANUARY);
	 cal.set(Calendar.DAY_OF_MONTH, 4);
	 cal.set(Calendar.HOUR_OF_DAY, 0);
	 cal.set(Calendar.MINUTE, 0);
	 cal.set(Calendar.SECOND, 0);
	 cal.set(Calendar.MILLISECOND, 0);
	 long msWeekStart = cal.getTimeInMillis();
	
	 ArrayList<UnavailableBlock> unavailableBlocks = new ArrayList<>();
	 UnavailableBlock unavailable1 = new UnavailableBlock(
	 new Date(msWeekStart - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)),
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS)));
	 UnavailableBlock unavailable2 = new UnavailableBlock(
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(0, TimeUnit.HOURS)),
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS)));
	 UnavailableBlock unavailable3 = new UnavailableBlock(
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)),
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(25, TimeUnit.HOURS)));
	 UnavailableBlock unavailable4 = new UnavailableBlock(
	 new Date(msWeekStart - TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS)),
	 new Date(msWeekStart - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)));
	 UnavailableBlock unavailable5 = new UnavailableBlock(
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(166, TimeUnit.HOURS)),
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(170, TimeUnit.HOURS)));
	 UnavailableBlock unavailable6 = new UnavailableBlock(
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(170, TimeUnit.HOURS)),
	 new Date(msWeekStart + TimeUnit.MILLISECONDS.convert(174, TimeUnit.HOURS)));
	
	 unavailableBlocks.add(unavailable1);
	 unavailableBlocks.add(unavailable2);
	 unavailableBlocks.add(unavailable3);
	 unavailableBlocks.add(unavailable4);
	 unavailableBlocks.add(unavailable5);
	 unavailableBlocks.add(unavailable6);
	
	 // Add objects in correct order to the db
	 try {
	 StorageService.addTemplate(template);
	 StorageService.addAssignment(assignment);
	 StorageService.addTimeBlock(block1);
	 StorageService.addTimeBlock(block2);
	 StorageService.addTimeBlock(block3);
	 StorageService.addTimeBlock(block4);
	 StorageService.addAllDefaultUnavailableBlocks(unavailableBlocks);
	 } catch (final StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 cal.setTimeInMillis(System.currentTimeMillis());
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	 cal.set(Calendar.HOUR_OF_DAY, 0);
	 cal.set(Calendar.MINUTE, 0);
	 cal.set(Calendar.SECOND, 0);
	 final long msWeekStartNow = cal.getTimeInMillis();
	
	 assertTrue(StorageService.getAllUnavailableBlocksWithinRange(new Date(msWeekStartNow),
	 new Date(msWeekStartNow + TimeUnit.MILLISECONDS.convert(168, TimeUnit.HOURS))).size() == 4);
	 }
	
	 @Test
	 public void getAllUnavailableBlocksWithinRangeUserAdjusted() {
	 //Create needed objects
	 ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
	 templateSteps.add(new TemplateStep("Step", 1.0, 1));
	 Template template = new Template("Template", templateSteps);
	 Assignment assignment = new Assignment("Assignment", new Date(), template);
	 Task task = new Task("Task", 1, 1, assignment.getID());
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
	
	 //We need to the date of all of the default timeBlocks to this week
	 Calendar cal = Calendar.getInstance();
	 cal.setTimeInMillis(System.currentTimeMillis());
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	 cal.set(Calendar.HOUR_OF_DAY,0);
	 cal.set(Calendar.MINUTE,0);
	 cal.set(Calendar.SECOND,0);
	 cal.set(Calendar.MILLISECOND,0);
	 long msBase = cal.getTimeInMillis();
	
	 UnavailableBlock unavailable1 = new UnavailableBlock(
	 new Date(msBase - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS)));
	 UnavailableBlock unavailable2 = new UnavailableBlock(
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(0, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS)));
	 UnavailableBlock unavailable3 = new UnavailableBlock(
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(25, TimeUnit.HOURS)));
	 UnavailableBlock unavailable4 = new UnavailableBlock(
	 new Date(msBase - TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS)),
	 new Date(msBase - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)));
	 UnavailableBlock unavailable5 = new UnavailableBlock(
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(166, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(170, TimeUnit.HOURS)));
	 UnavailableBlock unavailable6 = new UnavailableBlock(
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(170, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(174, TimeUnit.HOURS)));
	
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
	 StorageService.addTimeBlock(unavailable6);
	 }
	 catch (StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertTrue(StorageService.getAllUnavailableBlocksWithinRange(
	 new Date(msBase),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(168, TimeUnit.HOURS))).size() == 4);
	 }
	
	 @Test
	 public void getAllUnavailableBlocksWithinRangeUserAdjustedEliminateDuplicates() {
	 //Create needed objects
	 ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
	 templateSteps.add(new TemplateStep("Step", 1.0, 1));
	 Template template = new Template("Template", templateSteps);
	 Assignment assignment = new Assignment("Assignment", new Date(), template);
	 Task task = new Task("Task", 1, 1, assignment.getID());
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
	
	 //We need to the date of all of the user adjusted timeBlocks to this week
	 Calendar cal = Calendar.getInstance();
	 cal.setTimeInMillis(System.currentTimeMillis());
	 cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
	 cal.set(Calendar.HOUR_OF_DAY,0);
	 cal.set(Calendar.MINUTE,0);
	 cal.set(Calendar.SECOND,0);
	 cal.set(Calendar.MILLISECOND,0);
	 long msBase = cal.getTimeInMillis();
	
	 UnavailableBlock unavailable1 = new UnavailableBlock(
	 new Date(msBase - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS)));
	 UnavailableBlock unavailable2 = new UnavailableBlock(
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(0, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS)));
	 UnavailableBlock unavailable3 = new UnavailableBlock(
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(25, TimeUnit.HOURS)));
	 UnavailableBlock unavailable4 = new UnavailableBlock(
	 new Date(msBase - TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS)),
	 new Date(msBase - TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)));
	 UnavailableBlock unavailable5 = new UnavailableBlock(
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(166, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(170, TimeUnit.HOURS)));
	 UnavailableBlock unavailable6 = new UnavailableBlock(
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(170, TimeUnit.HOURS)),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(174, TimeUnit.HOURS)));
	
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
	 StorageService.addTimeBlock(unavailable6);
	 }
	 catch (StorageServiceException e) {
	 fail(e.getMessage());
	 }
	
	 assertTrue(StorageService.getAllUnavailableBlocksWithinRange(
	 new Date(msBase),
	 new Date(msBase + TimeUnit.MILLISECONDS.convert(169, TimeUnit.HOURS))).size() == 4);
	 }
	
	@Test
	public void replaceUnavailableBlocks() {
		// Create needed objects
		final ArrayList<ITemplateStep> templateSteps = new ArrayList<>();
		templateSteps.add(new TemplateStep("Step", 1.0, 1));
		final Template template = new Template("Template", templateSteps);
		final Assignment assignment = new Assignment("Assignment", new Date(), template);
		final Task task = new Task("Task", 1, 1, assignment.getID());
		assignment.addTask(task);
		
		final AssignmentBlock block1 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
				new Date(System.currentTimeMillis() + (86400 * 1000) * 3), task);
		final AssignmentBlock block2 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 3),
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), task);
		final AssignmentBlock block3 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 2),
				new Date(System.currentTimeMillis() + (86400 * 1000) * 4), task);
		final AssignmentBlock block4 = new AssignmentBlock(new Date(System.currentTimeMillis() + (86400 * 1000) * 10),
				new Date(System.currentTimeMillis() + (86400 * 1000) * 12), task);
		
		// We need to the date of all of the default timeBlocks to this week
		final Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(0));
		cal.add(Calendar.DAY_OF_YEAR, 3);
		final long msBaseStart = cal.getTimeInMillis();
		
		final ArrayList<UnavailableBlock> defaultBlocks = new ArrayList<>();
		final UnavailableBlock default1 = new UnavailableBlock(new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)), new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS)));
		final UnavailableBlock default2 = new UnavailableBlock(new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(12, TimeUnit.HOURS)), new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(14, TimeUnit.HOURS)));
		final UnavailableBlock default3 = new UnavailableBlock(new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)), new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(25, TimeUnit.HOURS)));
		final UnavailableBlock default4 = new UnavailableBlock(new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(49, TimeUnit.HOURS)), new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(54, TimeUnit.HOURS)));
		final UnavailableBlock default5 = new UnavailableBlock(new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(102, TimeUnit.HOURS)), new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(106, TimeUnit.HOURS)));
		final UnavailableBlock default6 = new UnavailableBlock(new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(125, TimeUnit.HOURS)), new Date(msBaseStart
			+ TimeUnit.MILLISECONDS.convert(130, TimeUnit.HOURS)));
		
		defaultBlocks.add(default1);
		defaultBlocks.add(default2);
		defaultBlocks.add(default3);
		defaultBlocks.add(default4);
		defaultBlocks.add(default5);
		defaultBlocks.add(default6);
		
		// We need to the date of all of the user adjusted timeBlocks to this week
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		final long msWeekStart = cal.getTimeInMillis();
		final Date weekStart = cal.getTime();
		cal.add(Calendar.WEEK_OF_YEAR, 1);
		final Date weekEnd = cal.getTime();
		
		final ArrayList<UnavailableBlock> unavailableList = new ArrayList<>();
		final UnavailableBlock unavailable1 = new UnavailableBlock(new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)), new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS)));
		final UnavailableBlock unavailable2 = new UnavailableBlock(new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(12, TimeUnit.HOURS)), new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(14, TimeUnit.HOURS)));
		final UnavailableBlock unavailable3 = new UnavailableBlock(new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS)), new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(25, TimeUnit.HOURS)));
		final UnavailableBlock unavailable4 = new UnavailableBlock(new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(49, TimeUnit.HOURS)), new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(54, TimeUnit.HOURS)));
		final UnavailableBlock unavailable5 = new UnavailableBlock(new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(102, TimeUnit.HOURS)), new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(106, TimeUnit.HOURS)));
		final UnavailableBlock unavailable6 = new UnavailableBlock(new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(125, TimeUnit.HOURS)), new Date(msWeekStart
			+ TimeUnit.MILLISECONDS.convert(130, TimeUnit.HOURS)));
		
		unavailableList.add(unavailable1);
		unavailableList.add(unavailable2);
		unavailableList.add(unavailable3);
		unavailableList.add(unavailable4);
		unavailableList.add(unavailable5);
		unavailableList.add(unavailable6);
		
		// Add objects in correct order to the db
		try {
			StorageService.addTemplate(template);
			StorageService.addAssignment(assignment);
			StorageService.addTimeBlock(block1);
			StorageService.addTimeBlock(block2);
			StorageService.addTimeBlock(block3);
			StorageService.addTimeBlock(block4);
			
			StorageService.addAllDefaultUnavailableBlocks(defaultBlocks);
		} catch (final StorageServiceException e) {
			fail(e.getMessage());
		}
		
		final List<UnavailableBlock> before = StorageService.getAllUnavailableBlocksWithinRange(weekStart, weekEnd);
		
		StorageService.replaceUnavailableBlocks(weekStart, weekEnd, unavailableList);
		
		final List<UnavailableBlock> after = StorageService.getAllUnavailableBlocksWithinRange(weekStart, weekEnd);
		
		assertEquals(before.toString(), after.toString());
	}
}
