package backend.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class Utilities {
	
	/*
	 * Misc SQL statements
	 */
	
	protected static final String DROP_ALL_TABLES = 
			"DROP TABLE IF EXISTS ASSIGNMENT, TASK, TEMPLATE, TEMPLATE_STEP, TIME_BLOCK, SETTING"; 
	
	/*
	 * ITimeBlockable SQL insertion statements 
	 */
	
	protected static final String INSERT_TIME_BLOCK = 
			"INSERT INTO TIME_BLOCK " + 
			"(BLOCK_ID, TASK_ID, BLOCK_START, BLOCK_END, BLOCK_MOVABLE, BLOCK_DEFAULT) " + 
			"VALUES (?, ?, ?, ?, ?, ?) ";
	
	protected static final String MERGE_TIME_BLOCK = 
			"MERGE INTO TIME_BLOCK " + 
			"(BLOCK_ID, TASK_ID, BLOCK_START, BLOCK_END, BLOCK_MOVABLE, BLOCK_DEFAULT) " + 
			"VALUES (?, ?, ?, ?, ?, ?) ";
	
	protected static final String UPDATE_TIME_BLOCK = 
			"UPDATE TIME_BLOCK " +
			"SET BLOCK_START = ?, BLOCK_END = ?, TASK_ID = ? " + 
			"WHERE BLOCK_ID = ? "; 
			
	protected static final String DELETE_TIME_BLOCK = 
			"DELETE FROM TIME_BLOCK " + 
			"WHERE BLOCK_ID = ? "; 
	
	/*
	 * AssignmentBlock and UnavailableBlock SQL retrieval statements
	 */
	
	protected static final String SELECT_ASSIGNMENT_BLOCK_BY_ID = 
			"SELECT * FROM TIME_BLOCK " + 
			"INNER JOIN TASK " + 
			"ON TASK.TASK_ID = TIME_BLOCK.TASK_ID " +
			"WHERE BLOCK_ID = ? "; 
	
	protected static final String SELECT_UNAVAILABLE_BLOCK_BY_ID = 
			"SELECT * FROM TIME_BLOCK " + 
			"WHERE BLOCK_ID = ? "; 
	
	protected static final String SELECT_ASSIGNMENT_BLOCKS_BY_DATE = 
			"SELECT * FROM TIME_BLOCK " + 
			"INNER JOIN TASK " + 
			"ON TASK.TASK_ID = TIME_BLOCK.TASK_ID " +
			"WHERE ((BLOCK_END BETWEEN ? AND ?) OR " + 	//block ends in the range, OR 
			"(BLOCK_START BETWEEN ? AND ?) OR " +		//block start in the range, OR
			"(BLOCK_START <= ? AND BLOCK_END >= ?)) " +	//block contains the range
			"AND BLOCK_MOVABLE = TRUE " +
			"ORDER BY TIME_BLOCK.BLOCK_START";
	
	protected static final String SELECT_UNAVAILABLE_BLOCKS_BY_DATE = 
			"SELECT * FROM TIME_BLOCK " + 
			"WHERE ((BLOCK_END BETWEEN ? AND ?) OR " + 	//block ends in the range, OR 
			"(BLOCK_START BETWEEN ? AND ?) OR " +		//block start in the range, OR
			"(BLOCK_START <= ? AND BLOCK_END >= ?)) " +	//block contains the range
			"AND BLOCK_MOVABLE = FALSE " +
			"ORDER BY TIME_BLOCK.BLOCK_START";
	
	/*
	 * Assignment SQL statements
	 */
	
	protected static final String INSERT_ASGN = 
			"INSERT INTO ASSIGNMENT " +
	        "(ASGN_ID, ASGN_NAME, ASGN_EXPECTED_HOURS, ASGN_DATE, ASGN_TEMPLATE_ID) " + 
	        "VALUES (?, ?, ?, ?, ?) ";
	
	protected static final String DELETE_ASGN = 
			"DELETE FROM ASSIGNMENT " +
	        "WHERE ASGN_ID = ? ";
	
	protected static final String UPDATE_ASGN = 
			"UPDATE ASSIGNMENT " +
			"SET ASGN_NAME = ?, ASGN_EXPECTED_HOURS = ?, ASGN_DATE = ?, ASGN_TEMPLATE_ID = ? " + 
	        "WHERE ASGN_ID = ? ";
	
	protected static final String SELECT_ALL_ASGNS = 
			"SELECT * FROM ASSIGNMENT " +
			"INNER JOIN TASK " +
			"ON TASK.ASGN_ID = ASSIGNMENT.ASGN_ID " +
	        "ORDER BY ASSIGNMENT.ASGN_DATE "; 
	
	protected static final String SELECT_ASGNS_TASKS_BY_DATE = 
			"SELECT * FROM ASSIGNMENT " +
			"INNER JOIN TASK " +
			"ON TASK.ASGN_ID = ASSIGNMENT.ASGN_ID " +
	        "WHERE ASSIGNMENT.ASGN_DATE BETWEEN ? AND ? " + 
	        "ORDER BY ASSIGNMENT.ASGN_ID "; 
	
	protected static final String SELECT_ASGN_BY_ID = 
			"SELECT * FROM ASSIGNMENT " +
			"INNER JOIN TASK " +
			"ON TASK.ASGN_ID = ASSIGNMENT.ASGN_ID " +
			"WHERE ASSIGNMENT.ASGN_ID = ? " + 
			"ORDER BY ASSIGNMENT.ASGN_ID "; 
	
	/*
	 * Task SQL statements
	 */
	
	protected static final String INSERT_TASK = 
			"INSERT INTO TASK " +
	        "(ASGN_ID, TASK_ID, TASK_NAME, TASK_PERCENT_TOTAL, TASK_PERCENT_COMPLETE, " +
	        "TASK_TIME_OF_DAY, TASK_SUGGESTED_LENGTH) " + 
	        "VALUES (?, ?, ?, ?, ?, ?, ?) ";
	
	protected static final String SELECT_TASKS_BY_DATE = 
			"SELECT TASK.* FROM TASK " +
			"INNER JOIN ASSIGNMENT " +
			"ON TASK.ASGN_ID = ASSIGNMENT.ASGN_ID " +
			"WHERE ASSIGNMENT.ASGN_DATE BETWEEN ? AND ? " + 
			"ORDER BY ASSIGNMENT.ASGN_DATE "; 
	
	protected static final String SELECT_TASK_BY_ID = 
			"SELECT * FROM TASK " +
			"WHERE TASK_ID = ? "; 

	protected static final String DELETE_TASKS_BY_ID = 
			"DELETE FROM TASK " +
			"WHERE ASGN_ID = ? "; 
	
	/*
	 * Template SQL statements
	 */
	
	protected static final String INSERT_TEMPLATE = 
			"INSERT INTO TEMPLATE " +
			"(TEMPLATE_ID, TEMPLATE_NAME, TEMPLATE_CONSECUTIVE_HOURS, TEMPLATE_NUM_CONSECUTIVE) " + 
			"VALUES (?, ?, ?, ?) ";

	protected static final String UPDATE_TEMPLATE = 
			"UPDATE TEMPLATE " +
			"SET TEMPLATE_NAME = ?, TEMPLATE_CONSECUTIVE_HOURS = ? " + 
			"WHERE TEMPLATE_ID = ? ";
	
	protected static final String DELETE_TEMPLATE = 
			"DELETE FROM TEMPLATE " +
	        "WHERE TEMPLATE_ID = ? ";
	
	protected static final String SELECT_TEMPLATE_BY_ID = 
			"SELECT * FROM TEMPLATE " +
			"WHERE TEMPLATE_ID = ? "; 
	
	protected static final String SELECT_TEMPLATES_AND_STEPS_BY_ID = 
			"SELECT * FROM TEMPLATE " +
			"INNER JOIN TEMPLATE_STEP " +
			"ON TEMPLATE.TEMPLATE_ID = TEMPLATE_STEP.TEMPLATE_ID " +
			"WHERE TEMPLATE.TEMPLATE_ID = ? " +
			"ORDER BY TEMPLATE.TEMPLATE_ID ";
	
	protected static final String SELECT_TEMPLATES_AND_STEPS_BY_NAME = 
			"SELECT * FROM TEMPLATE " +
			"INNER JOIN TEMPLATE_STEP " +
			"ON TEMPLATE.TEMPLATE_ID = TEMPLATE_STEP.TEMPLATE_ID " +
			"WHERE TEMPLATE.TEMPLATE_NAME = ? " +
			"ORDER BY TEMPLATE.TEMPLATE_ID ";
	
	protected static final String SELECT_ALL_TEMPLATES_AND_STEPS = 
			"SELECT * FROM TEMPLATE " +
			"INNER JOIN TEMPLATE_STEP " +
			"ON TEMPLATE.TEMPLATE_ID = TEMPLATE_STEP.TEMPLATE_ID " +
			"ORDER BY TEMPLATE.TEMPLATE_ID ";
	
	/*
	 * TemplateStep SQL statements
	 */
	
	protected static final String INSERT_TEMPLATE_STEP =  
			"INSERT INTO TEMPLATE_STEP " +
			"(TEMPLATE_ID, STEP_NAME, STEP_PERCENT_TOTAL, STEP_STEP_NUMBER, STEP_TIME_OF_DAY, STEP_TOD_COUNTERS) " + 
			"VALUES (?, ?, ?, ?, ?, ?) ";
	
	protected static final String MERGE_TEMPLATE_STEP = 
			"MERGE INTO TEMPLATE_STEP " +
			"(TEMPLATE_ID, STEP_NAME, STEP_PERCENT_TOTAL, STEP_STEP_NUMBER, STEP_TIME_OF_DAY) " +
			"VALUES (?, ?, ?, ?, ?) ";
	
	protected static final String SELECT_TEMPLATE_STEP_TOD_COUNTERS_BY_TEMPLATE_ID =
			"SELECT STEP_NAME, STEP_TOD_COUNTERS FROM TEMPLATE_STEP " +
			"WHERE TEMPLATE_ID = ? ";
	
	protected static final String DELETE_TEMPLATE_STEPS_BY_ID = 
			"DELETE FROM TEMPLATE_STEP " +
			"WHERE TEMPLATE_ID = ? "; 
	
	/*
	 * ITimeBlockable SQL insertion statements 
	 */
	
	protected static final String MERGE_SETTING =  
			"MERGE INTO SETTING " +
			"(SETTING_NAME, SETTING_VALUE) " + 
			"VALUES (?, ?) ";
	
	protected static final String SELECT_SETTING_BY_NAME = 
			"SELECT * FROM SETTING " +
			"WHERE SETTING_NAME = ? "; 
	
	protected static final String SELECT_ALL_SETTINGS = 
			"SELECT * FROM SETTING "; 
	
	/*
	 * Error handling helper methods
	 */
	
	public static void printSQLException(String msg, SQLException ex) {
	    for (Throwable e : ex) {
	        if (e instanceof SQLException) {
                System.err.println("ERROR: " + msg + ": " + e.getMessage());
	        }
	    }
	}
	
	public static void printException(String msg, Throwable e) {
		System.err.println("ERROR: " + msg + ": " + e.getMessage()); 
	}
	
	/*
	 * SQL query builder methods
	 */
	
	public static String buildCreateString(String tableName, List<String> columns) {
		StringBuilder result = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

		for (String column : columns) {
			result.append(column + ", "); 
		}
		result.replace(result.length() - 1, result.length(), ")"); 
		
		return result.toString(); 
	}
	
	public static void setValues(PreparedStatement preparedStatement, Object... values) throws SQLException {
		for (int i = 0; i < values.length; i++) {
	        preparedStatement.setObject(i + 1, values[i]);
	    }
	}
}
