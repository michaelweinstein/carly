package frontend.view;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import frontend.Utilities;

/**
 * This is the front-end for the pop-up dialogue box
 * to create or choose a Template when creating a new 
 * assignment. 
 * 
 * @author miweinst
 *
 */

//TODO: Might want to extend Circle2D or another Shape for UI that is not rectangular JPanel
public class TemplateWizardDialogueView extends JPanel {
	
	private static final long serialVersionUID = 1966342453022136202L;
	
	//TODO: Might want to keep public size in Utilities if we have multiple kinds of dialogue boxes
	//Size of Dialogue Box
	private static final Dimension dim = new Dimension(275, 200);

	public TemplateWizardDialogueView() {
		super();
		// Set theme of dialogue box
		Utilities.themeComponentInverse(this);
		Utilities.addBorderFull(this);
		
		// Size of dialogue
		this.setPreferredSize(dim);
		
		// Template Wizard label
		JLabel title = new JLabel("Template Wizard");
		title.setFont(new Font(Utilities.APP_FONT_NAME, Font.BOLD, 15));
		Utilities.themeComponent(title);
		
		//Add label and spacing
		this.add(Box.createVerticalStrut(20));
		this.add(title);
		this.add(Box.createVerticalStrut(15));
	
		
		//TODO: Guts of wizard
	}

}
