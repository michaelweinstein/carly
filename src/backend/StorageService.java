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

import data.Assignment;
import data.IAssignment;
import data.ITask;
import data.ITemplate;
import data.ITemplateStep;
import data.Task;
import data.Template;
import data.TemplateStep;
import data.TimeOfDay;

/**
 * Handles the storage, retrieval and persistence of data for Carly
 * 
 * @author eb27
 */
public class StorageService {
	
//	private static Cache<IAssignment> _assignments;
//	private static Cache<ITask> _tasks; 
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
	        assignmentCols.add(concatColumn("ASGN_DATE", "DATE"));
	        assignmentCols.add(concatColumn("ASGN_TEMPLATE_ID", "VARCHAR(255)"));
	        queries.add(Utilities.buildCreateString("ASSIGNMENT", assignmentCols)); 
			
	        //Tasks table
	        ArrayList<String> taskCols = new ArrayList<>();
	        taskCols.add(concatColumn("ASGN_ID", 
	        		"VARCHAR(255) NOT NULL FOREIGN KEY REFERENCES ASSIGNMENT (ASGN_ID) ON DELETE CASCADE"));
	        taskCols.add(concatColumn("TASK_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
	        taskCols.add(concatColumn("TASK_NAME", "VARCHAR(255)"));
	        taskCols.add(concatColumn("TASK_PERCENT_TOTAL", "DOUBLE"));
	        taskCols.add(concatColumn("TASK_PERCENT_COMPLETE", "DOUBLE"));
	        taskCols.add(concatColumn("TASK_TIME_OF_DAY", "VARCHAR(255)")); 
	        taskCols.add(concatColumn("TASK_SUGGESTED_LENGTH", "DOUBLE"));
	        queries.add(Utilities.buildCreateString("TASK", taskCols)); 
	        
	        //Template table
	        ArrayList<String> templateCols = new ArrayList<>();
	        templateCols.add(concatColumn("TEMPLATE_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
	        templateCols.add(concatColumn("TEMPLATE_NAME", "VARCHAR(255)"));
	        templateCols.add(concatColumn("TEMPLATE_CONSECUTIVE_HOURS", "DOUBLE"));
	        queries.add(Utilities.buildCreateString("TEMPLATE", templateCols));	
	        
	        //Template step table
	        ArrayList<String> stepCols = new ArrayList<>();
	        stepCols.add(concatColumn("TEMPLATE_ID", 
	        		"VARCHAR(255) NOT NULL FOREIGN KEY REFERENCES TEMPLATE (TEMPLATE_ID) ON DELETE CASCADE"));
	        stepCols.add(concatColumn("STEP_NAME", "VARCHAR(255)"));  
	        stepCols.add(concatColumn("STEP_PERCENT_TOTAL", "DOUBLE"));
	        stepCols.add(concatColumn("STEP_STEP_NUMBER", "INT"));
	        stepCols.add(concatColumn("STEP_NUM_DAYS", "INT"));
	        stepCols.add(concatColumn("STEP_HOURS_PER_DAY", "DOUBLE"));
	        stepCols.add(concatColumn("STEP_TIME_OF_DAY", "VARCHAR(255)"));
	        stepCols.add(concatColumn("PRIMARY KEY", "(TEMPLATE_ID, STEP_NAME)"));
	        queries.add(Utilities.buildCreateString("TEMPLATE_STEP", stepCols));

//		    try (Statement stmt = con.createStatement()) {
//		    	for (String query: queries) {
//		    		stmt.addBatch(query);
//		    	}
//		    	stmt.executeBatch(); //TODO CAN YOU BATCH CREATE STATEMENTS? 
//		    } 
	        
//	        String query = "SHOW COLUMNS FROM ASSIGNMENT"; 
//
//		    try (Statement stmt = con.createStatement()) {
//	
//		        ResultSet rs = stmt.executeQuery(query);
//	
//		        System.out.println("Processing results."); 
//		        System.out.println(rs.toString()); 
//		    }
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
//		_assignments = new Cache<>();
//		_tasks = new Cache<>();
		_templates = new Cache<>();
	}
	
	/*
	 * CRUD and dynamic queries for Assignments 
	 */

	public static synchronized IAssignment addAssignment(IAssignment assignment) {
		PreparedStatement assignmentStatement = null;
		PreparedStatement taskStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        con.setAutoCommit(false);
	        assignmentStatement = con.prepareStatement(Utilities.INSERT_ASGN);
	        taskStatement = con.prepareStatement(Utilities.INSERT_TASK);
        	String assignmentId = assignment.getID(); 
        	
        	//insert assignment
            Utilities.setValues(assignmentStatement, assignmentId, assignment.getName(), 
            		assignment.getExpectedHours(), assignment.getDueDate(), assignment.getTemplate().getID());
            assignmentStatement.execute();
            
            //insert associated tasks
            for (ITask task : assignment.getTasks()) {
            	Utilities.setValues(taskStatement, assignmentId, task.getTaskID(), task.getName(), 
            			task.getPercentOfTotal(), task.getPercentComplete(), 
            			task.getPreferredTimeOfDay().name(), task.getSuggestedBlockLength());
            	taskStatement.addBatch();
            }
            taskStatement.executeBatch();
            
            //commit to the database
            con.commit();
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
	    		if (assignmentStatement != null) {
		            assignmentStatement.close();
		        }
	    		if (taskStatement != null) {
		            taskStatement.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
	    
		return assignment; 
	}
	
	public static synchronized int addAllAssignments(List<IAssignment> assignmentList) {
		PreparedStatement assignmentStatement = null;
		PreparedStatement taskStatement = null;
	    Connection con = null; 
	    int numSuccess = 0; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        con.setAutoCommit(false);
	        assignmentStatement = con.prepareStatement(Utilities.INSERT_ASGN);
	        taskStatement = con.prepareStatement(Utilities.INSERT_TASK);

	        for (IAssignment assignment : assignmentList) {
	        	String assignmentId = assignment.getID(); 
	        	
	        	//insert assignment
	            Utilities.setValues(assignmentStatement, assignmentId, assignment.getName(), 
	            		assignment.getExpectedHours(), assignment.getDueDate(), assignment.getTemplate().getID());
	            assignmentStatement.execute();
	            
	            //insert associated tasks
	            for (ITask task : assignment.getTasks()) {
	            	Utilities.setValues(taskStatement, assignmentId, task.getTaskID(), task.getName(), 
	            			task.getPercentOfTotal(), task.getPercentComplete(), 
	            			task.getPreferredTimeOfDay().name(), task.getSuggestedBlockLength());
	            	taskStatement.addBatch();
	            }
	            taskStatement.executeBatch();
	            
	            //TODO: merge for template and template steps
	            
	            //commit to the database and update counter if successful
	            con.commit();
	            numSuccess++; 
	        }
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
	    		if (assignmentStatement != null) {
		            assignmentStatement.close();
		        }
	    		if (taskStatement != null) {
		            taskStatement.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
		
		return numSuccess; 
	}
	
	//TODO: Check to see if tasks are also deleted -- that is, if cascading deletes actually work.
	public static synchronized IAssignment removeAssignment(IAssignment assignment) {
		PreparedStatement assignmentStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        con.setAutoCommit(false);
	        assignmentStatement = con.prepareStatement(Utilities.DELETE_ASGN);
        	String assignmentId = assignment.getID(); 
        	
        	//insert assignment
            Utilities.setValues(assignmentStatement, assignmentId);
            assignmentStatement.execute();
            
            //commit to the database
            con.commit();
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
	    		if (assignmentStatement != null) {
		            assignmentStatement.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
	    
		return assignment; 
	}
	
	public static synchronized IAssignment updateAssignment(IAssignment assignment) {
		PreparedStatement assignmentStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        con.setAutoCommit(false);
	        assignmentStatement = con.prepareStatement(Utilities.UPDATE_ASGN);
        	String assignmentId = assignment.getID(); 
        	
        	//insert assignment
        	 Utilities.setValues(assignmentStatement, assignment.getName(), 
	            		assignment.getExpectedHours(), assignment.getDueDate(), 
	            		assignment.getTemplate().getID(), assignmentId);
            assignmentStatement.execute();
            
            //commit to the database
            con.commit();
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
	    		if (assignmentStatement != null) {
		            assignmentStatement.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
	    
	    return assignment; 
	}
	
	public static synchronized List<IAssignment> getAllAssignmentsWithinRange(Date date1, Date date2) {
		PreparedStatement assignmentStatement = null;
		PreparedStatement templateStatement = null; 
	    Connection con = null; 
	    HashMap<String,Assignment> _idToAssignment = new HashMap<>();   
	    HashMap<String,List<Assignment>> _templateIdToAssignmentList = new HashMap<>();
	    HashMap<String,Template> _idToTemplate = new HashMap<>(); 
	    ArrayList<IAssignment> results = new ArrayList<>(); 
	    
	    //Defensive programming in case the date1 is not earlier than date2
	    Date earlierDate = (date1.compareTo(date2) < 0) ? date1 : date2; 
	    Date laterDate = (date2.compareTo(date1) > 0) ? date2 : date1; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        assignmentStatement = con.prepareStatement(Utilities.SELECT_ASGN_TASKS_BY_DATE); 
        	Utilities.setValues(assignmentStatement, earlierDate, laterDate);
        	ResultSet asgnTaskResults = assignmentStatement.executeQuery();

        	while (asgnTaskResults.next()) {
        		//Getting all of the field for reconstructing the task object
        		String taskId = asgnTaskResults.getString("TASK_ID");
        		String taskName = asgnTaskResults.getString("TASK_NAME");
        		double taskPercentTotal = asgnTaskResults.getDouble("TASK_PERCENT_TOTAL");
        		double taskPercentComplete = asgnTaskResults.getDouble("TASK_PERCENT_COMPLETE");
        		String timeOfDay = asgnTaskResults.getString("TASK_TIME_OF_DAY");
        		TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
        		double taskSuggestedLength = asgnTaskResults.getDouble("TASK_SUGGESTED_LENGTH");
        		
        		//Get the necessary fields from assignment
        		String asgnId = asgnTaskResults.getString("ASSIGNMENT.ASGN_ID"); 
        		String asgnTemplateId = asgnTaskResults.getString("ASGN_TEMPLATE_ID");
        		
        		Assignment asgn;
        		Task task = new Task(taskId, taskName, taskPercentTotal, asgnId, taskPercentComplete, 
        				taskTimeOfDay, taskSuggestedLength);
        		
        		//if the assignment hasn't been reconstructed yet
        		if (!_idToAssignment.containsKey(asgnId)) {
            		String asgnName = asgnTaskResults.getString("ASGN_NAME"); 
            		Date asgnDueDate = asgnTaskResults.getDate("ASGN_DATE"); 
            		int asgnExpectedHours = asgnTaskResults.getInt("ASGN_EXPECTED_HOURS");
            		ArrayList<ITask> asgnTaskList = new ArrayList<>(); 
            		
            		asgnTaskList.add(task);
            		asgn = new Assignment(asgnId, asgnName, asgnDueDate, asgnExpectedHours, asgnTaskList); 
            		_idToAssignment.put(asgnId, asgn);
        		}
        		//if the assignment has already been reconstructed, just add task to its task list
        		else {
        			asgn = _idToAssignment.get(asgnId);
        			asgn.addTask(task);
        		}
        		
        		if (_templateIdToAssignmentList.containsKey(asgnTemplateId)) {
        			_templateIdToAssignmentList.get(asgnTemplateId).add(asgn);
        		}
        		else {
        			ArrayList<Assignment> asgnList = new ArrayList<>(); 
        			asgnList.add(asgn); 
        			_templateIdToAssignmentList.put(asgnTemplateId, asgnList); 
        		}
        	}
        	
        	/*
        	 * Assignments are still missing a reference to their Template.
        	 * We need to rebuilt Templates and all their TemplateSteps first
        	 */
        	
	        templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATE_AND_STEPS_BY_ID); 
	        
        	//Now add the appropriate template reference to each assignment 
        	for (Map.Entry<String, List<Assignment>> entry : _templateIdToAssignmentList.entrySet()) {
        		String listTemplateId = entry.getKey(); 
        		List<Assignment> associatedAssignments = entry.getValue(); 
        		
        		if (_templates.contains(listTemplateId)) {
        			for (Assignment a : associatedAssignments) {
            			a.setTemplate(_templates.get(listTemplateId));
            		}
        		}
        		else {
        			Utilities.setValues(templateStatement, listTemplateId);
            		ResultSet templateStepResults =  templateStatement.executeQuery();
            		
            		while (templateStepResults.next()) {
            			//Reconstructing the template step
                		String stepName = templateStepResults.getString("STEP_NAME");
                		double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
                		int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
                		int stepNumDays = templateStepResults.getInt("STEP_NUM_DAYS");
                		double stepHoursPerDay = templateStepResults.getDouble("STEP_HOURS_PER_DAY");
                		String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
                		TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
                		
                		String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");  
                		TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber, 
                				stepNumDays, stepHoursPerDay, stepTimeOfDay);
                		
                		if (!_idToTemplate.containsKey(templateId)) {
                			String templateName = templateStepResults.getString("TEMPLATE_NAME");
                    		double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
                    		ArrayList<ITemplateStep> templateList = new ArrayList<>();   
                    		templateList.add(step); 
                    		
                    		Template template = new Template(templateId, templateName, templateList, templateConsecutiveHours); 
                    		_idToTemplate.put(templateId, template);
                    		
                    		for (Assignment a : associatedAssignments) {
                    			a.setTemplate(template);
                    		}
                		}
                		else {
                			_idToTemplate.get(templateId).addStep(step); 
                		}
            		}
            		
            		_templates.insert(listTemplateId, _idToTemplate.get(listTemplateId)); 
        		}
        	}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("could not retrieve assignments", e);
	    } 
	    finally {
	    	try {
	    		if (assignmentStatement != null) {
		            assignmentStatement.close();
		        }
	    		if (templateStatement != null) {
		            templateStatement.close();
		        }
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
		
	    results.addAll(_idToAssignment.values()); 
		return results; 
	}

	public static synchronized List<ITask> getAllTasksWithinRange(Date date1, Date date2) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    ArrayList<ITask> results = new ArrayList<>(); 
	    
	    //Defensive programming in case the date1 is not earlier than date2
	    Date earlierDate = (date1.compareTo(date2) < 0) ? date1 : date2; 
	    Date laterDate = (date2.compareTo(date1) > 0) ? date2 : date1; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        statement = con.prepareStatement(Utilities.SELECT_TASKS_BY_DATE); 
        	Utilities.setValues(statement, earlierDate, laterDate);
        	ResultSet taskResults = statement.executeQuery();
        	
        	while (taskResults.next()) {
        		//Getting all of the field for reconstructing the task object
        		String taskId = taskResults.getString("TASK_ID");
        		String asgnId = taskResults.getString("ASGN_ID");
        		String taskName = taskResults.getString("TASK_NAME");
        		double taskPercentTotal = taskResults.getDouble("TASK_PERCENT_TOTAL");
        		double taskPercentComplete = taskResults.getDouble("TASK_PERCENT_COMPLETE");
        		String timeOfDay = taskResults.getString("TASK_TIME_OF_DAY");
        		TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
        		double taskSuggestedLength = taskResults.getDouble("TASK_SUGGESTED_LENGTH");
        		
        		results.add(new Task(taskId, taskName, taskPercentTotal, asgnId, taskPercentComplete, 
        				taskTimeOfDay, taskSuggestedLength));
        	}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("could not retrieve assignments", e);
	    } 
	    finally {
	    	try {
	    		if (statement != null) {
		            statement.close();
		        }
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
		
		return results; 
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
		if (_templates.contains(id)) {
			return _templates.get(id); 
		}
		
		PreparedStatement statement = null; 
	    Connection con = null; 
	    Template template = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        statement = con.prepareStatement(Utilities.SELECT_TEMPLATE_AND_STEPS_BY_ID); 
        	Utilities.setValues(statement, id);
    		ResultSet templateStepResults =  statement.executeQuery();
    		
    		while (templateStepResults.next()) {
    			//Reconstructing the template step
        		String stepName = templateStepResults.getString("STEP_NAME");
        		double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
        		int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
        		int stepNumDays = templateStepResults.getInt("STEP_NUM_DAYS");
        		double stepHoursPerDay = templateStepResults.getDouble("STEP_HOURS_PER_DAY");
        		String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
        		TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
        		
        		String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");  
        		TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber, 
        				stepNumDays, stepHoursPerDay, stepTimeOfDay);
        		
        		if (template == null) {
        			String templateName = templateStepResults.getString("TEMPLATE_NAME");
            		double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
            		ArrayList<ITemplateStep> templateList = new ArrayList<>();   
            		templateList.add(step); 
            		
            		template = new Template(templateId, templateName, templateList, templateConsecutiveHours); 
        		}
        		else {
        			template.addStep(step); 
        		}
    		}
        		
    		_templates.insert(id, template); 
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("could not retrieve assignments", e);
	    } 
	    finally {
	    	try {
	    		if (statement != null) {
		            statement.close();
		        }
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
		
		return template; 
	}
	
	public static synchronized ITemplate addTemplate(ITemplate temp) {
		PreparedStatement templateStatement = null;
		PreparedStatement stepStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        con.setAutoCommit(false);
	        templateStatement = con.prepareStatement(Utilities.INSERT_TEMPLATE);
	        stepStatement = con.prepareStatement(Utilities.INSERT_TEMPLATE_STEP);
        	String templateId = temp.getID(); 
        	
//        	"INSERT INTO TEMPLATE " +
//			"(TEMPLATE_ID, TEMPLATE_NAME, TEMPLATE_CONSECUTIVE_HOURS) " + 
//			"VALUES (?, ?, ?) ";
        	
        	//insert assignment
            Utilities.setValues(templateStatement, templateId, temp.getName(), temp.getPreferredConsecutiveHours());
            templateStatement.execute();
            
//            "INSERT INTO TEMPLATE_STEP " +
//			"(TEMPLATE_ID, STEP_NAME, STEP_PERCENT_TOTAL, STEP_STEP_NUMBER, _STEP_NUM_DAYS, " +
//			"STEP_HOURS_PER_DAY, STEP_TIME_OF_DAY) " + 
//			"VALUES (?, ?, ?, ?, ?, ?, ?) ";
            
            //insert associated tasks
            for (ITemplateStep step : temp.getAllSteps()) {
            	Utilities.setValues(stepStatement, templateId, step.getName(), step.getPercentOfTotal(), 
            			step.getStepNumber(), step.getNumberOfDays(), step.getHoursPerDay(), step.getBestTimeToWork().name());
            	stepStatement.addBatch();
            }
            stepStatement.executeBatch();
            
            //commit to the database
            con.commit();
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
	    		if (templateStatement != null) {
		            templateStatement.close();
		        }
	    		if (stepStatement != null) {
		            stepStatement.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
	    
		return temp; 
	}
	
	public static synchronized ITemplate updateTemplate(ITemplate temp) {
		PreparedStatement templateStatement = null;
		PreparedStatement stepStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
	        con.setAutoCommit(false);

	        templateStatement = con.prepareStatement(Utilities.UPDATE_TEMPLATE);
        	Utilities.setValues(templateStatement, temp.getName(), 
        			 temp.getPreferredConsecutiveHours(), temp.getID());
            templateStatement.execute();
            
            stepStatement = con.prepareStatement(Utilities.MERGE_TEMPLATE_STEP);
            for (ITemplateStep step : temp.getAllSteps()) {
            	Utilities.setValues(stepStatement, temp.getID(), step.getName(), step.getPercentOfTotal(), step.getStepNumber(), 
            			step.getNumberOfDays(), step.getHoursPerDay(), step.getBestTimeToWork().name());
            	stepStatement.addBatch();
            }
            stepStatement.executeBatch(); 
            
            //commit to the database
            con.commit();
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
	    		if (templateStatement != null) {
		            templateStatement.close();
		        }
	    		if (stepStatement != null) {
		            stepStatement.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
	    
	    return temp; 
	}
	
	public static synchronized List<ITemplate> getAllTemplates() {
		ArrayList<ITemplate> results = new ArrayList<>(); 
		HashMap<String,Template> _idToTemplate = new HashMap<>(); 
		PreparedStatement statement = null; 
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        statement = con.prepareStatement(Utilities.SELECT_ALL_TEMPLATE_AND_STEPS); 
    		ResultSet templateStepResults =  statement.executeQuery();
    		
    		while (templateStepResults.next()) {
    			//Reconstructing the template step
        		String stepName = templateStepResults.getString("STEP_NAME");
        		double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
        		int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
        		int stepNumDays = templateStepResults.getInt("STEP_NUM_DAYS");
        		double stepHoursPerDay = templateStepResults.getDouble("STEP_HOURS_PER_DAY");
        		String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
        		TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
        		
        		String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");  
        		TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber, 
        				stepNumDays, stepHoursPerDay, stepTimeOfDay);
        		Template template; 
        		
        		if (!_idToTemplate.containsKey(templateId)) {
        			String templateName = templateStepResults.getString("TEMPLATE_NAME");
            		double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
            		ArrayList<ITemplateStep> templateList = new ArrayList<>();   
            		templateList.add(step); 
            		
            		template = new Template(templateId, templateName, templateList, templateConsecutiveHours); 
            		_idToTemplate.put(templateId, template);
        		}
        		else {
        			template = _idToTemplate.get(templateId); 
        			template.addStep(step); 
        		}
    		}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("could not retrieve assignments", e);
	    } 
	    finally {
	    	try {
	    		if (statement != null) {
		            statement.close();
		        }
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
		
	    results.addAll(_idToTemplate.values()); 
		return results; 
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
