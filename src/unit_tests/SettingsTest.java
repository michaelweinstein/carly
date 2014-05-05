package unit_tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backend.database.StorageService;
import backend.database.StorageServiceException;
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
		try {
			StorageService.initialize(true);
		} catch (StorageServiceException e) {
			System.err.println("SettingsTest: startUp: Failed to initialize StorageService");
		}
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
