package backend;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import data.Assignment;
import data.AssignmentBlock;
import data.ITimeBlockable;
import data.Template;
import data.TemplateStep;
import data.UnavailableBlock;


public class MainTimeAllocator {
	
	
	public MainTimeAllocator() {
		Date start = new Date();
		Date due = new Date();
		due.setTime(due.getTime() + TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS));
		
		//Initialize the db
		StorageService.initialize(true);
		
		
		
		Assignment asgn = new Assignment("Test name", due, createBasicTemplate(), 30);
		Assignment asgn2 = new Assignment("Asgn2", 
				new Date(due.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)),
				createAnotherTemplate(), 21);
		TimeAllocator talloc = new TimeAllocator(asgn);
		talloc.insertAsgn();
		
		//Add the assignments to the db
		StorageService.addAssignment(asgn);
		StorageService.addAssignment(asgn2);
		
		//Add the templates to the db
		StorageService.addTemplate(asgn.getTemplate());
		StorageService.addTemplate(asgn2.getTemplate());
		
		//Push to db
		List<ITimeBlockable> results = talloc.getEntireBlockSet();
		System.out.println("First talloc block set call");
		for(int i = 0; i < results.size(); ++i) {
			System.out.println(results.get(i).toString());
			StorageService.addTimeBlock(results.get(i));
		}
		
		talloc = new TimeAllocator(asgn2);
		talloc.insertAsgn();
		
		//Push to db
		results = talloc.getEntireBlockSet();
		System.out.println("Second talloc block set call");
		for(int i = 0; i < results.size(); ++i) {
			System.out.println(results.get(i).toString());
			StorageService.addTimeBlock(results.get(i));
		}
		
		
		//TODO: ensure that the results from the db are a valid calendar
		List<UnavailableBlock> un = StorageService.getAllUnavailableBlocksWithinRange(start, due);
		System.out.println("unavailables: ");
		for(int i = 0; i < un.size(); ++i) {
			UnavailableBlock bl = un.get(i);
			System.out.println("Start: " + bl.getStart() + " || End: " + bl.getEnd());
		}
		List<AssignmentBlock> as = StorageService.getAllAssignmentBlocksWithinRange(start, due);
		System.out.println("assignments: ");
		for(int i = 0; i < as.size(); ++i) {
			AssignmentBlock bl = as.get(i);
			System.out.println("Start: " + bl.getStart() + " || End: " + bl.getEnd());
		}
	}
	
	public static void main(String[] args) {
		new MainTimeAllocator();
	}
	
	
	// Creates Template with three steps 
	private Template createBasicTemplate() {
		String name = "Basic Template";
		Template t = new Template(name);		
		t.addStep(new TemplateStep("Step 1", .25, 0));
		t.addStep(new TemplateStep("Step 2", .25, 1));
		t.addStep(new TemplateStep("Step 3", .5, 2));	
		return t;
	}
	
	private Template createAnotherTemplate() {
		String name = "Another Template";
		Template t = new Template(name);		
		t.addStep(new TemplateStep("p 1", .33, 0));
		t.addStep(new TemplateStep("p 2", .67, 1));
		return t;
	}
	
}
