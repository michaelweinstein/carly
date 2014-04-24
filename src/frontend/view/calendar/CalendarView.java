package frontend.view.calendar;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import frontend.Utils;

/**
 * Represents the panel holding the line and week views for the calendar
 * 
 * @author dgattey
 */
public class CalendarView extends JPanel {
	
	private final LineView		lineView;
	private final WeekView		weekView;
	
	private static final long	serialVersionUID	= -1015403696639767751L;
	
	public CalendarView() {
		lineView = new LineView();
		weekView = new WeekView();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		Utils.themeComponentAlt(this);
		
		add(makeDays());
		add(lineView);
		add(Box.createVerticalStrut(10));
		add(weekView);
	}
	
	/**
	 * Makes the days label panel
	 * 
	 * @return a new panel with the labels for days
	 */
	private static JPanel makeDays() {
		final JPanel par = new JPanel();
		final JPanel days = new JPanel();
		days.setLayout(new GridLayout(1, 7));
		par.setLayout(new BoxLayout(par, BoxLayout.X_AXIS));
		Utils.themeComponentAlt(days);
		Utils.themeComponentAlt(par);
		Utils.padComponent(par, 0, 10);
		
		final Font ft = new Font(Utils.APP_FONT_NAME, Font.PLAIN, 13);
		par.add(Box.createHorizontalStrut(WeekCanvas.X_OFFSET + 4));
		
		final JLabel s = new JLabel("Sun");
		s.setFont(ft);
		Utils.themeComponent(s);
		days.add(s);
		
		final JLabel m = new JLabel("Mon");
		m.setFont(ft);
		Utils.themeComponent(m);
		days.add(m);
		
		final JLabel t = new JLabel("Tues");
		t.setFont(ft);
		Utils.themeComponent(t);
		days.add(t);
		
		final JLabel w = new JLabel("Wed");
		w.setFont(ft);
		Utils.themeComponent(w);
		days.add(w);
		
		final JLabel tt = new JLabel("Thur");
		tt.setFont(ft);
		Utils.themeComponent(tt);
		days.add(tt);
		
		final JLabel f = new JLabel("Fri");
		f.setFont(ft);
		Utils.themeComponent(f);
		days.add(f);
		
		final JLabel ss = new JLabel("Sat");
		ss.setFont(ft);
		Utils.themeComponent(ss);
		days.add(ss);
		
		par.add(days);
		
		return par;
	}
	
	/**
	 * Does nothing currently
	 */
	public void reloadData() {}
	
}
