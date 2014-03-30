package frontend.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

/**
 * Represents all current tasks and their collective data
 * 
 * @author dgattey
 */
public class TaskView extends JPanel {
	
	private static final long	serialVersionUID	= -3581722774976194311L;
	
	public TaskView() {
		setForeground(Color.CYAN);
		setBackground(Color.CYAN);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200, 300);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(100, 200);
	}
}
