package backend.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.h2.jdbcx.JdbcConnectionPool;

import data.Assignment;
import data.AssignmentBlock;
import data.IAssignment;
import data.ITask;
import data.ITemplate;
import data.ITimeBlockable;
import data.UnavailableBlock;
import frontend.Main;

/**
 * Handles the storage, retrieval and persistence of data for Carly
 * 
 * @author eb27
 */
public class StorageService {
	
	private static Cache<ITemplate>		_templates;
	private static JdbcConnectionPool	_pool;
	
	/**
	 * Called each time application starts up
	 * 
	 * @param dropTables If true, recreates new blank tables; if false, persists data from last time
	 */
	public static void initialize(final boolean dropTables) {
		_templates = new Cache<>();
		final Properties props = new Properties();
		try {
			props.loadFromXML(new FileInputStream(Main.class.getClassLoader().getResource("db.properties")
					.getPath().replaceAll("%20", " ")));
		} catch (final IOException x) {
			Utilities.printException("StorageService: initialize: could not load database properties", x);
		}
		
		System.out.println(props.getProperty("DB_URL") + " | " + props.getProperty("DB_USER") + " | "
			+ props.getProperty("DB_PWD"));
		_pool = JdbcConnectionPool.create(props.getProperty("DB_URL"), props.getProperty("DB_USER"),
				props.getProperty("DB_PWD"));
		
		// Create tables in the database
		try (Connection con = _pool.getConnection()) {
			Class.forName("org.h2.Driver");
			if (dropTables) {
				try (Statement stmt = con.createStatement()) {
					stmt.execute(Utilities.DROP_ALL_TABLES);
				}
			}
			
			final ArrayList<String> queries = new ArrayList<>();
			AssignmentTaskStorage.buildTable(queries);
			TemplateStepStorage.buildTable(queries);
			TimeBlockStorage.buildTable(queries);
			SettingStorage.buildTable(queries);
			
			try (Statement stmt = con.createStatement()) {
				for (final String query : queries) {
					stmt.addBatch(query);
				}
				stmt.executeBatch();
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("StorageService: initialize: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("StorageService: initialize: could not create all tables", e);
		}
	}
	
	/**
	 * Cleans up when the application is shutting down
	 */
	public static void cleanup() {
		_pool.dispose();
	}
	
	/*
	 * ================================================================ CRUD and dynamic queries for Assignments & Tasks
	 * ================================================================
	 */
	
	/**
	 * Adds an Assignment and all of the Assignment's associated Tasks to the database The Assignment's associated
	 * Template must already be in the database
	 * 
	 * @param assignment Assignment to be added to the database
	 * @return Assignment that was added, for chaining calls
	 * @throws StorageServiceException Thrown when the Assigment's associated Template is not in the db
	 */
	public static IAssignment addAssignment(final IAssignment assignment) throws StorageServiceException {
		return AssignmentTaskStorage.addAssignment(assignment, _pool);
	}
	
	/**
	 * Remove an Assignment and all of its associated Tasks from the database
	 * 
	 * @param assignment IAssignment to be removed
	 * @return IAssignment that was removed, for chaining calls
	 */
	public static IAssignment removeAssignment(final IAssignment assignment) {
		return AssignmentTaskStorage.removeAssignment(assignment, _pool);
	}
	
	/**
	 * Update Assignment and clear and repopulate its associated Tasks Checks to see if the Template associated with the
	 * Assignment can still be found in the db
	 * 
	 * @param assignment Assignment to be updated
	 * @return Assignment that was updated, for chaining calls
	 * @throws StorageServiceException Thrown when the Assignment's associated Template cannot be found in the db
	 */
	public static Assignment updateAssignment(final Assignment assignment) throws StorageServiceException {
		return AssignmentTaskStorage.updateAssignment(assignment, _pool);
	}
	
	/**
	 * Get the Assignment identified by the passed-in id value
	 * 
	 * @param toBeFoundId Id value of the Assignment to be retrieved
	 * @return Assignment that was found, or null if the Assignment was not found
	 */
	public static Assignment getAssignment(final String toBeFoundId) {
		return AssignmentTaskStorage.getAssignment(toBeFoundId, _templates, _pool);
	}
	
	/**
	 * Retrieves all Assignments whose dueDate falls into the range specified, inclusive of the bounds
	 * 
	 * @param date1 Lower bound of the date range
	 * @param date2 Upper bound of the date range
	 * @return List of Assignments whose dueDate falls within the specified date range
	 */
	public static List<Assignment> getAllAssignmentsWithinRange(final Date date1, final Date date2) {
		return AssignmentTaskStorage.getAllAssignmentsWithinRange(date1, date2, _templates, _pool);
	}
	
	/**
	 * Retrieves all the Tasks whose associated Assignment's dueDate falls within the range specified, inclusive of
	 * bounds.
	 * 
	 * @param date1 Lower bound of the date range
	 * @param date2 Upper bound of the date range
	 * @return List of Tasks that fall within the date range specified
	 */
	public static List<ITask> getAllTasksWithinRange(final Date date1, final Date date2) {
		return AssignmentTaskStorage.getAllTasksWithinRange(date1, date2, _pool);
	}
	
	/*
	 * ================================================================ CRUD and dynamic queries for TimeBlocks
	 * ================================================================
	 */
	
	/**
	 * Gets all Unavailable Blocks within the specified range
	 * 
	 * @param date1 Lower bound for the date range
	 * @param date2 Upper bound for the date range
	 * @return List of all the blocks that fall COMPLETELY within these bounds
	 */
	public static List<UnavailableBlock> getAllUnavailableBlocksWithinRange(final Date date1, final Date date2) {
		return TimeBlockStorage.getAllUnavailableBlocksWithinRange(date1, date2, _pool);
	}
	
	/**
	 * Gets all Assignment Blocks within the specified range
	 * 
	 * @param date1 Lower bound for the date range
	 * @param date2 Upper bound for the date range
	 * @return List of all the blocks that fall COMPLETELY within these bounds
	 */
	public static List<AssignmentBlock> getAllAssignmentBlocksWithinRange(final Date date1, final Date date2) {
		return TimeBlockStorage.getAllAssignmentBlocksWithinRange(date1, date2, _pool);
	}
	
	/**
	 * Gets an Assignment Block
	 * 
	 * @param blockId Id of the Assignment Block in question
	 * @return AssignmentBlock corresponding to the given id
	 */
	public static AssignmentBlock getAssignmentBlock(final String blockId) {
		return TimeBlockStorage.getAssignmentBlock(blockId, _pool);
	}
	
	/**
	 * Get an Unavailable Block
	 * 
	 * @param blockId Block id of the unavailable block
	 * @return UnavailableBlock found corresponding to the given id
	 */
	public static UnavailableBlock getUnavailableBlock(final String blockId) {
		return TimeBlockStorage.getUnavailableBlock(blockId, _pool);
	}
	
	/**
	 * Adds a Time Block
	 * 
	 * @param block Block to be stored in the database
	 * @throws StorageServiceException When the TimeBlock's associated Task is not in the database
	 */
	public static void addTimeBlock(final ITimeBlockable block) throws StorageServiceException {
		TimeBlockStorage.addTimeBlock(block, _pool);
	}
	
	/**
	 * Adds VALID TimeBlocks if they don't already exist in the database Updates any TimeBlocks that already have been
	 * stored in the database
	 * 
	 * @param blockList
	 * @return A list of INVALID TimeBlocks (that is, those whose associated Task cannot be found in the database) that
	 *         were NOT added/updated
	 */
	public static List<ITimeBlockable> mergeAllTimeBlocks(final List<ITimeBlockable> blockList) {
		return TimeBlockStorage.mergeAllTimeBlocks(blockList, _pool);
	}
	
	/**
	 * Update TimeBlock with new start date, end date and associated task values
	 * 
	 * @param block Updated block
	 * @return Block that was passed in, for chaining calls
	 * @throws StorageServiceException Thrown when the TimeBlock's associated Task cannot be found in the database
	 */
	public static ITimeBlockable updateTimeBlock(final ITimeBlockable block) throws StorageServiceException {
		return TimeBlockStorage.updateTimeBlock(block, _pool);
	}
	
	/**
	 * Remove TimeBlock from the database
	 * 
	 * @param block Block to remove from the database
	 * @return Block that was removed, for chaining calls
	 */
	public static ITimeBlockable removeTimeBlock(final ITimeBlockable block) {
		return TimeBlockStorage.removeTimeBlock(block, _pool);
	}
	
	/*
	 * ================================================================ CRUD and dynamic queries for Templates and Steps
	 * ================================================================
	 */
	
	/**
	 * Get Template corresponding to the provided Id
	 * 
	 * @param id Id of the template to be found
	 * @return Found template
	 */
	public static ITemplate getTemplate(final String id) {
		return TemplateStepStorage.getTemplate(id, _templates, _pool);
	}
	
	/**
	 * Get all templates stored in the database
	 * 
	 * @return List containing all templates stored in the database
	 */
	public static List<ITemplate> getAllTemplates() {
		return TemplateStepStorage.getAllTemplates(_pool);
	}
	
	/**
	 * Add a template to the database
	 * 
	 * @param temp Template to be added
	 * @return Template that was added, for chaining calls
	 * @throws StorageServiceException Thrown when the Template has zero TemplateSteps
	 */
	public static ITemplate addTemplate(final ITemplate temp) throws StorageServiceException {
		return TemplateStepStorage.addTemplate(temp, _pool);
	}
	
	/**
	 * Update a template already existing in the database
	 * 
	 * @param temp Template to be updated
	 * @return Template that was updated, for chaining calls
	 * @throws StorageServiceException Thrown when the Template has zero TemplateSteps
	 */
	public static ITemplate updateTemplate(final ITemplate temp) throws StorageServiceException {
		return TemplateStepStorage.updateTemplate(temp, _templates, _pool);
	}
	
	/**
	 * Remove a template from the database
	 * 
	 * @param temp Template to be removed
	 * @return Template that was removed, for chaining method calls
	 */
	public static ITemplate removeTemplate(final ITemplate temp) {
		return TemplateStepStorage.removeTemplate(temp, _pool);
	}
	
	/*
	 * ================================================================ CRUD and dynamic queries for Settings
	 * ================================================================
	 */
	
	/**
	 * Merge one setting into the database
	 * 
	 * @param name String name of the setting to be merged
	 * @param val String value of the setting to be merged
	 */
	public static void mergeSetting(final String name, final String val) {
		SettingStorage.mergeSetting(name, val, _pool);
	}
	
	/**
	 * Merge all settings to the database
	 * 
	 * @param settings Map of String to String where key is the name of the setting and value is the info of the setting
	 * @return Boolean indicating if ALL settings were merged successfully
	 */
	public static boolean mergeAllSettings(final Map<String, String> settings) {
		return SettingStorage.mergeAllSettings(settings, _pool);
	}
	
	/**
	 * Get the setting information corresponding to the passed-in setting name
	 * 
	 * @param name Setting name to retrieve information for
	 * @return String value for the setting name passed in
	 */
	public static String getSetting(final String name) {
		return SettingStorage.getSetting(name, _pool);
	}
	
	/**
	 * Gets all the settings stored in the database
	 * 
	 * @return Mapping of String to String where key is the name of the setting, and value is the info of the setting
	 */
	public static Map<String, String> getAllSettings() {
		return SettingStorage.getAllSettings(_pool);
	}
	
	/*
	 * ================================================================ Helper methods
	 * ================================================================
	 */
	
	protected static String concatColumn(final String columnName, final String dataType) {
		return columnName + " " + dataType;
	}
}
