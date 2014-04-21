package data;

import java.util.Date;


public class UnavailableBlock implements ITimeBlockable {

	//TODO: Is the "m_isMovable" boolean trivial?  i.e. the AssignmentBlock class should
	//always support movable blocks, and the UnavailableBlock class should never.
	//TODO: Proposed solution - continue to store the member variable, but do not need
	//the extra parameter to the constructor
	
	private Date m_start;
	private Date m_end;
	private ITask m_task;
	private boolean m_isMovable;
	
	public UnavailableBlock(Date start, Date end, ITask task, boolean movable) {
		this.m_start = start;
		this.m_end = end;
		this.m_task = task;
		this.m_isMovable = movable;
	}
	
	@Override
	public void setStart(Date d) {
		m_start = d;
	}

	@Override
	public void setEnd(Date d) {
		m_end = d;
	}
	
	@Override
	public Date getStart() {
		return m_start;
	}


	@Override
	public Date getEnd() {
		return m_end;
	}

	@Override
	public void setTask(ITask t) {
		m_task = t;
	}

	@Override
	public ITask getTask() {
		return m_task;
	}

	@Override
	public String getTaskId() {
		return m_task.getTaskID();
	}

	@Override
	public boolean isMovable() {
		return m_isMovable;
	}

	@Override
	public int compareTo(ITimeBlockable o) {
		//Thought process behind comparing starts: the start time of an assignment
		//is always *now*, which is for all intensive purposes a unique time.
		//Therefore, no two time blocks should have the same start time.
		return m_start.compareTo(o.getStart());
	}
	
	@Override
	public String toString() {
		return "Unavailable: [" + m_start.toString() + ", " + m_end.toString() + "]";
	}

	@Override
	public long getLength() {
		return m_end.getTime() - m_start.getTime();
	}
	
}
