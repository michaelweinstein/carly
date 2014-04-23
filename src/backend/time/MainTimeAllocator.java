package backend.time;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import backend.database.StorageService;
import backend.database.StorageServiceException;

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
		Date due2 = new Date();
		due.setTime(due.getTime() + TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS));
		due2.setTime(due.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		//Initialize the db
		StorageService.initialize(true);
		
		//Create some sample assignments
		Assignment asgn = new Assignment("Test name", due, createBasicTemplate(), 30);
		Assignment asgn2 = new Assignment("Asgn2", due2, createAnotherTemplate(), 21);
		
		try {
			//Add the templates to the db
			StorageService.addTemplate(asgn.getTemplate());
			StorageService.addTemplate(asgn2.getTemplate());
			//Add the assignments to the db
			StorageService.addAssignment(asgn);
			StorageService.addAssignment(asgn2);
		} catch (StorageServiceException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//Add some unavailable blocks to the db
		UnavailableBlock ub1 = new UnavailableBlock((Date) start.clone(), new Date(start.getTime() + 14400000), null, false);
		UnavailableBlock ub2 = new UnavailableBlock(new Date(start.getTime() + 86400000),
				new Date(start.getTime() + 104400000), null, false);
		
		try {
			StorageService.addTimeBlock(ub1);
			StorageService.addTimeBlock(ub2);
		} catch (StorageServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		TimeAllocator talloc = new TimeAllocator(asgn);
		talloc.insertAsgn(start, due);
		
		//Push to db
		List<ITimeBlockable> results = talloc.getEntireBlockSet();
		System.out.println("First talloc block set call");
		for(int i = 0; i < results.size(); ++i) {
			ITimeBlockable block = results.get(i);
			System.out.println(block.toString());
			
			try {
				if(block.isMovable()) //Temp fix -- use MERGE functions or ADDALL functions later
					StorageService.addTimeBlock(block);
			} catch (StorageServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("results size 1: " + results.size());
		
		talloc = new TimeAllocator(asgn2);
		talloc.insertAsgn(start, due2);
		
		//Push to db
		results = talloc.getEntireBlockSet();
		System.out.println("Second talloc block set call");
		for(int i = 0; i < results.size(); ++i) {
			ITimeBlockable block = results.get(i);			
			System.out.println(block.toString());
			
			try {
				if(block.isMovable()) //Temp fix -- use MERGE functions or ADDALL functions later
					StorageService.addTimeBlock(block);			
				} catch (StorageServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("results size 2: " + results.size());
		
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
