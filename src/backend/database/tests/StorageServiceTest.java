package backend.database.tests;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import data.Assignment;
import data.ITemplate;
import data.Task;
import data.Template;
import data.TemplateStep;
import backend.database.StorageService;
import backend.database.Utilities;


public class StorageServiceTest {
	
	Connection _con; 
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(false);
		try {
			_con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			Class.forName("org.h2.Driver");
		}
		catch (ClassNotFoundException e) {
			fail("StorageServiceTest: setUp: db drive class not found: " + e.getMessage()); 
		}
	}
	
	@After
	public void tearDown() throws Exception {
		_con.close(); 
	}
	
	/*
	 * Testing initialization procedure
	 */
	
	@Test
	public void createTables() {		
		try {
			this.validateTables();
		} 
		catch (SQLException e) {
			fail("StorageServiceTest: createTables: could not create all tables" + e.getMessage()); 
		}
	}
	
	@Test
	public void createTablesMultipleTimes() {
		try {
			this.validateTables();
			StorageService.initialize(true);
			this.validateTables(); 
		} 
		catch (SQLException e) {
			fail("StorageServiceTest: createTables: could not create all tables" + e.getMessage()); 
		}
	}
	
	//TODO: fix this test -- need to pair type with column name
	private void validateTables() throws SQLException {
		//Make sure the correct number of tables are created
		ArrayList<String> tableNames = new ArrayList<>(); 
		String query = "SHOW TABLES"; 
	    try (Statement stmt = _con.createStatement()) {
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	tableNames.add(rs.getString("TABLE_NAME")); 
	        }
	    }
		
		assertTrue(tableNames.size() == 4);
		assertTrue(tableNames.contains("ASSIGNMENT"));
		assertTrue(tableNames.contains("TASK"));
		assertTrue(tableNames.contains("TEMPLATE"));
		assertTrue(tableNames.contains("TEMPLATE_STEP"));
		
		//Make sure Assignment table is correct
		ArrayList<String> asgnColName = new ArrayList<>();
		ArrayList<String> asgnColType = new ArrayList<>();
		query = "SHOW COLUMNS FROM ASSIGNMENT"; 
	    try (Statement stmt = _con.createStatement()) {
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	asgnColName.add(rs.getString("COLUMN_NAME"));  
	        	asgnColType.add(rs.getString("TYPE")); 
	        }
	    }
	    assertTrue(asgnColName.size() == 5);
        assertTrue(asgnColName.contains("ASGN_ID"));
        assertTrue(asgnColName.contains("ASGN_NAME"));
        assertTrue(asgnColName.contains("ASGN_EXPECTED_HOURS"));
        assertTrue(asgnColName.contains("ASGN_DATE"));
        assertTrue(asgnColName.contains("ASGN_TEMPLATE_ID"));
        
        assertTrue(asgnColType.size() == 5);
        assertTrue(asgnColType.contains("VARCHAR(255)"));
        assertTrue(asgnColType.contains("VARCHAR(255)"));
        assertTrue(asgnColType.contains("INTEGER(10)"));
        assertTrue(asgnColType.contains("DATE(8)"));
        assertTrue(asgnColType.contains("VARCHAR(255)"));
		
		//Make sure Task table is correct
		ArrayList<String> taskColName = new ArrayList<>();
		ArrayList<String> taskColType = new ArrayList<>();
		query = "SHOW COLUMNS FROM TASK"; 
	    try (Statement stmt = _con.createStatement()) {
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	taskColName.add(rs.getString("COLUMN_NAME"));  
	        	taskColType.add(rs.getString("TYPE")); 
	        }
	    }
	    
	    assertTrue(taskColName.size() == 7);
        assertTrue(taskColName.contains("ASGN_ID"));
        assertTrue(taskColName.contains("TASK_ID"));
        assertTrue(taskColName.contains("TASK_NAME"));
        assertTrue(taskColName.contains("TASK_PERCENT_TOTAL"));
        assertTrue(taskColName.contains("TASK_PERCENT_COMPLETE"));
        assertTrue(taskColName.contains("TASK_TIME_OF_DAY"));
        assertTrue(taskColName.contains("TASK_SUGGESTED_LENGTH"));
        
        assertTrue(taskColType.size() == 7);
        assertTrue(taskColType.contains("VARCHAR(255)"));
        assertTrue(taskColType.contains("VARCHAR(255)"));
        assertTrue(taskColType.contains("VARCHAR(255)"));
        assertTrue(taskColType.contains("DOUBLE(17)"));
        assertTrue(taskColType.contains("DOUBLE(17)"));
        assertTrue(taskColType.contains("VARCHAR(255)"));
        assertTrue(taskColType.contains("DOUBLE(17)"));
		
		//Make sure Template table is correct
        ArrayList<String> templateColName = new ArrayList<>();
		ArrayList<String> templateColType = new ArrayList<>();
		query = "SHOW COLUMNS FROM TEMPLATE"; 
	    try (Statement stmt = _con.createStatement()) {
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	templateColName.add(rs.getString("COLUMN_NAME"));  
	        	templateColType.add(rs.getString("TYPE")); 
	        }
	    }
        
	    assertTrue(templateColName.size() == 3);
        assertTrue(templateColName.contains("TEMPLATE_ID"));
        assertTrue(templateColName.contains("TEMPLATE_NAME"));
        assertTrue(templateColName.contains("TEMPLATE_CONSECUTIVE_HOURS"));
        
        assertTrue(templateColType.size() == 3);
        assertTrue(templateColType.contains("VARCHAR(255)"));
        assertTrue(templateColType.contains("VARCHAR(255)"));
        assertTrue(templateColType.contains("DOUBLE(17)"));
        
		//Make sure TemplateStep table is correct
        ArrayList<String> stepColName = new ArrayList<>();
		ArrayList<String> stepColType = new ArrayList<>();
		query = "SHOW COLUMNS FROM TEMPLATE_STEP"; 
	    try (Statement stmt = _con.createStatement()) {
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	stepColName.add(rs.getString("COLUMN_NAME"));  
	        	stepColType.add(rs.getString("TYPE")); 
	        }
	    }
        
	    assertTrue(stepColName.size() == 7);
        assertTrue(stepColName.contains("TEMPLATE_ID"));
        assertTrue(stepColName.contains("STEP_NAME"));
        assertTrue(stepColName.contains("STEP_PERCENT_TOTAL"));
        assertTrue(stepColName.contains("STEP_STEP_NUMBER"));
        assertTrue(stepColName.contains("STEP_NUM_DAYS"));
        assertTrue(stepColName.contains("STEP_HOURS_PER_DAY"));
        assertTrue(stepColName.contains("STEP_TIME_OF_DAY"));
        
        assertTrue(stepColType.size() == 7);
        assertTrue(stepColType.contains("VARCHAR(255)"));
        assertTrue(stepColType.contains("VARCHAR(255)"));
        assertTrue(stepColType.contains("DOUBLE(17)"));
        assertTrue(stepColType.contains("INTEGER(10)"));
        assertTrue(stepColType.contains("INTEGER(10)"));
        assertTrue(stepColType.contains("DOUBLE(17)"));
        assertTrue(stepColType.contains("VARCHAR(255)"));
	}
}
