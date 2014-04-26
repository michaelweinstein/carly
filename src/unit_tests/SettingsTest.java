package unit_tests;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.junit.Test;

import frontend.view.settings.SettingsView;

/**
 * Unit tests for Settings page
 * 
 * @author miweinst
 *
 */

public class SettingsTest {
	
	@Test
	public void runSettings()
	{
		/**
		 * Just used for testing GUI for now. 
		 * All this does is open the frame, but allows
		 * SettingsPage to be viewed independently, 
		 * without modifying App or Main.
		 */
		JFrame frame = new JFrame("Settings");
		Dimension dim = new Dimension(800, 600);
		frame = makeFrame(dim);
		SettingsView view = new SettingsView();
		view.setPreferredSize(dim);
		frame.add(view);
		
		//Don't let JUnit test finish. Exit on close
		while(true);
	}
	
	/* HELPER METHODS */
	
	/**
	 * Returns Frame to run Settings in,
	 * at specified size.
	 */
	private JFrame makeFrame(Dimension dim) {
		JFrame frame = new JFrame("Settings");
		frame.setPreferredSize(dim);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
		return frame;
	}
}
