package frontend.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import frontend.FrontUtilities;

/**
 * Represents the toolbar at top of the screen
 * 
 * @author dgattey
 */
public class ToolbarView extends JPanel {
	
	private static final long	serialVersionUID	= -2158045975284361590L;
	
	public ToolbarView() {
		FrontUtilities.themeComponent(this);
		FrontUtilities.addBorderBottom(this);
		FrontUtilities.padComponentWithBorder(this, 0, 20);
		
		// Assignment adding
		final AddAssignmentDialog dialog = new AddAssignmentDialog();
		final JButton addAssignmentButton = new JButton("New Assignment");
		addAssignmentButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.pack();
				dialog.setVisible(true);
			}
		});
		
		// Title
		final JLabel title = new JLabel(FrontUtilities.APP_NAME);
		title.setFont(new Font(FrontUtilities.APP_FONT_NAME, Font.BOLD, 24));
		FrontUtilities.themeComponent(title);
		
		// Addition of all things
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(Box.createHorizontalStrut(20));
		add(title);
		add(Box.createGlue());
		add(addAssignmentButton);
		add(Box.createHorizontalStrut(20));
	}
}
