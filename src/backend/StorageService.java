package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.IAssignment;
import data.ITask;
import data.ITemplate;

/**
 * Handles the storage, retrieval and persistence of data for Carly
 * 
 * @author eb27
 */
public class StorageService {
	
	private static Cache<IAssignment> _assignments;
	private static Cache<ITask> _tasks; 
	private static Cache<ITemplate> _templates; 
	
	/**
	 * Called when the user opens app for the first time 
	 */
	public static synchronized void setUp() {
		//Create tables in the database
		try (Connection con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD)) {
			Class.forName("org.h2.Driver");
			
			System.out.println("Connected to db!");
	        ArrayList<String> queries = new ArrayList<>(); 
	        
	        //Assignment table
	        ArrayList<String> assignmentCols = new ArrayList<>();
	        assignmentCols.add(concatColumn("ASGN_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
	        assignmentCols.add(concatColumn("ASGN_NAME", "VARCHAR(255)"));
	        assignmentCols.add(concatColumn("ASGN_EXPECTED_HOURS", "INT"));
//	        assignmentCols.add(concatColumn("ASGN_TASK_IDS", "ARRAY"));
	        assignmentCols.add(concatColumn("ASGN_TEMPLATE_ID", "VARCHAR(255)"));
	        queries.add(Utilities.buildCreateString("ASSIGNMENT", assignmentCols)); 
			
//	        //Tasks table
//	        ArrayList<String> taskCols = new ArrayList<>();
//	        taskCols.add(concatColumn("ASGN_ID", "VARCHAR(255) NOT NULL FOREIGN KEY REFERENCES ASSIGNMENT (ASGN_ID)"));
//	        taskCols.add(concatColumn("TASK_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
//	        taskCols.add(concatColumn("TASK_NAME", "VARCHAR(255)"));
//	        taskCols.add(concatColumn("TASK_PERCENT_TOTAL", "DOUBLE"));
//	        taskCols.add(concatColumn("TASK_PERCENT_COMPLETE", "DOUBLE"));
//	        taskCols.add(concatColumn("TASK_TIME_OF_DAY", "VARCHAR(255)")); //TODO: define relational solution for enum
//	        taskCols.add(concatColumn("TASK_SUGGESTED_LENGTH", "DOUBLE"));
//	        queries.add(Utilities.buildCreateString("TASK", taskCols)); 
//	        
//	        //Template table
//	        ArrayList<String> templateCols = new ArrayList<>();
//	        templateCols.add(concatColumn("TEMPLATE_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
//	        templateCols.add(concatColumn("TEMPLATE_NAME", "VARCHAR(255)"));
////	        templateCols.add(concatColumn("TEMPLATE_STEP_IDS", "ARRAY"));
//	        queries.add(Utilities.buildCreateString("TEMPLATE", templateCols));
//	        
//	        //Template step table
//	        ArrayList<String> stepCols = new ArrayList<>();
//	        stepCols.add(concatColumn("TEMPLATE_ID", "VARCHAR(255) NOT NULL FOREIGN KEY REFERENCES TEMPLATE (TEMPLATE_ID)"));
//	        stepCols.add(concatColumn("STEP_NAME", "VARCHAR(255)"));  
//	        stepCols.add(concatColumn("STEP_TIME_OF_DAY", "VARCHAR(255)")); //TODO: define relational solution for enum
//	        stepCols.add(concatColumn("STEP_HOUR_LENGTH", "DOUBLE"));
//	        stepCols.add(concatColumn("PRIMARY KEY", "(TEMPLATE_ID, STEP_NAME)")); //TODO: can you make the foreign key also the primary key?
//	        queries.add(Utilities.buildCreateString("TEMPLATE_STEP", stepCols));
//
//		    try (Statement stmt = con.createStatement()) {
//		    	for (String query: queries) {
//		    		stmt.addBatch(query);
//		    	}
//		    	stmt.executeBatch(); 
//		    } 
	        
	        String query = "SHOW COLUMNS FROM ASSIGNMENT"; 

		    try (Statement stmt = con.createStatement()) {
	
		        ResultSet rs = stmt.executeQuery(query);
	
		        System.out.println("Processing results."); 
		        System.out.println(rs.toString()); 
		    }
		} 
		catch (ClassNotFoundException e) {
			Utilities.printException("db drive class not found", e); 
		} catch (SQLException e) {
			Utilities.printSQLException("could not create all tables", e); 
		}
	}
	
	/** 
	 * Called at each application startup
	 */
	public static synchronized void initialize() {
		_assignments = new Cache<>();
		_tasks = new Cache<>();
		_templates = new Cache<>();
	}
	
	/*
	 * CRUD and dynamic queries for Assignments 
	 */

	public static synchronized IAssignment addAssignment(IAssignment assign) {
		return null; 
	}
	public static synchronized int addAllAssignments(List<IAssignment> assign) {
		PreparedStatement updateSales = null;
	    PreparedStatement updateTotal = null;
	    Connection con = null; 
	    int numSuccess = 0; 

	    String updateStatement =
	        "INSERT INTO ASSIGNMENT" +
	        "set TOTAL = TOTAL + ? " +
	        "where COF_NAME = ?";

	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        con.setAutoCommit(false);
	        updateTotal = con.prepareStatement(updateStatement);

//	        for (Map.Entry<String, Integer> e : salesForWeek.entrySet()) {
//	            updateSales.setInt(1, e.getValue().intValue());
//	            updateSales.setString(2, e.getKey());
//	            updateSales.executeUpdate();
//	            updateTotal.setInt(1, e.getValue().intValue());
//	            updateTotal.setString(2, e.getKey());
//	            updateTotal.executeUpdate();
//	            con.commit();
//	        }
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } catch(SQLException x) {
	                Utilities.printSQLException("could not roll back transaction", x);
	            }
	        }
	    } 
	    finally {
	    	try {
	    		if (updateSales != null) {
		            updateSales.close();
		        }
		        if (updateTotal != null) {
		            updateTotal.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
		
		return numSuccess; 
	}
	public static synchronized IAssignment removeAssignment(IAssignment assign) {
		return null; 
	}
	public static synchronized IAssignment updateAssignment(IAssignment assign) {
		return null; 
	}
	public static synchronized List<IAssignment> getAllAssignmentsWithinRange(Date date1, Date date2) {
		return new ArrayList<IAssignment>(0); 
	}

	public static synchronized List<ITask> getAllTasksWithinRange(Date date1, Date date2) {
		return new ArrayList<ITask>(0); 
	}
	
	/*
	 * Store and retrieve TimeBlocks
	 */

//	public static synchronized List<UnavailableBlock> getAllUnavailableBlocksWithinRange(Date date1, Date date2);
//	public static synchronized List<AssignmentBlock> getAllAssignmentBlocksWithinRange(Date date1, Date date2);

//	public static synchronized void addTimeBlock(ITimeBlockable block);
//	public static synchronized void addAllTimeBlocks(List<ITimeBlockable> block);

	/*
	 * Storage and retrieval of Templates used to lay out Assignments 
	 */
	
	public static synchronized ITemplate getTemplate(String id) {
		return null; 
	}
	
	public static synchronized void addTemplate(ITemplate temp) {
		
	}
	
	public static synchronized List<ITemplate> getAllTemplates() {
		return new ArrayList<ITemplate>(0); 
	}
	
	/*
	 * Storage and retrieval of settings
	 */

	public static synchronized void addSetting(String name, String val) {
		
	}
	
	public static synchronized String getSetting(String name) {
		return ""; 
	}
	
	public static synchronized Map<String, String> getAllSettings() {
		return new HashMap<String, String>(0); 
	}
	
	/*
	 * Helper methods
	 */
	
	private static String concatColumn(String columnName, String dataType) {
		return columnName + " " + dataType; 
	}
}
