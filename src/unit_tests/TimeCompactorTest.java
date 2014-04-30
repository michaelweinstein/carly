package unit_tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import backend.time.TimeAllocator;
import backend.time.TimeCompactor;
import backend.time.TimeUtilities;
import data.Assignment;
import data.AssignmentBlock;
import data.ITimeBlockable;
import data.Template;
import data.TemplateStep;
import data.UnavailableBlock;


public class TimeCompactorTest {
	
	
	@Test
	public void testSimpleCompaction() {
		List<ITimeBlockable> allBlocks = new ArrayList<ITimeBlockable>();
		Date start = new Date();
		Date end = new Date(start.getTime() + 36000000);
		AssignmentBlock b1 = new AssignmentBlock(start, new Date(start.getTime() + 1080000), null);
		UnavailableBlock b2 = new UnavailableBlock(new Date(start.getTime() + 1100000), 
				new Date(start.getTime() + 1800000), null);
		AssignmentBlock b3 = new AssignmentBlock(new Date(start.getTime() + 20000000),
				new Date(start.getTime() + 25000000), null);
		AssignmentBlock b4 = new AssignmentBlock(new Date(start.getTime() + 28000000),
				new Date(start.getTime() + 34000000), null);
		
		//Insert all blocks
		allBlocks.add(b1);
		allBlocks.add(b2);
		allBlocks.add(b3);
		allBlocks.add(b4);
		
		System.out.println("Compact Test 1: ");
		System.out.println(TimeUtilities.printSchedule(allBlocks));
		System.out.println("----");
		//Test compaction - the UnavailableBlock in the middle should not be moved
		TimeCompactor.compact(allBlocks, start, end, new Date());
		
		System.out.println(TimeUtilities.printSchedule(allBlocks));
		System.out.println("******************************************");
	}
	
	@Test
	public void testSimpleDecompaction() {
		List<ITimeBlockable> allBlocks = new ArrayList<ITimeBlockable>();
		Date start = new Date();
		Date end = new Date(start.getTime() + 36000000);
		AssignmentBlock b1 = new AssignmentBlock(start, new Date(start.getTime() + 1080000), null);
		UnavailableBlock b2 = new UnavailableBlock(new Date(start.getTime() + 1100000), 
				new Date(start.getTime() + 1800000), null);
		AssignmentBlock b3 = new AssignmentBlock(new Date(start.getTime() + 20000000),
				new Date(start.getTime() + 25000000), null);
		AssignmentBlock b4 = new AssignmentBlock(new Date(start.getTime() + 28000000),
				new Date(start.getTime() + 34000000), null);
		
		
		//Insert all blocks
		allBlocks.add(b1);
		allBlocks.add(b2);
		allBlocks.add(b3);
		allBlocks.add(b4);
		
		System.out.println("Decompact Test 1, (Start, End) = (" + start + ", " + end + ")");
		System.out.println(TimeUtilities.printSchedule(allBlocks));
		System.out.println("----");
		//Test de-compaction - the UnavailableBlock in the middle should not be moved
		TimeCompactor.decompact(allBlocks, start, end);
		
		System.out.println(TimeUtilities.printSchedule(allBlocks));
		System.out.println("******************************************");
	}
	
	@Test
	public void decompaction2(){ 
		Date start = new Date();
		Date due = new Date(start.getTime() + TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS));
		
		
		List<ITimeBlockable> allBlocks = new ArrayList<ITimeBlockable>();
		
		//Add the blocks into the list
		UnavailableBlock ub1 = new UnavailableBlock((Date) start.clone(), new Date(start.getTime() + 14400000), null);
		UnavailableBlock ub2 = new UnavailableBlock(new Date(start.getTime() + 86400000),
				new Date(start.getTime() + 104400000), null);
		allBlocks.add(ub1);
		allBlocks.add(new AssignmentBlock(new Date(start.getTime() + 14400000), 
				new Date(start.getTime() + 86300000), null));
		allBlocks.add(ub2);
		allBlocks.add(new AssignmentBlock(new Date(start.getTime() + 104400000), 
				new Date(start.getTime() + 110000000), null));
		allBlocks.add(new AssignmentBlock(new Date(start.getTime() + 110000000), 
				new Date(start.getTime() + 130000000), null));
		allBlocks.add(new AssignmentBlock(new Date(start.getTime() + 130000000), 
				new Date(start.getTime() + 150000000), null));
	
		System.out.println("Decompact Test 2, (Start, End) = (" + start + ", " + due + ")");
		System.out.println(TimeUtilities.printSchedule(allBlocks));
		System.out.println("----");
		//Test de-compaction - the UnavailableBlock in the middle should not be moved
		TimeCompactor.decompact(allBlocks, start, due);
		
		System.out.println(TimeUtilities.printSchedule(allBlocks));
		System.out.println("******************************************");
	}
	
	
}
