package unit_tests;

import java.util.Calendar;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backend.database.StorageService;
import frontend.view.startup.SurveyView;

/**
 * Unit tests for Settings page
 * 
 * @author miweinst
 *
 */

public class SettingsTest {
	
	// TODO: Delete GUI test runs; maybe the whole class
	@Before
	public void startUp() {
		StorageService.initialize(true);
	}
	@After 
	public void cleanUp() {
		StorageService.cleanup();
	}
	
	@Test
	public void runStartupSurvey() 
	{
		SurveyView surveyDialog = new SurveyView();
		surveyDialog.setVisible(true);
		while(true);
	}
	
	@Test
	public void calendarTest() 
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 1970);
		c.set(Calendar.DAY_OF_YEAR, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		System.out.println("I set(2): " + c.getTime());
		
		Calendar c2 = Calendar.getInstance();
		c2.setTime(new Date(0));
		
		System.out.println("II setTime(Date): " + c2.getTime());
		
		Calendar c3 = Calendar.getInstance();
		c3.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
		
		System.out.println("III set(4-5): " + c3.getTime());
	}
	
/*	@Test
	public void runSettings()
	{
		*//**
		 * Just used for testing GUI for now. 
		 * All this does is open the frame, but allows
		 * SettingsPage to be viewed independently, 
		 * without modifying App or Main.
		 *//*
		JFrame frame = new JFrame("Settings");
		Dimension dim = new Dimension(800, 600);
		frame = makeFrame(dim);
		SettingsView view = new SettingsView();
		view.setPreferredSize(dim);
		frame.add(view);
		
		//Don't let JUnit test finish. Exit on close
		while(true);
	}*/
	
	/* HELPER METHODS */
	
	/**
	 * Returns Frame to run Settings in,
	 * at specified size.
	 */
/*	private JFrame makeFrame(Dimension dim) {
		JFrame frame = new JFrame("Settings");
		frame.setPreferredSize(dim);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
		return frame;
	}*/
}
