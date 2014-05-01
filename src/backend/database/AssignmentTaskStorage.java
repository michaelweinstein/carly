package backend.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.h2.jdbcx.JdbcConnectionPool;

import data.Assignment;
import data.IAssignment;
import data.ITask;
import data.ITemplate;
import data.ITemplateStep;
import data.Task;
import data.Template;
import data.TemplateStep;
import data.TimeOfDay;

public class AssignmentTaskStorage {
	
	/**
	 * Adds the create table string to the list of create table statements to be executed in StorageService
	 * 
	 * @param queries List of queries that StorageService.initialize() will execute
	 */
	protected static void buildTable(final ArrayList<String> queries) {
		// Assignment table
		final ArrayList<String> assignmentCols = new ArrayList<>();
		assignmentCols.add(StorageService.concatColumn("ASGN_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
		assignmentCols.add(StorageService.concatColumn("ASGN_NAME", "VARCHAR(255)"));
		assignmentCols.add(StorageService.concatColumn("ASGN_EXPECTED_HOURS", "INT"));
		assignmentCols.add(StorageService.concatColumn("ASGN_DATE", "BIGINT"));
		assignmentCols.add(StorageService.concatColumn("ASGN_TEMPLATE_ID", "VARCHAR(255)"));
		queries.add(Utilities.buildCreateString("ASSIGNMENT", assignmentCols));
		
		// Tasks table
		final ArrayList<String> taskCols = new ArrayList<>();
		taskCols.add(StorageService.concatColumn("ASGN_ID", "VARCHAR(255) NOT NULL"));
		taskCols.add(StorageService.concatColumn("FOREIGN KEY (ASGN_ID)",
				"REFERENCES ASSIGNMENT (ASGN_ID) ON DELETE CASCADE"));
		taskCols.add(StorageService.concatColumn("TASK_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
		taskCols.add(StorageService.concatColumn("TASK_NAME", "VARCHAR(255)"));
		taskCols.add(StorageService.concatColumn("TASK_PERCENT_TOTAL", "DOUBLE"));
		taskCols.add(StorageService.concatColumn("TASK_PERCENT_COMPLETE", "DOUBLE"));
		taskCols.add(StorageService.concatColumn("TASK_TIME_OF_DAY", "VARCHAR(255)"));
		taskCols.add(StorageService.concatColumn("TASK_SUGGESTED_LENGTH", "DOUBLE"));
		queries.add(Utilities.buildCreateString("TASK", taskCols));
	}
	
	/**
	 * Adds an Assignment and all of the Assignment's associated Tasks to the database The Assignment's associated
	 * Template must already be in the database
	 * 
	 * @param assignment Assignment to be added to the database
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Assignment that was added, for chaining calls
	 * @throws StorageServiceException Thrown when the Assigment's associated Template is not in the db
	 */
	protected static IAssignment addAssignment(final IAssignment assignment, final JdbcConnectionPool pool)
			throws StorageServiceException {
		PreparedStatement assignmentStatement = null;
		PreparedStatement taskStatement = null;
		PreparedStatement templateStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			assignmentStatement = con.prepareStatement(Utilities.INSERT_ASGN);
			taskStatement = con.prepareStatement(Utilities.INSERT_TASK);
			String assignmentId = assignment.getID();
			
			// insert assignment
			Utilities.setValues(assignmentStatement, assignmentId, assignment.getName(), assignment.getExpectedHours(),
					assignment.getDueDate().getTime(), assignment.getTemplate().getID());
			assignmentStatement.execute();
			
			// insert associated tasks
			for (ITask task : assignment.getTasks()) {
				Utilities.setValues(taskStatement, assignmentId, task.getTaskID(), task.getName(),
						task.getPercentOfTotal(), task.getPercentComplete(), task.getPreferredTimeOfDay().name(),
						task.getSuggestedBlockLength());
				taskStatement.addBatch();
			}
			taskStatement.executeBatch();
			
			// Check to see that the template associated exists in the db
			templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATE_BY_ID);
			Utilities.setValues(templateStatement, assignment.getTemplate().getID());
			ResultSet rs = templateStatement.executeQuery();
			int num = 0;
			while (rs.next()) {
				num++;
			}
			
			// If the associated template is not in the db, rollback and throw and exception
			if (num == 0) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("AssignmentTaskStorage: addAssignment: "
						+ "could not roll back transaction", x);
				}
				
				throw new StorageServiceException("AssignmentTaskStorage: addAssignment: "
					+ "Assignments's associated Template must be in the database.");
			}
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: addAssignment: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("AssignmentTaskStorage: addAssignment: "
				+ "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("AssignmentTaskStorage: addAssignment: "
						+ "could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (assignmentStatement != null) {
					assignmentStatement.close();
				}
				if (taskStatement != null) {
					taskStatement.close();
				}
				if (templateStatement != null) {
					templateStatement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("AssignmentTaskStorage: addAssignment: could not close resource", x);
			}
		}
		
		return assignment;
	}
	
