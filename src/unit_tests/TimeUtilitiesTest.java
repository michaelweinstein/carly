package unit_tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import backend.time.TimeUtilities;
import data.AssignmentBlock;
import data.ITimeBlockable;
import data.UnavailableBlock;

public class TimeUtilitiesTest {
	
	@Test
	public void testInsertion() {
		List<ITimeBlockable> blocks = new ArrayList<ITimeBlockable>();
		
		final Date d1 = new Date();
		final Date d2 = new Date(d1.getTime() + 10800000); // 3 hrs later
		final Date d3 = new Date(d2.getTime() + 5400000); // 1.5 hrs later
		final Date d4 = new Date(d3.getTime() + 3600000); // 1 hr later
		
		blocks.add(new AssignmentBlock(d1, d2, null));
		blocks.add(new UnavailableBlock(d3, d4));
		
		// Insert a block at the beginning
		final AssignmentBlock b1 = new AssignmentBlock(new Date(d1.getTime() - 50000), (Date) d1.clone(), null);
		TimeUtilities.insertIntoSortedList(blocks, b1);
		assertTrue(blocks.get(0) == b1);
		
		// Insert a block at the end
		final AssignmentBlock b2 = new AssignmentBlock(new Date(d4.getTime()), new Date(d4.getTime() + 50000), null);
		TimeUtilities.insertIntoSortedList(blocks, b2);
		assertTrue(blocks.get(blocks.size() - 1) == b2);
		
		// Insert a block between existing blocks
		final UnavailableBlock b3 = new UnavailableBlock(new Date(d2.getTime()), new Date(d2.getTime() + 3600000));
		TimeUtilities.insertIntoSortedList(blocks, b3);
		assertTrue(blocks.get(2) == b3);
		
		// Insert into an empty list
		blocks = new ArrayList<>();
		TimeUtilities.insertIntoSortedList(blocks, b1);
		assertTrue(blocks.get(0) == b1);
	}
	
	@Test
	public void testIndexOf() {
		List<ITimeBlockable> blocks = new ArrayList<>();
		
		final Date d1 = new Date();
		final Date d2 = new Date(d1.getTime() + 10800000); // 3 hrs later
		final Date d3 = new Date(d2.getTime() + 5400000); // 1.5 hrs later
		final Date d4 = new Date(d3.getTime() + 3600000); // 1 hr later
		
		blocks.add(new AssignmentBlock(d1, d2, null));
		blocks.add(new UnavailableBlock(d3, d4));
		
		// Test item before list
		assertTrue(TimeUtilities.indexOfFitLocn(blocks, new Date(d1.getTime() - 300)) == 0);
		
		// Test item in middle of list
		assertTrue(TimeUtilities.indexOfFitLocn(blocks, new Date(d2.getTime() + 50)) == 1);
		
		// Test item after list
		assertTrue(TimeUtilities.indexOfFitLocn(blocks, new Date(d4.getTime() + 50)) == 2);
		
		// Test empty list
		blocks = new ArrayList<>();
		assertTrue(TimeUtilities.indexOfFitLocn(blocks, new Date()) == 0);
	}
}
