package frontend.app;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import frontend.Utils;
import frontend.view.CButton;

/**
 * Class to use when showing an error to the user
 * 
 * @author dgattey
 */
public class ErrorDialog extends JDialog {
	
	private static final long	serialVersionUID	= 1L;
	private final JTextArea		_text;
	private final JButton		_button;
	
	/**
	 * Constructor sets up defaults and look and feel
	 */
	public ErrorDialog() {
		super();
		setAlwaysOnTop(true);
		
		// Create main pane
		final JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		main.setAlignmentX(LEFT_ALIGNMENT);
		Utils.themeComponent(main);
		Utils.padComponent(main, 15, 15);
		
		// Text and title
		final JLabel title = new JLabel("Error");
		_text = new JTextArea();
		_text.setAlignmentX(LEFT_ALIGNMENT);
		title.setAlignmentX(LEFT_ALIGNMENT);
		_text.setFont(Utils.getFont(Font.PLAIN, 14));
		_text.setEditable(false);
		_text.setFocusable(false);
		_text.setWrapStyleWord(true);
		_text.setLineWrap(true);
		Utils.themeComponent(title);
		Utils.setFont(title, 20);
		Utils.padComponent(title, 0, 0, 20, 0);
		Utils.addBorderBottom(title);
		Utils.padComponentWithBorder(title, 0, 0, 10, 0);
		Utils.themeComponent(_text);
		
		// Create button
		_button = new CButton("Close");
		_button.setAlignmentX(RIGHT_ALIGNMENT);
		_button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
			}
		});
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(_button);
		buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
		Utils.themeComponent(buttonPanel);
		
		// Add all to dialog
		main.add(title);
		main.add(Box.createVerticalStrut(10));
		main.add(_text);
		main.add(buttonPanel);
		setContentPane(main);
		setMinimumSize(new Dimension(350, 200));
		pack();
	}
	
	/**
	 * Updates the text to use in showing
	 * 
	 * @param newText the new text to use
	 */
	public void setText(final String newText) {
		_text.setText(newText);
	}
	
	/**
	 * Updates the button text
	 * 
	 * @param buttonText the new text
	 */
	public void setButtonText(final String buttonText) {
		_button.setText(buttonText);
	}
}