	/**
	 * Remove an Assignment and all of its associated Tasks from the database
	 * 
	 * @param assignment IAssignment to be removed
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return IAssignment that was removed, for chaining calls
	 */
	protected static IAssignment removeAssignment(final IAssignment assignment, final JdbcConnectionPool pool) {
		PreparedStatement assignmentStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			assignmentStatement = con.prepareStatement(Utilities.DELETE_ASGN);
			final String assignmentId = assignment.getID();
			
			Utilities.setValues(assignmentStatement, assignmentId);
			assignmentStatement.execute();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: removeAssignment: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("AssignmentTaskStorage: removeAssignment: attempting to roll back transaction",
					e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException(
							"AssignmentTaskStorage: removeAssignment: could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (assignmentStatement != null) {
					assignmentStatement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("AssignmentTaskStorage: removeAssignment: could not close resource", x);
			}
		}
		
		return assignment;
	}
	
	/**
	 * Update Assignment and clear and repopulate its associated Tasks Checks to see if the Template associated with the
	 * Assignment can still be found in the db
	 * 
	 * @param assignment Assignment to be updated
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Assignment that was updated, for chaining calls
	 * @throws StorageServiceException Thrown when the Assignment's associated Template cannot be found in the db
	 */
	protected static Assignment updateAssignment(final Assignment assignment, final JdbcConnectionPool pool)
			throws StorageServiceException {
		PreparedStatement assignmentStatement = null;
		PreparedStatement taskDeleteStatement = null;
		PreparedStatement taskInsertStatement = null;
		PreparedStatement templateStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			con.setAutoCommit(false);
			
			// Check to see that the template associated exists in the db
			templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATE_BY_ID);
			Utilities.setValues(templateStatement, assignment.getTemplate().getID());
			final ResultSet rs = templateStatement.executeQuery();
			int num = 0;
			while (rs.next()) {
				num++;
			}
			
