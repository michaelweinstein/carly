package backend.database;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;

import data.ITask;
import data.ITemplate;
import data.ITemplateStep;
import data.Template;
import data.TemplateStep;
import data.TimeOfDay;
import frontend.Utils;

public class TemplateStepStorage {
	
	/**
	 * Builds the create table strings for the initialize method in StorageService
	 * 
	 * @param queries List of create tables queries for StorageService to execute
	 */
	protected static void buildTable(final ArrayList<String> queries) {
		// Template table
		final ArrayList<String> templateCols = new ArrayList<>();
		templateCols.add(StorageService.concatColumn("TEMPLATE_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
		templateCols.add(StorageService.concatColumn("TEMPLATE_NAME", "VARCHAR(255)"));
		templateCols.add(StorageService.concatColumn("TEMPLATE_CONSECUTIVE_HOURS", "DOUBLE"));
		templateCols.add(StorageService.concatColumn("TEMPLATE_NUM_CONSECUTIVE", "INT"));
		queries.add(Utilities.buildCreateString("TEMPLATE", templateCols));
		
		// Template step table
		final ArrayList<String> stepCols = new ArrayList<>();
		stepCols.add(StorageService.concatColumn("TEMPLATE_ID", "VARCHAR(255) NOT NULL"));
		stepCols.add(StorageService.concatColumn("FOREIGN KEY (TEMPLATE_ID)",
				"REFERENCES TEMPLATE (TEMPLATE_ID) ON DELETE CASCADE"));
		stepCols.add(StorageService.concatColumn("STEP_NAME", "VARCHAR(255)"));
		stepCols.add(StorageService.concatColumn("STEP_PERCENT_TOTAL", "DOUBLE"));
		stepCols.add(StorageService.concatColumn("STEP_STEP_NUMBER", "INT"));
		stepCols.add(StorageService.concatColumn("STEP_TIME_OF_DAY", "VARCHAR(255)"));
		stepCols.add(StorageService.concatColumn("STEP_TOD_COUNTERS", "ARRAY"));
		stepCols.add(StorageService.concatColumn("PRIMARY KEY", "(TEMPLATE_ID, STEP_NAME)"));
		queries.add(Utilities.buildCreateString("TEMPLATE_STEP", stepCols));
	}
	
	/**
	 * Get Template corresponding to the provided Id
	 * 
	 * @param id Id of the template to be found
	 * @param templates Cache of templates recently searched for
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Found template
	 */
	protected static ITemplate getTemplate(final String id, final Cache<ITemplate> templates,
			final JdbcConnectionPool pool, final boolean forceUpdate) {
		if (templates.contains(id) && !forceUpdate) {
			return templates.get(id);
		}
		
		PreparedStatement statement = null;
		Connection con = null;
		Template template = null;
		final ArrayList<ArrayList<ITemplateStep>> listOfStepLists = new ArrayList<>();
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			statement = con.prepareStatement(Utilities.SELECT_TEMPLATES_AND_STEPS_BY_ID);
			Utilities.setValues(statement, id);
			final ResultSet templateStepResults = statement.executeQuery();
			
			while (templateStepResults.next()) {
				// Reconstructing the template step
				final String stepName = templateStepResults.getString("STEP_NAME");
				final double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
				final int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
				final String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
				final TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay);
				
				final String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");
				final TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber, stepTimeOfDay);
				
				// If the template hasn't already been reconstructed
				if (template == null) {
					final String templateName = templateStepResults.getString("TEMPLATE_NAME");
					final double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
					final ArrayList<ITemplateStep> templateList = new ArrayList<>();
					templateList.add(step);
					listOfStepLists.add(templateList);
					
					template = new Template(templateId, templateName, templateList, templateConsecutiveHours);
				}
				// If the template has been reconstructed, add the reconstructed TemplateStep to its list
				else {
					template.addStep(step);
				}
			}
			
			// Null check in case the template with the corresponding id is not found
			if (template != null) {
				templates.insert(id, template);
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: getTemplate: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TemplateStepStorage: getTemplate: " + "could not retrieve assignments", e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TemplateStepStorage: getTemplate: " + "could not close resource", x);
			}
		}
		
		if (template != null) {
			TemplateStepStorage.sortSteps(template);
		}
		return template;
	}
	
	/**
	 * Get Template corresponding to the provided name
	 * 
	 * @param name Name of the Template to be found
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Found template
	 */
	protected static ITemplate getTemplateByName(final String name, final JdbcConnectionPool pool) {
		PreparedStatement statement = null;
		Connection con = null;
		Template template = null;
		ArrayList<ITemplateStep> stepList = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			statement = con.prepareStatement(Utilities.SELECT_TEMPLATES_AND_STEPS_BY_NAME);
			Utilities.setValues(statement, name);
			final ResultSet templateStepResults = statement.executeQuery();
			
			while (templateStepResults.next()) {
				// Reconstructing the template step
				final String stepName = templateStepResults.getString("STEP_NAME");
				final double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
				final int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
				final String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
				final TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay);
				
				final String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");
				final TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber, stepTimeOfDay);
				
				// If the template hasn't already been reconstructed
				if (template == null) {
					final String templateName = templateStepResults.getString("TEMPLATE_NAME");
					final double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
					final ArrayList<ITemplateStep> templateList = new ArrayList<>();
					templateList.add(step);
					stepList = templateList;
					
					template = new Template(templateId, templateName, templateList, templateConsecutiveHours);
				}
				// If the template has been reconstructed, add the reconstructed TemplateStep to its list
				else {
					template.addStep(step);
				}
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: getTemplateByName: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TemplateStepStorage: getTemplateByName: " + "could not retrieve assignments",
					e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TemplateStepStorage: getTemplateByName: " + "could not close resource", x);
			}
		}
		
		if (template != null) {
			TemplateStepStorage.sortSteps(template);
		}
		return template;
	}
	
	/**
	 * Add a template to the database
	 * 
	 * @param temp Template to be added
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Template that was added, for chaining calls
	 * @throws StorageServiceException Thrown when the Template has zero TemplateSteps
	 */
	protected static ITemplate addTemplate(final ITemplate temp, final JdbcConnectionPool pool)
			throws StorageServiceException {
		if (temp.getAllSteps().size() == 0) {
			throw new StorageServiceException("TemplateStepStorage: addTemplate: "
				+ "Template must have at least one Template Step");
		}
		
		PreparedStatement templateStatement = null;
		PreparedStatement stepStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			templateStatement = con.prepareStatement(Utilities.INSERT_TEMPLATE);
			stepStatement = con.prepareStatement(Utilities.INSERT_TEMPLATE_STEP);
			final String templateId = temp.getID();
			
			// insert assignment
			Utilities.setValues(templateStatement, templateId, temp.getName(), temp.getPreferredConsecutiveHours(), 1);
			templateStatement.execute();
			
			// insert associated tasks
			for (final ITemplateStep step : temp.getAllSteps()) {
				Utilities.setValues(stepStatement, templateId, step.getName(), step.getPercentOfTotal(),
						step.getStepNumber(), step.getBestTimeToWork().name(), new Double[] { 0.0, 0.0, 0.0, 0.0 });
				stepStatement.addBatch();
			}
			stepStatement.executeBatch();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: addTemplate: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TemplateStepStorage: addTemplate: attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TemplateStepStorage: addTemplate: could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (templateStatement != null) {
					templateStatement.close();
				}
				if (stepStatement != null) {
					stepStatement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TemplateStepStorage: addTemplate: could not close resource", x);
			}
		}
		
		return temp;
	}
	
	/**
	 * Update a template already existing in the database
	 * 
	 * @param temp Template to be updated
	 * @param templates Cache of templates recently searched for
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Template that was updated, for chaining calls
	 * @throws StorageServiceException Thrown when the Template has zero TemplateSteps
	 */
	protected static ITemplate updateTemplate(final ITemplate temp, final Cache<ITemplate> templates,
			final JdbcConnectionPool pool) throws StorageServiceException {
		if (temp.getAllSteps().size() == 0) {
			throw new StorageServiceException("TemplateStepStorage: updateTemplate: "
				+ "Template must have at least one Template Step");
		}
		
		PreparedStatement templateStatement = null;
		PreparedStatement todCountersStatement = null;
		PreparedStatement deleteStepStatement = null;
		PreparedStatement insertStepStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			con.setAutoCommit(false);
			
			// Update the existing template, if any of the values have changed
			templateStatement = con.prepareStatement(Utilities.UPDATE_TEMPLATE);
			Utilities.setValues(templateStatement, temp.getName(), temp.getPreferredConsecutiveHours(), temp.getID());
			templateStatement.execute();
			
			// Get the tod counters and from the existing template steps
			final HashMap<String, Double[]> stepIdToCounters = new HashMap<>();
			todCountersStatement = con.prepareStatement(Utilities.SELECT_TEMPLATE_STEP_TOD_COUNTERS_BY_TEMPLATE_ID);
			Utilities.setValues(todCountersStatement, temp.getID());
			final ResultSet todCountersResults = todCountersStatement.executeQuery();
			
			while (todCountersResults.next()) {
				final String stepName = todCountersResults.getString("STEP_NAME");
				final Array array = todCountersResults.getArray("STEP_TOD_COUNTERS");
				final Object[] objArray = (Object[]) array.getArray();
				
				final Double[] todCounters = new Double[objArray.length];
				for (int i = 0; i < objArray.length; i++) {
					todCounters[i] = (Double) objArray[i];
				}
				
				stepIdToCounters.put(stepName, todCounters);
			}
			
			// Delete all template steps from before
			deleteStepStatement = con.prepareStatement(Utilities.DELETE_TEMPLATE_STEPS_BY_ID);
			Utilities.setValues(deleteStepStatement, temp.getID());
			deleteStepStatement.execute();
			
			// Insert new template steps
			insertStepStatement = con.prepareStatement(Utilities.INSERT_TEMPLATE_STEP);
			for (final ITemplateStep step : temp.getAllSteps()) {
				
				Double[] todCounters = { 0.0, 0.0, 0.0, 0.0 };
				if (stepIdToCounters.containsKey(step.getName())) {
					todCounters = stepIdToCounters.get(step.getName());
				}
				
				Utilities.setValues(insertStepStatement, temp.getID(), step.getName(), step.getPercentOfTotal(),
						step.getStepNumber(), step.getBestTimeToWork().name(), todCounters);
				insertStepStatement.addBatch();
			}
			insertStepStatement.executeBatch();
			
			templates.insert(temp.getID(), temp);
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: updateTemplate: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException(
					"TemplateStepStorage: updateTemplate: " + "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TemplateStepStorage: updateTemplate: "
						+ "could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (templateStatement != null) {
					templateStatement.close();
				}
				if (todCountersStatement != null) {
					todCountersStatement.close();
				}
				if (deleteStepStatement != null) {
					deleteStepStatement.close();
				}
				if (insertStepStatement != null) {
					insertStepStatement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TemplateStepStorage: updateTemplate: could not close resource", x);
			}
		}
		
		return temp;
	}
	
	/**
	 * Remove a template from the database
	 * 
	 * @param temp Template to be removed
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Template that was removed, for chaining method calls
	 */
	protected static ITemplate removeTemplate(final ITemplate temp, final JdbcConnectionPool pool) {
		PreparedStatement statement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			statement = con.prepareStatement(Utilities.DELETE_TEMPLATE);
			Utilities.setValues(statement, temp.getID());
			statement.execute();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: removeTemplate: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException(
					"TemplateStepStorage: removeTemplate: " + "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TemplateStepStorage: removeTemplate: "
						+ "could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TemplateStepStorage: removeTemplate: could not close resource", x);
			}
		}
		return temp;
	}
	
	/**
	 * Get all templates stored in the database
	 * 
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return List containing all templates stored in the database
	 */
	protected static List<ITemplate> getAllTemplates(final JdbcConnectionPool pool) {
		final ArrayList<ITemplate> results = new ArrayList<>();
		final HashMap<String, Template> idToTemplate = new HashMap<>();
		final ArrayList<ArrayList<ITemplateStep>> listOfTaskLists = new ArrayList<>();
		PreparedStatement statement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			statement = con.prepareStatement(Utilities.SELECT_ALL_TEMPLATES_AND_STEPS);
			final ResultSet templateStepResults = statement.executeQuery();
			
			while (templateStepResults.next()) {
				// Reconstructing the template step
				final String stepName = templateStepResults.getString("STEP_NAME");
				final double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
				final int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
				final String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
				final TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay);
				
				final String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");
				final TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber, stepTimeOfDay);
				Template template;
				
				if (!idToTemplate.containsKey(templateId)) {
					final String templateName = templateStepResults.getString("TEMPLATE_NAME");
					final double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
					final ArrayList<ITemplateStep> templateList = new ArrayList<>();
					templateList.add(step);
					listOfTaskLists.add(templateList);
					
					template = new Template(templateId, templateName, templateList, templateConsecutiveHours);
					idToTemplate.put(templateId, template);
				} else {
					template = idToTemplate.get(templateId);
					template.addStep(step);
				}
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: getAllTemplates: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TemplateStepStorage: getAllTemplates: " + "could not retrieve all templates",
					e);
		}
		finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TemplateStepStorage: getAllTemplates: " + "could not close resource", x);
			}
		}
		
		results.addAll(idToTemplate.values());
		for (final ITemplate template : results) {
			TemplateStepStorage.sortSteps(template);
		}
		return results;
	}
	
	/**
	 * Learn from and set the appropriate TimeOfDay for a TemplateStep corresponding to the Task
	 * 
	 * @param task Task corresponding to the TemplateStep we will update
	 * @param todKey String name of the TimeOfDay enum we will apply the deltaTod to
	 * @param deltaTod How much we will change the counter by
	 * @param templates Cache of templates recently searched for
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 */
	protected static void learnTemplateStepTimeOfDay(final ITask task, final String todKey, final double deltaTod,
			final Cache<ITemplate> templates, final JdbcConnectionPool pool) {
		// Can't learn when the todKey is a valid time of day
		if (todKey.equals("")) {
			return;
		}
		
		PreparedStatement retrieveStatement = null;
		PreparedStatement storeStatement = null;
		Connection con = null;
		String templateId = "";
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			retrieveStatement = con.prepareStatement(Utilities.SELECT_TEMPLATE_STEP_BY_ASGN_ID_AND_STEP_NUM);
			Utilities.setValues(retrieveStatement, task.getAssignmentID(), task.getTaskNumber());
			final ResultSet templateStepResult = retrieveStatement.executeQuery();
			
			// Skip the column headings
			templateStepResult.next();
			
			// Recreate the necessary fields from the results
			templateId = templateStepResult.getString("TEMPLATE_ID");
			final String stepName = templateStepResult.getString("STEP_NAME");
			String highestTodKey = templateStepResult.getString("STEP_TIME_OF_DAY");
			final Array array = templateStepResult.getArray("STEP_TOD_COUNTERS");
			final Object[] objArray = (Object[]) array.getArray();
			final Double[] todCounters = new Double[objArray.length];
			for (int i = 0; i < objArray.length; i++) {
				todCounters[i] = (Double) objArray[i];
			}
			
			// Modify tod with learnings
			final int countersIndex = TemplateStepStorage.todKeyToIndex(todKey);
			if (countersIndex != -1) {
				todCounters[countersIndex] += deltaTod;
				highestTodKey = TemplateStepStorage.maxTodKey(todCounters);
			} else {
				Utils.printError("TimeBlockStorage: learnTemplateStepTimeOfDay: "
					+ "Could not resolve passed in todKey: " + todKey);
			}
			
			// Update the db with the new learnings
			storeStatement = con.prepareStatement(Utilities.UPDATE_TEMPLATE_STEP_TOD);
			Utilities.setValues(storeStatement, highestTodKey, todCounters, templateId, stepName);
			storeStatement.execute();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: learnTemplateStepTimeOfDay: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TemplateStepStorage: learnTemplateStepTimeOfDay: "
				+ "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TemplateStepStorage: learnTemplateStepTimeOfDay: "
						+ "could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (retrieveStatement != null) {
					retrieveStatement.close();
				}
				if (storeStatement != null) {
					storeStatement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TemplateStepStorage: learnTemplateStepTimeOfDay: "
					+ "could not close resource", x);
			}
		}
		
		// Update the cache
		if (!templateId.equals("")) {
			final ITemplate template = StorageService.getTemplate(templateId, true);
			templates.insert(templateId, template);
		}
	}
	
	/**
	 * Learn from and set the consecutiveHours for the Template corresponding to the Task
	 * 
	 * @param task Task corresponding to the Template we will update
	 * @param consecutiveHours Consecutive hours we will add to the running average
	 * @param templates Cache of templates recently searched for
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 */
	protected static void learnTemplateConsecutiveHours(final ITask task, final double consecutiveHours,
			final Cache<ITemplate> templates, final JdbcConnectionPool pool) {
		PreparedStatement retrieveStatement = null;
		PreparedStatement storeStatement = null;
		Connection con = null;
		String templateId = "";
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			retrieveStatement = con.prepareStatement(Utilities.SELECT_TEMPLATE_BY_ASGN_ID);
			Utilities.setValues(retrieveStatement, task.getAssignmentID());
			final ResultSet templateResult = retrieveStatement.executeQuery();
			
			// Skip the column headings
			templateResult.next();
			
			// Recreate the necessary fields from the results
			templateId = templateResult.getString("TEMPLATE_ID");
			double templateConsecutiveHours = templateResult.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
			int templateNumConsecutive = templateResult.getInt("TEMPLATE_NUM_CONSECUTIVE");
			
			templateConsecutiveHours = (templateConsecutiveHours * templateNumConsecutive + consecutiveHours)
				/ (templateNumConsecutive + 1);
			templateNumConsecutive++;
			
			// Update the db with the new learnings
			storeStatement = con.prepareStatement(Utilities.UPDATE_TEMPLATE_CONSECUTIVE_HOURS);
			Utilities.setValues(storeStatement, templateConsecutiveHours, templateNumConsecutive, templateId);
			storeStatement.execute();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: learnTemplateConsecutiveHours: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TemplateStepStorage: learnTemplateConsecutiveHours: "
				+ "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TemplateStepStorage: learnTemplateConsecutiveHours: "
						+ "could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (retrieveStatement != null) {
					retrieveStatement.close();
				}
				if (storeStatement != null) {
					storeStatement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TemplateStepStorage: learnTemplateConsecutiveHours: "
					+ "could not close resource", x);
			}
		}
		
		// Update the cache
		if (!templateId.equals("")) {
			final ITemplate template = StorageService.getTemplate(templateId, true);
			templates.insert(templateId, template);
		}
	}
	
	/*
	 * Helper methods
	 */
	
	private static int todKeyToIndex(final String todKey) {
		int i = 0;
		for (final TimeOfDay tod : TimeOfDay.values()) {
			if (tod.name().equals(todKey)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	private static String countersIndexTotodKey(final int countersIndex) {
		int i = 0;
		for (final TimeOfDay tod : TimeOfDay.values()) {
			if (i == countersIndex) {
				return tod.name();
			}
			i++;
		}
		return "";
	}
	
	private static String maxTodKey(final Double[] todCounters) {
		int maxIndex = 0;
		double maxValue = 0.0;
		int i = 0;
		for (final Double count : todCounters) {
			if (count > maxValue) {
				maxValue = count;
				maxIndex = i;
			}
			i++;
		}
		return TemplateStepStorage.countersIndexTotodKey(maxIndex);
	}
	
	public static void sortSteps(final ITemplate template) {
		Collections.sort(template.getAllSteps(), new Comparator<ITemplateStep>() {
			
			@Override
			public int compare(final ITemplateStep arg0, final ITemplateStep arg1) {
				final int stepNum1 = arg0.getStepNumber();
				final int stepNum2 = arg1.getStepNumber();
				
				if (stepNum1 > stepNum2) {
					return 1;
				} else if (stepNum1 < stepNum2) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		;
	}
}
