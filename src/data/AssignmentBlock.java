package data;

import java.util.Date;
import java.util.Objects;

public class AssignmentBlock implements ITimeBlockable {
	
	private final String	m_uniqueId;
	private Date			m_start;
	private Date			m_end;
	private ITask			m_task;
	private final boolean	m_isMovable;
	
	public AssignmentBlock(final Date start, final Date end, final ITask task) {
		m_uniqueId = DataUtil.generateID() + start.getTime() / 100000;
		m_start = start;
		m_end = end;
		m_task = task;
		m_isMovable = true;
	}
	
	/**
	 * Constructor used by StorageService to reconstruct this object
	 * 
	 * @param id Unique identifier for this block
	 * @param start Starting date of block
	 * @param end Ending date of block
	 * @param task Task associated with this block
	 * @param movable Whether this block is movable
	 */
	public AssignmentBlock(final String id, final Date start, final Date end, final ITask task, final boolean movable) {
		m_uniqueId = id;
		m_start = start;
		m_end = end;
		m_task = task;
		m_isMovable = movable;
	}
	
	@Override
	public String getId() {
		return m_uniqueId;
	}
	
	@Override
	public void setStart(final Date d) {
		m_start = d;
	}
	
	@Override
	public void setEnd(final Date d) {
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
	public void setTask(final ITask t) {
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
	public int compareTo(final ITimeBlockable o) {
		// Thought process behind comparing starts: the start time of an assignment
		// is always *now*, which is for all intensive purposes a unique time.
		// Therefore, no two time blocks should have the same start time.
		return m_start.compareTo(o.getStart());
	}
	
	@Override
	public String toString() {
		return "Assigned: [" + m_start.toString() + ", " + m_end.toString() + "]";
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof AssignmentBlock)) {
			return false;
		}
		
		final AssignmentBlock ab = (AssignmentBlock) o;
		return m_start.equals(ab.m_start) && m_end.equals(ab.m_end) && m_uniqueId.equals(ab.m_uniqueId);
	}
	
	public String fullString() {
		return "Assigned: [" + m_uniqueId + ", " + m_start.toString() + ", " + m_end.toString() + ", "
			+ m_task.toString() + ", " + m_isMovable + "]";
	}
	
	@Override
	public long getLength() {
		return m_end.getTime() - m_start.getTime();
	}
	
	/**
	 * Needed!
	 */
	@Override
	public int hashCode() {
		return Objects.hash(m_start, m_end, m_task, m_uniqueId);
	}
}
