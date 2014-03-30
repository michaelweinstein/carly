package frontend.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import frontend.Utilities;
import frontend.app.GUIApp;

/**
 * Class that deals with drawing information to screen
 * 
 * @author dgattey
 */
public class ViewController {
	
	private final GUIApp		app;
	
	// Constants
	private static final Color	BGCOLOR	= Color.black;
	private static final Color	FGCOLOR	= Color.white;
	static final String			FONT	= "Arial";
	
	// View stuff
	private JFrame				window;
	
	/**
	 * Constructor with an app for use later
	 * 
	 * @param app the parent app
	 */
	public ViewController(final GUIApp app) {
		this.app = app;
	}
	
	/**
	 * Constructor makes a frame, canvas to go on top of it, sets up mouse listeners, and adds components
	 */
	public void create() {
		window = new JFrame(Utilities.APPNAME);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setMinimumSize(new Dimension(400, 700));
		window.setResizable(false);
		
		// Show it
		window.pack();
		window.setVisible(true);
	}
	
	/**
	 * Sets background and foreground color of this panel
	 * 
	 * @param panel the panel to apply it to
	 */
	private static void theme(final JComponent panel) {
		panel.setForeground(FGCOLOR);
		panel.setBackground(BGCOLOR);
	}
	
	/**
	 * Sets the padding of this panel to be x on either side and y on top and bottom
	 * 
	 * @param panel the panel
	 * @param x the horizontal padding
	 * @param y the vertical padding
	 */
	private static void setPadding(final JComponent panel, final int x, final int y) {
		panel.setBorder(new EmptyBorder(y, x, y, x));
	}
}