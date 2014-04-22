package backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import data.Assignment;
import data.AssignmentBlock;
import data.IAssignment;
import data.ITask;
import data.ITemplate;
import data.ITemplateStep;
import data.ITimeBlockable;
import data.Task;
import data.Template;
import data.TemplateStep;
import data.TimeOfDay;
import data.UnavailableBlock;

/**
 * Handles the storage, retrieval and persistence of data for Carly
 * 
 * @author eb27
 */
public class StorageService {
	
	private static Cache<Template> _templates; 
	
	/**
	 * Called each time application starts up 
	 */
	public static synchronized void initialize(boolean dropTables) {
		_templates = new Cache<>();
		//Create tables in the database
		try (Connection con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD)) {
			Class.forName("org.h2.Driver");
			
			if (dropTables) {
				System.out.println("StorageService: initialize: dropping tables...");
			    try (Statement stmt = con.createStatement()) {
			        stmt.execute(Utilities.DROP_ALL_TABLES);
			    }
			}
			
	        ArrayList<String> queries = new ArrayList<>(); 
	        AssignmentTaskStorage.buildTable(queries); 
	        TemplateStepStorage.buildTable(queries); 
	        TimeBlockStorage.buildTable(queries); 
	        SettingStorage.buildTable(queries); 

		    try (Statement stmt = con.createStatement()) {
		    	for (String query: queries) {
		    		stmt.addBatch(query);
		    	}
		    	stmt.executeBatch(); 
		    } 
	        
		    //DEBUG
//	        String query = "SHOW TABLES"; 
//		    try (Statement stmt = con.createStatement()) {
//		        ResultSet rs = stmt.executeQuery(query);
//		        
//		        ResultSetMetaData rsmd = rs.getMetaData();
//		        int columnCount = rsmd.getColumnCount();
//		        System.out.println("Column names are: ");
//		        // The column count starts from 1
//		        for (int i = 1; i < columnCount + 1; i++ ) {
//		          String name = rsmd.getColumnName(i);
//		          System.out.println(name);
//		        }
//	
//		        System.out.println("Processing results.");
//		        while (rs.next()) {
//		        	System.out.println(rs.getString("TABLE_NAME"));
//		        }
//		    }
		    //DEBUG
		} 
		catch (ClassNotFoundException e) {
			Utilities.printException("db drive class not found", e); 
		} catch (SQLException e) {
			Utilities.printSQLException("could not create all tables", e); 
		}
	}
	
	/*
	 * ================================================================
	 * 
	 * CRUD and dynamic queries for Assignments & Tasks
	 *  
	 * ================================================================
	 */

	public static synchronized IAssignment addAssignment(IAssignment assignment) throws StorageServiceException {
		return AssignmentTaskStorage.addAssignment(assignment); 
	}
	
	public static synchronized IAssignment removeAssignment(IAssignment assignment) {
		return AssignmentTaskStorage.removeAssignment(assignment); 
	}
	
	public static synchronized Assignment updateAssignment(Assignment assignment) {
	    return AssignmentTaskStorage.updateAssignment(assignment); 
	}
	
	public static synchronized Assignment getAssignmentById(String toBeFoundId) {
		return AssignmentTaskStorage.getAssignmentById(toBeFoundId, _templates); 
	}
	
	public static synchronized List<Assignment> getAllAssignmentsWithinRange(Date date1, Date date2) {
		return AssignmentTaskStorage.getAllAssignmentsWithinRange(date1, date2, _templates); 
	}
	
	public static synchronized List<ITask> getAllTasksWithinRange(Date date1, Date date2) {
		return AssignmentTaskStorage.getAllTasksWithinRange(date1, date2); 
	}
	
	/*
	 * ================================================================
	 * 
	 * CRUD and dynamic queries for Time Blocks 
	 *  
	 * ================================================================
	 */

	public static synchronized List<UnavailableBlock> getAllUnavailableBlocksWithinRange(Date date1, Date date2) {
		return TimeBlockStorage.getAllUnavailableBlocksWithinRange(date1, date2); 
	}
	
	public static synchronized List<AssignmentBlock> getAllAssignmentBlocksWithinRange(Date date1, Date date2) {
		return TimeBlockStorage.getAllAssignmentBlocksWithinRange(date1, date2); 
	}

	public static synchronized void addTimeBlock(ITimeBlockable block) throws StorageServiceException {
		TimeBlockStorage.addTimeBlock(block); 
	}
	
	public static synchronized void mergeAllTimeBlocks(List<ITimeBlockable> block) {
		TimeBlockStorage.mergeAllTimeBlocks(block); 
	}
	
	public static synchronized ITimeBlockable updateTimeBlock(ITimeBlockable block) {
		return TimeBlockStorage.updateTimeBlock(block); 
	}
	
	public static synchronized ITimeBlockable removeTimeBlock(ITimeBlockable block) {
		return TimeBlockStorage.removeTimeBlock(block); 
	}

	/*
	 * ================================================================
	 * 
	 * CRUD and dynamic queries for Templates and Steps 
	 *  
	 * ================================================================
	 */
	
	public static synchronized Template getTemplate(String id) {
		return TemplateStepStorage.getTemplate(id, _templates); 
	}
	
	public static synchronized ITemplate addTemplate(ITemplate temp) throws StorageServiceException {
		return TemplateStepStorage.addTemplate(temp); 
	}
	
	public static synchronized ITemplate updateTemplate(ITemplate temp) {
		return TemplateStepStorage.updateTemplate(temp); 
	}
	
	public static synchronized List<ITemplate> getAllTemplates() {
		return TemplateStepStorage.getAllTemplates();
	}
	
	/*
	 * ================================================================
	 * 
	 * CRUD and dynamic queries for Settings 
	 *  
	 * ================================================================
	 */

	public static synchronized void addSetting(String name, String val) {
		SettingStorage.addSetting(name, val); 
	}
	
	public static synchronized String getSetting(String name) {
		return SettingStorage.getSetting(name); 
	}
	
	public static synchronized Map<String, String> getAllSettings() {
		return SettingStorage.getAllSettings(); 
	}
	
	/*
	 * ================================================================
	 * 
	 * Helper methods 
	 *  
	 * ================================================================
	 */
	
	protected static String concatColumn(String columnName, String dataType) {
		return columnName + " " + dataType; 
	}
}
