package unit_tests;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import data.Assignment;
import data.IAssignment;
import data.ITask;
import data.ITemplateStep;
import data.Template;
import data.TemplateStep;

/**
 * Unit tests for key data structure classes, i.e.
 * Assignment, Task, Template, TemplateStep
 * 
 * @author miweinst
 *
 */

public class DataTest {
	
	@Test
	public void createSimpleTemplate()
	{
		/**
		 * Creates a simple Template with
		 * TemplateSteps and tests data access.
		 */
		String name = "Test Template";
		Template t = new Template(name);		
		/* Basic data access test */
		assertTrue(t.getName().equals(name));		
		/* No steps added yet */
		List<ITemplateStep> steps = t.getAllSteps();
		assertTrue(steps.isEmpty());
		assertTrue(t.getStepByName("I don't exist") == null);		
		TemplateStep s1 = new TemplateStep("Step 1", .25, 0);
		t.addStep(s1);
		TemplateStep s2 = new TemplateStep("Step 2", .25, 1);
		t.addStep(s2);
		TemplateStep s3 = new TemplateStep("Step 3", .5, 2);
		t.addStep(s3);		
		/* Steps added */	
		steps = t.getAllSteps();
		assertTrue(steps.size() == 3);
		assertTrue(t.getStepByName("Step 1").equals(s1));
		assertTrue(t.getStepByName("Step 2").equals(s2));
		assertTrue(t.removeStep(t.getStepByName("Step 3")).equals(s3));
		assertTrue(steps.size() == 2);
	}
	
	@Test
	public void assignmentWithoutTemplate() 
	{
		/**
		 * Tests Assignment methods for adding
		 * and removing Tasks. Also tests simple
		 * Task methods.
		 */
		
		// TODO
	}
	
	@Test
	public void assignmentWithTemplate()
	{
		/**
		 * Creates a Template and then an Assignment 
		 * based on that template. Mostly tests 
		 * private method Assignment.createTasksFromTemplate
		 */
		Template t = createBasicTemplate();
		Assignment asgn = new Assignment("With Template", getCurrentDate(), t);
		
		/* Test createTasksFromTemplate ran successfully */
		List<ITask> tasks = asgn.getTasks();
		assertTrue(tasks.size() == t.getAllSteps().size());
		
		/* Test information in Task */
		ITask task = tasks.get(1);
		assertTrue(task.getName().equals("With Template:Step 2"));
		assertTrue(task.getAssignmentID().equals(asgn.getID()));
		assertTrue(task.getPercentComplete() == 0);
		assertTrue(task.getPercentOfTotal() == t.getStepByName("Step 2").getPercentOfTotal());
		assertTrue(task.getSuggestedBlockLength() == t.getPreferredConsecutiveHours());
	}
	
	@Test
	public void assignmentTrinity() 
	{
		/**
		 * Tests equals, hashCode and toString overloaded
		 * in the Assignment class. 
		 */
		/* Create Assignment with no Template*/
		String name = "Test Assignment";
		Date dueDate = new Date(System.currentTimeMillis());
		IAssignment asgn1 = new Assignment(name, dueDate, null, 5);
		IAssignment asgn2 = new Assignment(name, dueDate, null, 5);
		
		/* equals */		
		assertTrue(asgn1.equals(asgn1));
		assertTrue(!asgn1.equals(asgn2)); // ame data, different UID
		
		/* hashCode */
		assertTrue(asgn1.hashCode() == asgn1.hashCode());
		assertTrue(asgn1.hashCode() != asgn2.hashCode());
		
		/* toString */
		assertTrue(asgn1.toString().equals(new String(name + ", " + asgn1.getID())));
	}
	
	@Test
	public void simpleAssignment1() 
	{
		/**
		 * Tests the Assignment(name, date, template, expectedHours) 
		 * constructor; tests simple performance of accessors and mutators.
		 */
		
		/* Create Assignment with no Template*/
		String name = "Test Assignment";
		Date dueDate = new Date(System.currentTimeMillis());
		IAssignment test = new Assignment(name, dueDate, null, 5);
		
		/* Test accessors */
		assertTrue(test.getName().equals(name));
		assertTrue(test.getTemplate() == null);
		// No Template, so Tasks list is null at initialization
		assertTrue(test.getTasks().isEmpty());
		assertTrue(test.getDueDate().equals(dueDate));
		// Used first constructor, so at DEFAULT expected hours
		assertTrue(test.getExpectedHours() == 5);
		// Test mutator and check again
		test.setExpectedHours(6);
		assertTrue(test.getExpectedHours() == 6);
	}
	
	/* HELPER METHODS */
	
	// Creates Template with three steps 
	private Template createBasicTemplate() {
		String name = "Basic Template";
		Template t = new Template(name);		
		t.addStep(new TemplateStep("Step 1", .25, 0));
		t.addStep(new TemplateStep("Step 2", .25, 1));
		t.addStep(new TemplateStep("Step 3", .5, 2));	
		return t;
	}
	
	// Returns the Date based on current time
	private Date getCurrentDate() {
		return new Date(System.currentTimeMillis());
	}
}
