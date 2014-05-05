package backend.database.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backend.database.StorageService;
import backend.database.StorageServiceException;

public class StorageServiceTest {
	
	Connection	_con;
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(true);
		final Properties props = new Properties();
		
		try {
			props.loadFromXML(new FileInputStream(new File("config/db.properties")));
			Class.forName("org.h2.Driver");
			_con = DriverManager.getConnection(props.getProperty("DB_URL"), props.getProperty("DB_USER"),
					props.getProperty("DB_PWD"));
		} catch (final ClassNotFoundException e) {
			fail("StorageServiceTest: setUp: db drive class not found: " + e.getMessage());
		} catch (final IOException e) {
			fail("StorageServiceTest: setUp: could not load database properties: " + e.getMessage());
		}
	}
	
	@After
	public void tearDown() throws Exception {
		_con.close();
		StorageService.cleanup();
	}
	
	/*
	 * Testing initialization procedure
	 */
	
	@Test
	public void createTable() {
		try {
			validateTables();
		} catch (final SQLException e) {
			fail("StorageServiceTest: createTable: could not create all tables" + e.getMessage());
		}
	}
	
	@Test
	public void createTablesWithDrop() {
		try {
			validateTables();
			StorageService.initialize(true);
			validateTables();
		} catch (final SQLException | StorageServiceException e) {
			fail("StorageServiceTest: createTablesWithDrop: could not create all tables" + e.getMessage());
		}
	}
	
	@Test
	public void createTablesNoDrop() {
		try {
			validateTables();
			StorageService.initialize(false);
			validateTables();
		} catch (final SQLException | StorageServiceException e) {
			fail("StorageServiceTest: createTablesNoDrop: could not create all tables" + e.getMessage());
		}
	}
	
	private void validateTables() throws SQLException {
		// Make sure the correct number of tables are created
		final ArrayList<String> tableNames = new ArrayList<>();
		String query = "SHOW TABLES";
		try (Statement stmt = _con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				tableNames.add(rs.getString("TABLE_NAME"));
			}
		}
		
		assertTrue(tableNames.size() == 6);
		assertTrue(tableNames.contains("ASSIGNMENT"));
		assertTrue(tableNames.contains("TASK"));
		assertTrue(tableNames.contains("TEMPLATE"));
		assertTrue(tableNames.contains("TEMPLATE_STEP"));
		assertTrue(tableNames.contains("TIME_BLOCK"));
		assertTrue(tableNames.contains("SETTING"));
		
		// Make sure Assignment table is correct
		final HashMap<String, String> asgnCol = new HashMap<>();
		query = "SHOW COLUMNS FROM ASSIGNMENT";
		try (Statement stmt = _con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				asgnCol.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE"));
			}
		}
		
		assertTrue(asgnCol.size() == 5);
		assertEquals("VARCHAR(255)", asgnCol.get("ASGN_ID"));
		assertEquals("VARCHAR(255)", asgnCol.get("ASGN_NAME"));
		assertEquals("INTEGER(10)", asgnCol.get("ASGN_EXPECTED_HOURS"));
		assertEquals("BIGINT(19)", asgnCol.get("ASGN_DATE"));
		assertEquals("VARCHAR(255)", asgnCol.get("ASGN_TEMPLATE_ID"));
		
		// Make sure Task table is correct
		final HashMap<String, String> taskCol = new HashMap<>();
		query = "SHOW COLUMNS FROM TASK";
		try (Statement stmt = _con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				taskCol.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE"));
			}
		}
		
		assertTrue(taskCol.size() == 7);
		assertEquals("VARCHAR(255)", taskCol.get("ASGN_ID"));
		assertEquals("VARCHAR(255)", taskCol.get("TASK_ID"));
		assertEquals("VARCHAR(255)", taskCol.get("TASK_NAME"));
		assertEquals("DOUBLE(17)", taskCol.get("TASK_PERCENT_TOTAL"));
		assertEquals("DOUBLE(17)", taskCol.get("TASK_PERCENT_COMPLETE"));
		assertEquals("VARCHAR(255)", taskCol.get("TASK_TIME_OF_DAY"));
		assertEquals("DOUBLE(17)", taskCol.get("TASK_SUGGESTED_LENGTH"));
		
		// Make sure Template table is correct
		final HashMap<String, String> templateCol = new HashMap<>();
		query = "SHOW COLUMNS FROM TEMPLATE";
		try (Statement stmt = _con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				templateCol.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE"));
			}
		}
		
		assertTrue(templateCol.size() == 3);
		assertEquals("VARCHAR(255)", templateCol.get("TEMPLATE_ID"));
		assertEquals("VARCHAR(255)", templateCol.get("TEMPLATE_NAME"));
		assertEquals("DOUBLE(17)", templateCol.get("TEMPLATE_CONSECUTIVE_HOURS"));
		
		// Make sure TemplateStep table is correct
		final HashMap<String, String> stepCol = new HashMap<>();
		query = "SHOW COLUMNS FROM TEMPLATE_STEP";
		try (Statement stmt = _con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				stepCol.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE"));
			}
		}
		
		assertTrue(stepCol.size() == 5);
		assertEquals("VARCHAR(255)", stepCol.get("TEMPLATE_ID"));
		assertEquals("VARCHAR(255)", stepCol.get("STEP_NAME"));
		assertEquals("DOUBLE(17)", stepCol.get("STEP_PERCENT_TOTAL"));
		assertEquals("INTEGER(10)", stepCol.get("STEP_STEP_NUMBER"));
		assertEquals("VARCHAR(255)", stepCol.get("STEP_TIME_OF_DAY"));
		
		// Make sure TimeBlock table is correct
		final HashMap<String, String> blockCol = new HashMap<>();
		query = "SHOW COLUMNS FROM TIME_BLOCK";
		try (Statement stmt = _con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				blockCol.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE"));
			}
		}
		
		assertTrue(blockCol.size() == 5);
		assertEquals("VARCHAR(255)", blockCol.get("BLOCK_ID"));
		assertEquals("VARCHAR(255)", blockCol.get("TASK_ID"));
		assertEquals("BIGINT(19)", blockCol.get("BLOCK_START"));
		assertEquals("BIGINT(19)", blockCol.get("BLOCK_END"));
		assertEquals("BOOLEAN(1)", blockCol.get("BLOCK_MOVABLE"));
		
		// Make sure Setting table is correct
		final HashMap<String, String> settingCol = new HashMap<>();
		query = "SHOW COLUMNS FROM SETTING";
		try (Statement stmt = _con.createStatement()) {
			final ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				settingCol.put(rs.getString("COLUMN_NAME"), rs.getString("TYPE"));
			}
		}
		
		assertTrue(settingCol.size() == 2);
		assertEquals("VARCHAR(255)", settingCol.get("SETTING_NAME"));
		assertEquals("VARCHAR(255)", settingCol.get("SETTING_VALUE"));
	}
}
