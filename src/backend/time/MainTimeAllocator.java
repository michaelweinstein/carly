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
			e1.printStackTrace();
		}
		
		TimeAllocator talloc = new TimeAllocator(asgn);
		talloc.insertAsgn(start, due);
		
		//Push to db
		List<ITimeBlockable> results = talloc.getEntireBlockSet();
		List<ITimeBlockable> errs = StorageService.mergeAllTimeBlocks(results);
		if(errs.size() != 0)
			System.err.println(":(");
//		List<ITimeBlockable> results = talloc.getEntireBlockSet();
//		System.out.println("First talloc block set call");
//		for(int i = 0; i < results.size(); ++i) {
//			ITimeBlockable block = results.get(i);
//			System.out.println(block.toString());
//			
//			try {
//				if(block.isMovable()) //Temp fix -- use MERGE functions or ADDALL functions later
//					StorageService.addTimeBlock(block);
//			} catch (StorageServiceException e) {
//				e.printStackTrace();
//			}
//		}
//		System.out.println("results size 1: " + results.size());
		
		talloc = new TimeAllocator(asgn2);
		talloc.insertAsgn(start, due2);
		
		//Push to db
		results = talloc.getEntireBlockSet();
		errs = StorageService.mergeAllTimeBlocks(results);
		if(errs.size() != 0)
			System.err.println(":( 2");
//		results = talloc.getEntireBlockSet();
//		System.out.println("Second talloc block set call");
//		for(int i = 0; i < results.size(); ++i) {
//			ITimeBlockable block = results.get(i);			
//			System.out.println(block.toString());
//			
//			try {
//				if(block.isMovable()) //Temp fix -- use MERGE functions or ADDALL functions later
//					StorageService.addTimeBlock(block);			
//				} catch (StorageServiceException e) {
//				e.printStackTrace();
//			}
//		}
//		System.out.println("results size 2: " + results.size());
		
		
		
		//Try to insert a 3rd assignment
		Date start3 = new Date(System.currentTimeMillis() + 86400000);
		Date due3 = new Date(start3.getTime() + (86400000 * 10)); //works for  * 6
		Assignment asgn3 = new Assignment("Asgn3", due3, createAThirdTemplate(), 70);

		//Fourth assignment
		Date start4 = new Date(System.currentTimeMillis() + (86400000 * 4));
		Date due4 = new Date(start3.getTime() + (86400000 * 11)); //works for  * 6
		Assignment asgn4 = new Assignment("Asgn4", due4, createBasicTemplate(), 50);
		
		
		try {
			//Add the templates to the db
			StorageService.addTemplate(asgn3.getTemplate());
			StorageService.addTemplate(asgn4.getTemplate());
			
			//Add the assignments to the db
			StorageService.addAssignment(asgn3);
			StorageService.addAssignment(asgn4);
		} catch (StorageServiceException e3) {
			e3.printStackTrace();
		}
		
		
		talloc = new TimeAllocator(asgn3);
		talloc.insertAsgn(start3, due3);
		
		//Push to db
		results = talloc.getEntireBlockSet();
		errs = StorageService.mergeAllTimeBlocks(results);
		if(errs.size() != 0)
			System.err.println(":( 3");		
		
		
		talloc = new TimeAllocator(asgn4);
		talloc.insertAsgn(start4, due4);
		
		//Push to db
		results = talloc.getEntireBlockSet();
		errs = StorageService.mergeAllTimeBlocks(results);
		if(errs.size() != 0)
			System.err.println(":( 4");		
		
		
		//TODO: ensure that the results from the db are a valid calendar
		System.out.println("Asgn 1: [" + start + ", " + due + "]");
		System.out.println("Asgn 2: [" + start + ", " + due2 + "]");
		System.out.println("Asgn 3: [" + start3 + ", " + due3 + "]");
		System.out.println("Asgn 4: [" + start4 + ", " + due4 + "]");
		List<UnavailableBlock> un = StorageService.getAllUnavailableBlocksWithinRange(start, due4);
		System.out.println("unavailables: ");
		for(int i = 0; i < un.size(); ++i) {
			UnavailableBlock bl = un.get(i);
			System.out.println(bl.fullString());
		}
		List<AssignmentBlock> as = StorageService.getAllAssignmentBlocksWithinRange(start, due4);
		System.out.println("assignments: ");
		for(int i = 0; i < as.size(); ++i) {
			AssignmentBlock bl = as.get(i);
			System.out.println(bl.fullString());
		}
		
		//Try a drag operation
		AssignmentBlock sampleBlock = as.get(0);
		TimeModifier.updateBlock(sampleBlock, new Date(sampleBlock.getStart().getTime() + 5000), 
				new Date(sampleBlock.getEnd().getTime() + 5000));
		
		TimeModifier.updateBlock(sampleBlock, new Date(sampleBlock.getStart().getTime() - 3000), 
				new Date(sampleBlock.getEnd().getTime() - 3000));
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

	private Template createAThirdTemplate() {
		String name = "Third time's the charm";
		Template t = new Template(name);		
		t.addStep(new TemplateStep("s1", .1, 0));
		t.addStep(new TemplateStep("s2", .05, 1));
		t.addStep(new TemplateStep("s3", .4, 2));
		t.addStep(new TemplateStep("s4", .25, 3));
		t.addStep(new TemplateStep("s5", .2, 4));
		return t;
	}
	
}
