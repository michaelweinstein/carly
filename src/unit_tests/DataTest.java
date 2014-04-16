package unit_tests;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import data.Assignment;
import data.IAssignment;

/**
 * Unit tests for key data structure classes, i.e.
 * Assignment, Task, Template, TemplateStep
 * 
 * @author miweinst
 *
 */

public class DataTest {
	
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
}
