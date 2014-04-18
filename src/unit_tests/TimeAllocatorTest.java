package unit_tests;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import backend.TimeAllocator;
import data.Assignment;
import data.ITimeBlockable;
import data.Template;
import data.TemplateStep;

public class TimeAllocatorTest {


	
	@Test
	public void testAllocation() {
		Date due = new Date();
		due.setTime(due.getTime() + TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS));
		
		//Create a template for the assignment
		String name = "Basic Template";
		Template t = new Template(name);		
		t.addStep(new TemplateStep("Step 1", .25, 0));
		t.addStep(new TemplateStep("Step 2", .25, 1));
		t.addStep(new TemplateStep("Step 3", .5, 2));
		
		//Create a template for assignment 2
		String name2 = "Another Template";
		Template t2 = new Template(name2);		
		t2.addStep(new TemplateStep("p 1", .33, 0));
		t2.addStep(new TemplateStep("p 2", .67, 1));
		
		
		Assignment asgn = new Assignment("Test name", due, t, 30);
		Assignment asgn2 = new Assignment("Asgn2", 
				new Date(due.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)), t2, 21);
		TimeAllocator talloc = new TimeAllocator(asgn);
		talloc.insertAsgn();
		
		List<ITimeBlockable> results = talloc.getEntireBlockSet();
	}
	
}
