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
		final Date start = new Date();
		final Date due = new Date();
		final Date due2 = new Date();
		due.setTime(due.getTime() + TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS));
		due2.setTime(due.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		// Initialize the db
		try {
			StorageService.initialize(true);
		} catch (final StorageServiceException e) {
			System.err.println("FAILED TO INITIALIZE");
			return;
		}
		
		// Create some sample assignments
		final Assignment asgn = new Assignment("Test name", due, createBasicTemplate(), 30);
		final Assignment asgn2 = new Assignment("Asgn2", due2, createAnotherTemplate(), 21);
		
		try {
			// Add the templates to the db
			StorageService.addTemplate(asgn.getTemplate());
			StorageService.addTemplate(asgn2.getTemplate());
			// Add the assignments to the db
			StorageService.addAssignment(asgn);
			StorageService.addAssignment(asgn2);
		} catch (final StorageServiceException e2) {
			e2.printStackTrace();
		}
		
		// Add some unavailable blocks to the db
		final UnavailableBlock ub1 = new UnavailableBlock((Date) start.clone(), new Date(start.getTime() + 14400000));
		final UnavailableBlock ub2 = new UnavailableBlock(new Date(start.getTime() + 86400000), new Date(
				start.getTime() + 104400000));
		
		try {
			StorageService.addTimeBlock(ub1);
			StorageService.addTimeBlock(ub2);
		} catch (final StorageServiceException e1) {
			e1.printStackTrace();
		}
		
		TimeAllocator talloc = new TimeAllocator(asgn);
		try {
			talloc.insertAsgn(start, due);
		} catch (final NotEnoughTimeException net) {
			System.err.println("INSERTION FAILED");
			return;
		}
		
		// Push to db
		List<ITimeBlockable> results = talloc.getEntireBlockSet();
		List<ITimeBlockable> errs = StorageService.mergeAllTimeBlocks(results);
		if (errs.size() != 0) {
			System.err.println(":(");
		}
		
		talloc = new TimeAllocator(asgn2);
		try {
			talloc.insertAsgn(start, due2);
		} catch (final NotEnoughTimeException net) {
			System.err.println("INSERTION FAILED");
			return;
		}
		// Push to db
		results = talloc.getEntireBlockSet();
		errs = StorageService.mergeAllTimeBlocks(results);
		if (errs.size() != 0) {
			System.err.println(":( 2");
		}
		
		// Try to insert a 3rd assignment
		final Date start3 = new Date(System.currentTimeMillis() + 86400000);
		final Date due3 = new Date(start3.getTime() + (86400000 * 10)); // works for * 6
		final Assignment asgn3 = new Assignment("Asgn3", due3, createAThirdTemplate(), 70);
		
		// Fourth assignment
		final Date start4 = new Date(System.currentTimeMillis() + (86400000 * 4));
		final Date due4 = new Date(start3.getTime() + (86400000 * 11)); // works for * 6
		final Assignment asgn4 = new Assignment("Asgn4", due4, createBasicTemplate(), 50);
		
		try {
			// Add the templates to the db
			StorageService.addTemplate(asgn3.getTemplate());
			StorageService.addTemplate(asgn4.getTemplate());
			
			// Add the assignments to the db
			StorageService.addAssignment(asgn3);
			StorageService.addAssignment(asgn4);
		} catch (final StorageServiceException e3) {
			e3.printStackTrace();
		}
		
		talloc = new TimeAllocator(asgn3);
		try {
			talloc.insertAsgn(start, due3);
		} catch (final NotEnoughTimeException net) {
			System.err.println("INSERTION FAILED");
			return;
		}
		
		// Push to db
		results = talloc.getEntireBlockSet();
		errs = StorageService.mergeAllTimeBlocks(results);
		if (errs.size() != 0) {
			System.err.println(":( 3");
		}
		
		talloc = new TimeAllocator(asgn4);
		try {
			talloc.insertAsgn(start, due4);
		} catch (final NotEnoughTimeException net) {
			System.err.println("INSERTION FAILED");
			return;
		}
		// Push to db
		results = talloc.getEntireBlockSet();
		errs = StorageService.mergeAllTimeBlocks(results);
		if (errs.size() != 0) {
			System.err.println(":( 4");
		}
		
		// TODO: ensure that the results from the db are a valid calendar
		System.out.println("Asgn 1: [" + start + ", " + due + "]");
		System.out.println("Asgn 2: [" + start + ", " + due2 + "]");
		System.out.println("Asgn 3: [" + start3 + ", " + due3 + "]");
		System.out.println("Asgn 4: [" + start4 + ", " + due4 + "]");
		final List<UnavailableBlock> un = StorageService.getAllUnavailableBlocksWithinRange(start, due4);
		System.out.println("unavailables: ");
		for (int i = 0; i < un.size(); ++i) {
			final UnavailableBlock bl = un.get(i);
			System.out.println(bl.fullString());
		}
		final List<AssignmentBlock> as = StorageService.getAllAssignmentBlocksWithinRange(start, due4);
		System.out.println("assignments: ");
		for (int i = 0; i < as.size(); ++i) {
			final AssignmentBlock bl = as.get(i);
			System.out.println(bl.fullString());
		}
		
		// Try a drag operation
		final AssignmentBlock sampleBlock = as.get(0);
		TimeModifier.updateBlock(sampleBlock, new Date(sampleBlock.getStart().getTime() + 5000), new Date(sampleBlock
				.getEnd().getTime() + 5000));
		
		TimeModifier.updateBlock(sampleBlock, new Date(sampleBlock.getStart().getTime() - 3000), new Date(sampleBlock
				.getEnd().getTime() - 3000));
	}
	
	// Creates Template with three steps
	private static Template createBasicTemplate() {
		final String name = "Basic Template";
		final Template t = new Template(name);
		t.addStep(new TemplateStep("Step 1", .25, 0));
		t.addStep(new TemplateStep("Step 2", .25, 1));
		t.addStep(new TemplateStep("Step 3", .5, 2));
		return t;
	}
	
	private static Template createAnotherTemplate() {
		final String name = "Another Template";
		final Template t = new Template(name);
		t.addStep(new TemplateStep("p 1", .33, 0));
		t.addStep(new TemplateStep("p 2", .67, 1));
		return t;
	}
	
	private static Template createAThirdTemplate() {
		final String name = "Third time's the charm";
		final Template t = new Template(name);
		t.addStep(new TemplateStep("s1", .1, 0));
		t.addStep(new TemplateStep("s2", .05, 1));
		t.addStep(new TemplateStep("s3", .4, 2));
		t.addStep(new TemplateStep("s4", .25, 3));
		t.addStep(new TemplateStep("s5", .2, 4));
		return t;
	}
	
}