			// If the associated template is not in the db, rollback and throw and exception
			if (num == 0) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("AssignmentTaskStorage: updateAssignment: "
						+ "could not roll back transaction", x);
				}
				
				throw new StorageServiceException("AssignmentTaskStorage: updateAssignment: "
					+ "Assignments's associated Template must be in the database.");
			}
			
			assignmentStatement = con.prepareStatement(Utilities.UPDATE_ASGN);
			final String assignmentId = assignment.getID();
			
			Utilities.setValues(assignmentStatement, assignment.getName(), assignment.getExpectedHours(), assignment
					.getDueDate().getTime(), assignment.getTemplate().getID(), assignmentId);
			assignmentStatement.execute();
			
			// Delete all template steps from before
			taskDeleteStatement = con.prepareStatement(Utilities.DELETE_TASKS_BY_ID);
			Utilities.setValues(taskDeleteStatement, assignmentId);
			taskDeleteStatement.execute();
			
			// Insert new template steps
			taskInsertStatement = con.prepareStatement(Utilities.INSERT_TASK);
			for (final ITask task : assignment.getTasks()) {
				Utilities.setValues(taskInsertStatement, assignmentId, task.getTaskID(), task.getName(),
						task.getPercentOfTotal(), task.getPercentComplete(), task.getPreferredTimeOfDay().name(),
						task.getSuggestedBlockLength());
				taskInsertStatement.addBatch();
			}
			taskInsertStatement.executeBatch();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: updateAssignment: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("AssignmentTaskStorage: updateAssignment: "
				+ "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("AssignmentTaskStorage: updateAssignment: "
						+ "could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (assignmentStatement != null) {
					assignmentStatement.close();
				}
				if (taskDeleteStatement != null) {
					taskDeleteStatement.close();
				}
				if (taskInsertStatement != null) {
					taskInsertStatement.close();
				}
				if (templateStatement != null) {
					templateStatement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities
						.printSQLException("AssignmentTaskStorage: updateAssignment: " + "could not close resource", x);
			}
		}
		
		return assignment;
	}
	
	/**
	 * Get the Assignment identified by the passed-in id value
	 * 
	 * @param toBeFoundId Id value of the Assignment to be retrieved
	 * @param templates Cache of templates for finding the associated Template
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Assignment that was found, or null if the Assignment was not found
	 */
	protected static Assignment getAssignment(final String toBeFoundId, final Cache<ITemplate> templates,
			final JdbcConnectionPool pool) {
		PreparedStatement assignmentStatement = null;
		PreparedStatement templateStatement = null;
		Connection con = null;
		Assignment result = null;
		ITemplate template = null;
		String templateId = "";
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			assignmentStatement = con.prepareStatement(Utilities.SELECT_ASGN_BY_ID);
			Utilities.setValues(assignmentStatement, toBeFoundId);
			final ResultSet asgnTaskResults = assignmentStatement.executeQuery();
			
			while (asgnTaskResults.next()) {
				// Getting all of the field for reconstructing the task object
				final String taskId = asgnTaskResults.getString("TASK_ID");
				final String taskName = asgnTaskResults.getString("TASK_NAME");
				final double taskPercentTotal = asgnTaskResults.getDouble("TASK_PERCENT_TOTAL");
				final double taskPercentComplete = asgnTaskResults.getDouble("TASK_PERCENT_COMPLETE");
				final String timeOfDay = asgnTaskResults.getString("TASK_TIME_OF_DAY");
				final TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay);
				final double taskSuggestedLength = asgnTaskResults.getDouble("TASK_SUGGESTED_LENGTH");
				
				// Get the necessary fields from assignment
				final String asgnId = asgnTaskResults.getString("ASSIGNMENT.ASGN_ID");
				
				final Task task = new Task(taskId, taskName, taskPercentTotal, asgnId, taskPercentComplete,
						taskTimeOfDay, taskSuggestedLength);
				
				// if the assignment hasn't been reconstructed yet
				if (result == null) {
					templateId = asgnTaskResults.getString("ASGN_TEMPLATE_ID");
					final String asgnName = asgnTaskResults.getString("ASGN_NAME");
					final Date asgnDueDate = new Date(asgnTaskResults.getLong("ASGN_DATE"));
					final int asgnExpectedHours = asgnTaskResults.getInt("ASGN_EXPECTED_HOURS");
					final ArrayList<ITask> asgnTaskList = new ArrayList<>();
					
					asgnTaskList.add(task);
					result = new Assignment(asgnId, asgnName, asgnDueDate, asgnExpectedHours, asgnTaskList);
				}
				// if the assignment has already been reconstructed, just add task to its task list
				else {
					result.addTask(task);
				}
			}
			
			/*
			 * Assignments are still missing a reference to their Template. We need to rebuilt Templates and all their
			 * TemplateSteps first
			 */
			
			templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATES_AND_STEPS_BY_ID);
			
			// Now add the appropriate template reference to each assignment
			if (templates.contains(templateId)) {
				template = templates.get(templateId);
			} else {
				Utilities.setValues(templateStatement, templateId);
				final ResultSet templateStepResults = templateStatement.executeQuery();
				
				while (templateStepResults.next()) {
					// Reconstructing the template step
					final String stepName = templateStepResults.getString("STEP_NAME");
					final double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
					final int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
					final String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
					final TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay);
					
					final String templId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");
					final TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber,
							stepTimeOfDay);
					
					if (template == null) {
						final String templateName = templateStepResults.getString("TEMPLATE_NAME");
						final double templateConsecutiveHours = templateStepResults
								.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
						final ArrayList<ITemplateStep> templateList = new ArrayList<>();
						templateList.add(step);
						
						template = new Template(templId, templateName, templateList, templateConsecutiveHours);
					} else {
						template.addStep(step);
					}
				}
				if (template != null) {
					templates.insert(templateId, template);
				}
			}
			if (template != null) {
				result.setTemplate(template);
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: getAssignmentById: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("AssignmentTaskStorage: getAssignmentById: could not retrieve assignments", e);
		}
		finally {
			try {
				if (assignmentStatement != null) {
					assignmentStatement.close();
				}
				if (templateStatement != null) {
					templateStatement.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("AssignmentTaskStorage: getAssignmentById: could not close resource", x);
			}
		}
		
		return result;
	}
	
	/**
	 * Retrieves all Assignments
	 * 
	 * @param templates Cache of templates for retrieving the associated Templates
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return List of all Assignments stored in the db
	 */
	protected static List<IAssignment> getAllAssignments(final Cache<ITemplate> templates, 
			final JdbcConnectionPool pool) {
		PreparedStatement assignmentStatement = null;
		PreparedStatement templateStatement = null;
		Connection con = null;
		final HashMap<String, Assignment> idToAssignment = new HashMap<>();
		final HashMap<String, List<Assignment>> templateIdToAssignmentList = new HashMap<>();
		final HashMap<String, Template> idToTemplate = new HashMap<>();
		final List<IAssignment> results = new ArrayList<>();
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			assignmentStatement = con.prepareStatement(Utilities.SELECT_ALL_ASGNS);
			final ResultSet asgnTaskResults = assignmentStatement.executeQuery();
			
			while (asgnTaskResults.next()) {
				// Getting all of the fields for reconstructing the task object
				final String taskId = asgnTaskResults.getString("TASK_ID");
				final String taskName = asgnTaskResults.getString("TASK_NAME");
				final double taskPercentTotal = asgnTaskResults.getDouble("TASK_PERCENT_TOTAL");
				final double taskPercentComplete = asgnTaskResults.getDouble("TASK_PERCENT_COMPLETE");
				final String timeOfDay = asgnTaskResults.getString("TASK_TIME_OF_DAY");
				final TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay);
				final double taskSuggestedLength = asgnTaskResults.getDouble("TASK_SUGGESTED_LENGTH");
				
				// Get the necessary fields from assignment
				final String asgnId = asgnTaskResults.getString("ASSIGNMENT.ASGN_ID");
				final String asgnTemplateId = asgnTaskResults.getString("ASGN_TEMPLATE_ID");
				
				Assignment asgn;
				final Task task = new Task(taskId, taskName, taskPercentTotal, asgnId, taskPercentComplete,
						taskTimeOfDay, taskSuggestedLength);
				
				// if the assignment hasn't been reconstructed yet
				if (!idToAssignment.containsKey(asgnId)) {
					final String asgnName = asgnTaskResults.getString("ASGN_NAME");
					final Date asgnDueDate = new Date(asgnTaskResults.getLong("ASGN_DATE"));
					final int asgnExpectedHours = asgnTaskResults.getInt("ASGN_EXPECTED_HOURS");
					final ArrayList<ITask> asgnTaskList = new ArrayList<>();
					
					asgnTaskList.add(task);
					asgn = new Assignment(asgnId, asgnName, asgnDueDate, asgnExpectedHours, asgnTaskList);
					idToAssignment.put(asgnId, asgn);
				}
				// if the assignment has already been reconstructed, just add task to its task list
				else {
					asgn = idToAssignment.get(asgnId);
					asgn.addTask(task);
				}
				
				if (templateIdToAssignmentList.containsKey(asgnTemplateId)) {
					templateIdToAssignmentList.get(asgnTemplateId).add(asgn);
				} else {
					final ArrayList<Assignment> asgnList = new ArrayList<>();
					asgnList.add(asgn);
					templateIdToAssignmentList.put(asgnTemplateId, asgnList);
				}
			}
			
			/*
			 * Assignments are still missing a reference to their Template. We need to rebuilt Templates and all their
			 * TemplateSteps first
			 */
			
			templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATES_AND_STEPS_BY_ID);
			
			// Now add the appropriate template reference to each assignment
			for (final Map.Entry<String, List<Assignment>> entry : templateIdToAssignmentList.entrySet()) {
				final String listTemplateId = entry.getKey();
				final List<Assignment> associatedAssignments = entry.getValue();
				
				if (templates.contains(listTemplateId)) {
					for (final Assignment a : associatedAssignments) {
						a.setTemplate(templates.get(listTemplateId));
					}
				} else {
					Utilities.setValues(templateStatement, listTemplateId);
					final ResultSet templateStepResults = templateStatement.executeQuery();
					
					while (templateStepResults.next()) {
						// Reconstructing the template step
						final String stepName = templateStepResults.getString("STEP_NAME");
						final double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
						final int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
						final String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
						final TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay);
						
						final String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");
						final TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber,
								stepTimeOfDay);
						
						if (!idToTemplate.containsKey(templateId)) {
							final String templateName = templateStepResults.getString("TEMPLATE_NAME");
							final double templateConsecutiveHours = templateStepResults
									.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
							final ArrayList<ITemplateStep> templateList = new ArrayList<>();
							templateList.add(step);
							
							final Template template = new Template(templateId, templateName, templateList,
									templateConsecutiveHours);
							idToTemplate.put(templateId, template);
							
							for (final Assignment a : associatedAssignments) {
								a.setTemplate(template);
							}
						} else {
							idToTemplate.get(templateId).addStep(step);
						}
					}
					
					templates.insert(listTemplateId, idToTemplate.get(listTemplateId));
				}
			}
		} catch (final ClassNotFoundException e) {
			Utilities
					.printException("AssignmentTaskStorage: getAllAssignmentsWithinRange: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("AssignmentTaskStorage: getAllAssignmentsWithinRange: "
				+ "could not retrieve assignments", e);
		}
		finally {
			try {
				if (assignmentStatement != null) {
					assignmentStatement.close();
				}
				if (templateStatement != null) {
					templateStatement.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("AssignmentTaskStorage: getAllAssignmentsWithinRange: "
					+ "could not close resource", x);
			}
		}
		
		results.addAll(idToAssignment.values());
		return results;
	}
	
	/**
	 * Retrieves all Assignments whose dueDate falls into the range specified, inclusive of the bounds
	 * 
	 * @param date1 Lower bound of the date range
	 * @param date2 Upper bound of the date range
	 * @param templates Cache of templates for retrieving the associated Templates
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return List of Assignments whose dueDate falls within the specified date range
	 */
	protected static List<IAssignment> getAllAssignmentsWithinRange(final Date date1, final Date date2,
			final Cache<ITemplate> templates, final JdbcConnectionPool pool) {
		PreparedStatement assignmentStatement = null;
		PreparedStatement templateStatement = null;
		Connection con = null;
		final HashMap<String, Assignment> idToAssignment = new HashMap<>();
		final HashMap<String, List<Assignment>> templateIdToAssignmentList = new HashMap<>();
		final HashMap<String, Template> idToTemplate = new HashMap<>();
		final List<IAssignment> results = new ArrayList<>();
		
		// Defensive programming in case the date1 is not earlier than date2
		final Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2;
		final Date later = (date2.compareTo(date1) > 0) ? date2 : date1;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			assignmentStatement = con.prepareStatement(Utilities.SELECT_ASGNS_TASKS_BY_DATE);
			Utilities.setValues(assignmentStatement, earlier.getTime(), later.getTime());
			final ResultSet asgnTaskResults = assignmentStatement.executeQuery();
			
			while (asgnTaskResults.next()) {
				// Getting all of the field for reconstructing the task object
				final String taskId = asgnTaskResults.getString("TASK_ID");
				final String taskName = asgnTaskResults.getString("TASK_NAME");
				final double taskPercentTotal = asgnTaskResults.getDouble("TASK_PERCENT_TOTAL");
				final double taskPercentComplete = asgnTaskResults.getDouble("TASK_PERCENT_COMPLETE");
				final String timeOfDay = asgnTaskResults.getString("TASK_TIME_OF_DAY");
				final TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay);
				final double taskSuggestedLength = asgnTaskResults.getDouble("TASK_SUGGESTED_LENGTH");
				
				// Get the necessary fields from assignment
				final String asgnId = asgnTaskResults.getString("ASSIGNMENT.ASGN_ID");
				final String asgnTemplateId = asgnTaskResults.getString("ASGN_TEMPLATE_ID");
				
				Assignment asgn;
				final Task task = new Task(taskId, taskName, taskPercentTotal, asgnId, taskPercentComplete,
						taskTimeOfDay, taskSuggestedLength);
				
				// if the assignment hasn't been reconstructed yet
				if (!idToAssignment.containsKey(asgnId)) {
					final String asgnName = asgnTaskResults.getString("ASGN_NAME");
					final Date asgnDueDate = new Date(asgnTaskResults.getLong("ASGN_DATE"));
					final int asgnExpectedHours = asgnTaskResults.getInt("ASGN_EXPECTED_HOURS");
					final ArrayList<ITask> asgnTaskList = new ArrayList<>();
					
					asgnTaskList.add(task);
					asgn = new Assignment(asgnId, asgnName, asgnDueDate, asgnExpectedHours, asgnTaskList);
					idToAssignment.put(asgnId, asgn);
				}
				// if the assignment has already been reconstructed, just add task to its task list
				else {
					asgn = idToAssignment.get(asgnId);
					asgn.addTask(task);
				}
				
				if (templateIdToAssignmentList.containsKey(asgnTemplateId)) {
					templateIdToAssignmentList.get(asgnTemplateId).add(asgn);
				} else {
					final ArrayList<Assignment> asgnList = new ArrayList<>();
					asgnList.add(asgn);
					templateIdToAssignmentList.put(asgnTemplateId, asgnList);
				}
			}
			
			/*
			 * Assignments are still missing a reference to their Template. We need to rebuilt Templates and all their
			 * TemplateSteps first
			 */
			
			templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATES_AND_STEPS_BY_ID);
			
			// Now add the appropriate template reference to each assignment
			for (final Map.Entry<String, List<Assignment>> entry : templateIdToAssignmentList.entrySet()) {
				final String listTemplateId = entry.getKey();
				final List<Assignment> associatedAssignments = entry.getValue();
				
				if (templates.contains(listTemplateId)) {
					for (final Assignment a : associatedAssignments) {
						a.setTemplate(templates.get(listTemplateId));
					}
				} else {
					Utilities.setValues(templateStatement, listTemplateId);
					final ResultSet templateStepResults = templateStatement.executeQuery();
					
					while (templateStepResults.next()) {
						// Reconstructing the template step
						final String stepName = templateStepResults.getString("STEP_NAME");
						final double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
						final int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
						final String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
						final TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay);
						
						final String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");
						final TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber,
								stepTimeOfDay);
						
						if (!idToTemplate.containsKey(templateId)) {
							final String templateName = templateStepResults.getString("TEMPLATE_NAME");
							final double templateConsecutiveHours = templateStepResults
									.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
							final ArrayList<ITemplateStep> templateList = new ArrayList<>();
							templateList.add(step);
							
							final Template template = new Template(templateId, templateName, templateList,
									templateConsecutiveHours);
							idToTemplate.put(templateId, template);
							
							for (final Assignment a : associatedAssignments) {
								a.setTemplate(template);
							}
						} else {
							idToTemplate.get(templateId).addStep(step);
						}
					}
					
					templates.insert(listTemplateId, idToTemplate.get(listTemplateId));
				}
			}
		} catch (final ClassNotFoundException e) {
			Utilities
					.printException("AssignmentTaskStorage: getAllAssignmentsWithinRange: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("AssignmentTaskStorage: getAllAssignmentsWithinRange: "
				+ "could not retrieve assignments", e);
		}
		finally {
			try {
				if (assignmentStatement != null) {
					assignmentStatement.close();
				}
				if (templateStatement != null) {
					templateStatement.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("AssignmentTaskStorage: getAllAssignmentsWithinRange: "
					+ "could not close resource", x);
			}
		}
		
		results.addAll(idToAssignment.values());
		return results;
	}
	
	/**
	 * Retrieves all the Tasks whose associated Assignment's dueDate falls within the range specified, inclusive of
	 * bounds.
	 * 
	 * @param date1 Lower bound of the date range
	 * @param date2 Upper bound of the date range
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return List of Tasks that fall within the date range specified
	 */
	protected static List<ITask> getAllTasksWithinRange(final Date date1, final Date date2,
			final JdbcConnectionPool pool) {
		PreparedStatement statement = null;
		Connection con = null;
		final ArrayList<ITask> results = new ArrayList<>();
		
		// Defensive programming in case the date1 is not earlier than date2
		final Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2;
		final Date later = (date2.compareTo(date1) > 0) ? date2 : date1;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			statement = con.prepareStatement(Utilities.SELECT_TASKS_BY_DATE);
			Utilities.setValues(statement, earlier.getTime(), later.getTime());
			final ResultSet taskResults = statement.executeQuery();
			
			while (taskResults.next()) {
				// Getting all of the field for reconstructing the task object
				final String taskId = taskResults.getString("TASK_ID");
				final String asgnId = taskResults.getString("ASGN_ID");
				final String taskName = taskResults.getString("TASK_NAME");
				final double taskPercentTotal = taskResults.getDouble("TASK_PERCENT_TOTAL");
				final double taskPercentComplete = taskResults.getDouble("TASK_PERCENT_COMPLETE");
				final String timeOfDay = taskResults.getString("TASK_TIME_OF_DAY");
				final TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay);
				final double taskSuggestedLength = taskResults.getDouble("TASK_SUGGESTED_LENGTH");
				
				results.add(new Task(taskId, taskName, taskPercentTotal, asgnId, taskPercentComplete, taskTimeOfDay,
						taskSuggestedLength));
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: getAllTasksWithinRange: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("AssignmentTaskStorage: getAllTasksWithinRange: "
				+ "could not retrieve assignments", e);
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
				Utilities.printSQLException("AssignmentTaskStorage: getAllTasksWithinRange: "
					+ "could not close resource", x);
			}
		}
		
		return results;
	}
}
