package backend;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class Utilities {
	/*
	 * DB configuration parameters
	 * TODO: factor out into external configuration file
	 */
	
	protected static final String DB_URL = "jdbc:h2:~/test";
	protected static final String DB_USER = "sa"; 
	protected static final String DB_PWD = ""; 
	protected static final String DB_NAME = "test"; 
	
	/*
	 * Assignment SQL statements
	 */
	
	protected static final String INSERT_ASGN = 
			"INSERT INTO ASSIGNMENT " +
	        "(ASGN_ID, ASGN_NAME, ASGN_EXPECTED_HOURS, ASGN_DATE, ASGN_TEMPLATE_ID) " + 
	        "VALUES (?, ?, ?, ?) ";
	protected static final String DELETE_ASGN = 
			"DELETE FROM ASSIGNMENT " +
	        "WHERE ASGN_ID = ? ";
	protected static final String UPDATE_ASGN = 
			"UPDATE ASSIGNMENT " +
			"SET ASGN_NAME = ?, ASGN_EXPECTED_HOURS = ?, ASGN_DATE = ?, ASGN_TEMPLATE_ID = ? " + 
	        "WHERE ASGN_ID = ? ";
	protected static final String SELECT_ASGN_TASKS_BY_DATE = 
			"SELECT * FROM ASSIGNMENT " +
			"INNER JOIN TASK " +
			"ON TASK.ASGN_ID = ASSIGNMENT.ASGN_ID " +
			"ORDER BY ASSIGNMENT.ASGN_ID " +
	        "WHERE ASSIGNMENT.ASGN_DATE BETWEEN ? AND ? ";
	
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
			"ORDER BY ASSIGNMENT.ASGN_DATE " +
		    "WHERE ASSIGNMENT.ASGN_DATE BETWEEN ? AND ? ";

	/*
	 * Template SQL statements
	 */
	
	protected static final String INSERT_TEMPLATE = 
			"INSERT INTO TEMPLATE " +
			"(TEMPLATE_ID, TEMPLATE_NAME, TEMPLATE_CONSECUTIVE_HOURS) " + 
			"VALUES (?, ?, ?) ";
	
	protected static final String INSERT_TEMPLATE_STEP =  
			"INSERT INTO TEMPLATE_STEP " +
			"(TEMPLATE_ID, STEP_NAME, STEP_PERCENT_TOTAL, STEP_STEP_NUMBER, _STEP_NUM_DAYS, " +
			"STEP_HOURS_PER_DAY, STEP_TIME_OF_DAY) " + 
			"VALUES (?, ?, ?, ?, ?, ?, ?) ";
	
	protected static final String UPDATE_TEMPLATE = 
			"UPDATE TEMPLATE " +
			"SET TEMPLATE_NAME = ?, TEMPLATE_CONSECUTIVE_HOURS = ? " + 
			"WHERE TEMPLATE_ID = ? "; 
	
	protected static final String MERGE_TEMPLATE_STEP = 
			"MERGE INTO TEMPLATE_STEP " +
			"(TEMPLATE_ID, STEP_NAME, STEP_PERCENT_TOTAL, STEP_STEP_NUMBER, _STEP_NUM_DAYS, " +
			"STEP_HOURS_PER_DAY, STEP_TIME_OF_DAY) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?) ";  
	
	protected static final String SELECT_TEMPLATE_AND_STEPS_BY_ID = 
			"SELECT * FROM TEMPLATE " +
			"INNER JOIN TEMPLATE_STEP " +
			"ON TEMPLATE.TEMPLATE_ID = TEMPLATE_STEP.TEMPLATE_ID " +
			"ORDER BY TEMPLATE.TEMPLATE_ID " +
	        "WHERE TEMPLATE.TEMPLATE_ID = ? ";
	
	protected static final String SELECT_ALL_TEMPLATE_AND_STEPS = 
			"SELECT * FROM TEMPLATE " +
			"INNER JOIN TEMPLATE_STEP " +
			"ON TEMPLATE.TEMPLATE_ID = TEMPLATE_STEP.TEMPLATE_ID " +
			"ORDER BY TEMPLATE.TEMPLATE_ID ";
	
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
		StringBuilder result = new StringBuilder("CREATE TABLE " + tableName + " (");

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
