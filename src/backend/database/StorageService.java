package backend.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
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
import data.Template;
import data.TemplateStep;
import data.UnavailableBlock;

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
	 * @return boolean True if first start, false if not first start
	 * @throws StorageServiceException if there was an error
	 */
	public static boolean initialize(final boolean dropTables) throws StorageServiceException {
		_templates = new Cache<>();
		final Properties props = new Properties();
		try {
			props.loadFromXML(new FileInputStream(new File("config/db.properties")));
		} catch (final IOException x) {
			throw new StorageServiceException("StorageService: initialize: could not load database properties"
				+ x.getMessage());
		}
		_pool = JdbcConnectionPool.create(props.getProperty("DB_URL"), props.getProperty("DB_USER"),
				props.getProperty("DB_PWD"));
		
		boolean firstStart = false;
		// Create tables in the database
		try (Connection con = _pool.getConnection()) {
			Class.forName("org.h2.Driver");
			
			int tableNum = 0;
			final String startupQuery = "SHOW TABLES";
			try (Statement stmt = con.createStatement()) {
				final ResultSet rs = stmt.executeQuery(startupQuery);
				while (rs.next()) {
					tableNum++;
				}
			}
			
			if (tableNum < 6) {
				firstStart = true;
			}
			
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
			throw new StorageServiceException("StorageService: initialize: db drive class not found: " + e.getMessage());
		} catch (final SQLException e) {
			throw new StorageServiceException("StorageService: initialize: could not create all tables: "
				+ e.getMessage());
		}
		
		// Add default templates
		if (firstStart) {
			try {
				final BufferedReader reader = new BufferedReader(new FileReader("config/templates.txt"));
				String line;
				Template currTemplate = null;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("START")) {
						final String[] arr = line.split(";");
						currTemplate = new Template(arr[1], Double.parseDouble(arr[2]));
					} else if (line.startsWith("END") && currTemplate != null) {
						StorageService.addTemplate(currTemplate);
					} else if (currTemplate != null) {
						final String[] arr = line.split(";");
						currTemplate.addStep(new TemplateStep(arr[0], Double.parseDouble(arr[1]), currTemplate
								.getAllSteps().size()));
					}
				}
				reader.close();
				
			} catch (final IOException | NumberFormatException | IndexOutOfBoundsException e) {
				throw new StorageServiceException("Template file couldn't be parsed: " + e.getMessage());
			}
		}
		return firstStart;
	}
	
	/**
	 * Cleans up when the application is shutting down
	 */
	public static void cleanup() {
		_pool.dispose();
	}
	
	/**
	 * Drops all tables from the db
	 */
	public static void dropTables() {
		try (Connection con = _pool.getConnection()) {
			Class.forName("org.h2.Driver");
			try (Statement stmt = con.createStatement()) {
				stmt.execute(Utilities.DROP_ALL_TABLES);
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("StorageService: dropTables: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("StorageService: dropTables: could not create all tables", e);
		}
	}
	
	/*
	 * ================================================ CRUD and dynamic queries for Assignments & Tasks
	 * ================================================
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
	 * Retrieves all Assignments
	 * 
	 * @return List of all Assignments stored in the db
	 */
	public static List<IAssignment> getAllAssignments() {
		return AssignmentTaskStorage.getAllAssignments(_templates, _pool);
	}
	
	/**
	 * Retrieves all Assignments whose dueDate falls into the range specified, inclusive of the bounds
	 * 
	 * @param date1 Lower bound of the date range
	 * @param date2 Upper bound of the date range
	 * @return List of Assignments whose dueDate falls within the specified date range
	 */
	public static List<IAssignment> getAllAssignmentsWithinRange(final Date date1, final Date date2) {
		return AssignmentTaskStorage.getAllAssignmentsWithinRange(date1, date2, _templates, _pool);
	}
	
	/**
	 * Update a task that already exists in the db.
	 * 
	 * @param task Task to be updated
	 * @return Task that was updated, for chaining calls
	 */
	public static ITask updateTask(final ITask task) {
		return AssignmentTaskStorage.updateTask(task, _pool);
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
	 * ======================================= CRUD and dynamic queries for TimeBlocks
	 * =======================================
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
	 * Adds VALID Available Blocks if they don't already exist in the database Updates any TimeBlocks that already have been
	 * stored in the database
	 * 
	 * All unavailable blocks passed in will be ignored. 
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
	
	/**
	 * Add all default unavailable blocks from the startup survey
	 * 
	 * @param blockList List of default unavailable blocks to add
	 */
	public static void addAllDefaultUnavailableBlocks(final List<UnavailableBlock> blockList) {
		TimeBlockStorage.addAllDefaultUnavailableBlocks(blockList, _pool);
	}
	
	/**
	 * @param startDate
	 * @param endDate
	 * @param blockList
	 */
	public static void replaceUnavailableBlocks(final Date startDate, final Date endDate,
			final List<? extends ITimeBlockable> blockList) {
		TimeBlockStorage.replaceUnavailableBlocks(startDate, endDate, blockList, _pool);
	}
	
	/*
	 * ================================================ CRUD and dynamic queries for Templates and Steps
	 * ================================================
	 */
	
	/**
	 * Get Template corresponding to the provided Id
	 * 
	 * @param id Id of the template to be found
	 * @return Found template
	 */
	public static ITemplate getTemplate(final String id) {
		return TemplateStepStorage.getTemplate(id, _templates, _pool, false);
	}
	
	/**
	 * Get Template corresponding to the provided Id Option to forceUpdate for use by learning functions in
	 * TemplateStepStorage
	 * 
	 * @param id Id of the template to be found
	 * @return Found template
	 */
	protected static ITemplate getTemplate(final String id, final boolean forceUpdate) {
		return TemplateStepStorage.getTemplate(id, _templates, _pool, forceUpdate);
	}
	
	/**
	 * Get Template corresponding to the provided name
	 * 
	 * @param name Name of the Template to be found
	 * @return Found template
	 */
	public static ITemplate getTemplateByName(final String name) {
		return TemplateStepStorage.getTemplateByName(name, _pool);
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
	
	/**
	 * Learn from and set the appropriate TimeOfDay for a TemplateStep corresponding to the Task
	 * 
	 * @param task Task corresponding to the TemplateStep we will update
	 * @param todKey String name of the TimeOfDay enum we will apply the deltaTod to
	 * @param deltaTod How much we will change the counter by
	 */
	public static void learnTemplateStepTimeOfDay(final ITask task, final String todKey, final double deltaTod) {
		TemplateStepStorage.learnTemplateStepTimeOfDay(task, todKey, deltaTod, _templates, _pool);
	}
	
	/**
	 * Learn from and set the consecutiveHours for the Template corresponding to the Task
	 * 
	 * @param task Task corresponding to the Template we will update
	 * @param consecutiveHours Consecutive hours we will add to the running average
	 */
	public static void learnTemplateConsecutiveHours(final ITask task, final double consecutiveHours) {
		TemplateStepStorage.learnTemplateConsecutiveHours(task, consecutiveHours, _templates, _pool);
	}
	
	/*
	 * ===================================== CRUD and dynamic queries for Settings =====================================
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
	 * ============== Helper methods ==============
	 */
	
	protected static String concatColumn(final String columnName, final String dataType) {
		return columnName + " " + dataType;
	}
}
