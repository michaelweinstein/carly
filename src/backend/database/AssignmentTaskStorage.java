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
import data.IAssignment;
import data.ITask;
import data.ITemplateStep;
import data.ITimeBlockable;
import data.Task;
import data.Template;
import data.TemplateStep;
import data.TimeOfDay;


public class AssignmentTaskStorage {
	
	protected static void buildTable(ArrayList<String> queries) {
        //Assignment table
        ArrayList<String> assignmentCols = new ArrayList<>();
        assignmentCols.add(StorageService.concatColumn("ASGN_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
        assignmentCols.add(StorageService.concatColumn("ASGN_NAME", "VARCHAR(255)"));
        assignmentCols.add(StorageService.concatColumn("ASGN_EXPECTED_HOURS", "INT"));
        assignmentCols.add(StorageService.concatColumn("ASGN_DATE", "BIGINT"));
        assignmentCols.add(StorageService.concatColumn("ASGN_TEMPLATE_ID", "VARCHAR(255)"));
        queries.add(Utilities.buildCreateString("ASSIGNMENT", assignmentCols));

        //Tasks table
        ArrayList<String> taskCols = new ArrayList<>();
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
	
	protected static synchronized IAssignment addAssignment(IAssignment assignment) throws StorageServiceException {
		PreparedStatement assignmentStatement = null;
		PreparedStatement taskStatement = null;
		PreparedStatement templateStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD); 
	    	
	        con.setAutoCommit(false);
	        assignmentStatement = con.prepareStatement(Utilities.INSERT_ASGN);
	        taskStatement = con.prepareStatement(Utilities.INSERT_TASK);
        	String assignmentId = assignment.getID(); 
        	
        	//insert assignment
        	System.out.println("INSERT_ASGN calling setValues()");
            Utilities.setValues(assignmentStatement, assignmentId, assignment.getName(), 
            		assignment.getExpectedHours(), assignment.getDueDate().getTime(), assignment.getTemplate().getID());
            assignmentStatement.execute();
            
            //insert associated tasks
            for (ITask task : assignment.getTasks()) {
            	System.out.println("INSERT_TASK calling setValues()");
            	Utilities.setValues(taskStatement, assignmentId, task.getTaskID(), task.getName(), 
            			task.getPercentOfTotal(), task.getPercentComplete(), 
            			task.getPreferredTimeOfDay().name(), task.getSuggestedBlockLength());
            	taskStatement.addBatch();
            }
            taskStatement.executeBatch();
            
            //Check to see that the template associated exists in the db
            templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATE_BY_ID); 
            Utilities.setValues(templateStatement, assignment.getTemplate().getID());
            ResultSet rs = templateStatement.executeQuery();
            int num = 0; 
            while (rs.next()) {
            	num++; 
            }
            
            //If the associated template is not in the db, rollback and throw and exception
            if (num == 0) {
            	try {
	                con.rollback();
	            } 
            	catch(SQLException x) {
	                Utilities.printSQLException("AssignmentTaskStorage: addAssignment: " +
	                		"could not roll back transaction", x);
	            }
            	
            	throw new StorageServiceException("AssignmentTaskStorage: addAssignment: " +
            			"Assignments's associated Template must be in the database."); 
            }
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: addAssignment: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("AssignmentTaskStorage: addAssignment: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } catch(SQLException x) {
	                Utilities.printSQLException("AssignmentTaskStorage: addAssignment: " +
	                		"could not roll back transaction", x);
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
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("AssignmentTaskStorage: addAssignment: could not close resource", x);
            }
	    }
	    
		return assignment; 
	}
	

	
	//TODO: Check to see if tasks are also deleted -- that is, if cascading deletes actually work.
	protected static synchronized IAssignment removeAssignment(IAssignment assignment) {
		PreparedStatement assignmentStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        con.setAutoCommit(false);
	        assignmentStatement = con.prepareStatement(Utilities.DELETE_ASGN);
        	String assignmentId = assignment.getID(); 
        	
        	//Tasksinsert assignment
            Utilities.setValues(assignmentStatement, assignmentId);
            assignmentStatement.execute();
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: removeAssignment: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("AssignmentTaskStorage: removeAssignment: attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } catch(SQLException x) {
	                Utilities.printSQLException("AssignmentTaskStorage: removeAssignment: could not roll back transaction", x);
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
                Utilities.printSQLException("AssignmentTaskStorage: removeAssignment: could not close resource", x);
            }
	    }
	    
		return assignment; 
	}
	
	protected static synchronized Assignment updateAssignment(Assignment assignment) {
		PreparedStatement assignmentStatement = null;
		PreparedStatement taskStatement = null; 
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        con.setAutoCommit(false);
	        assignmentStatement = con.prepareStatement(Utilities.UPDATE_ASGN);
        	String assignmentId = assignment.getID(); 
        	List<ITask> taskList = assignment.getTasks(); 
        	
        	//insert assignment
        	Utilities.setValues(assignmentStatement, assignment.getName(), 
	            		assignment.getExpectedHours(), assignment.getDueDate().getTime(), 
	            		assignment.getTemplate().getID(), assignmentId);
            assignmentStatement.execute();
            
            //merge all tasks
            taskStatement = con.prepareStatement(Utilities.MERGE_TASK);
            for (ITask task : taskList) {
            	Utilities.setValues(taskStatement, assignmentId, task.getTaskID(), task.getName(), 
            			task.getPercentComplete(), task.getPreferredTimeOfDay().name(), task.getSuggestedBlockLength());
            	taskStatement.addBatch();
            }
            taskStatement.executeBatch(); 
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: updateAssignment: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("AssignmentTaskStorage: updateAssignment: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } catch(SQLException x) {
	                Utilities.printSQLException("AssignmentTaskStorage: updateAssignment: " +
	                		"could not roll back transaction", x);
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
                Utilities.printSQLException("AssignmentTaskStorage: updateAssignment: " +
                		"could not close resource", x);
            }
	    }
	    
	    return assignment; 
	}
	
	protected static synchronized Assignment getAssignmentById(String toBeFoundId, Cache<Template> templates) {
		System.out.println("StorageService.getAssignmentById");
		PreparedStatement assignmentStatement = null;
		PreparedStatement templateStatement = null; 
	    Connection con = null;
	    Assignment result = null; 
	    Template template = null;
	    String templateId = "";
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	    	System.out.println("preparing statement!");
	        assignmentStatement = con.prepareStatement(Utilities.SELECT_ASGN_BY_ID); 
        	System.out.println("SELECT_ASGN_BY_ID calling setValues()");
	        Utilities.setValues(assignmentStatement, toBeFoundId);
        	ResultSet asgnTaskResults = assignmentStatement.executeQuery();

        	//DEBUG
        	ResultSet debugSet = asgnTaskResults; 
	        ResultSetMetaData rsmd = debugSet.getMetaData();
	        int columnCount = rsmd.getColumnCount();

	        System.out.println("AssignmentTaskStorage: getAssignmentById: asgn/task query column names are: ");
	        // The column count starts from 1
	        for (int i = 1; i < columnCount + 1; i++ ) {
	          String name = rsmd.getColumnName(i);
	          System.out.println("\t" + name);
	        }
		    //DEBUG
        	
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
        		
        		Task task = new Task(taskId, taskName, taskPercentTotal, asgnId, taskPercentComplete, 
        				taskTimeOfDay, taskSuggestedLength);
        		
        		//if the assignment hasn't been reconstructed yet
        		if (result == null) {
            		String asgnName = asgnTaskResults.getString("ASGN_NAME"); 
            		Date asgnDueDate = new Date(asgnTaskResults.getLong("ASGN_DATE")); 
            		int asgnExpectedHours = asgnTaskResults.getInt("ASGN_EXPECTED_HOURS");
            		ArrayList<ITask> asgnTaskList = new ArrayList<>(); 
            		
            		asgnTaskList.add(task);
            		result = new Assignment(asgnId, asgnName, asgnDueDate, asgnExpectedHours, asgnTaskList);
            		templateId = asgnTemplateId; 
        		}
        		//if the assignment has already been reconstructed, just add task to its task list
        		else {
        			result.addTask(task);
        		}
        	}
        	
        	/*
        	 * Assignments are still missing a reference to their Template.
        	 * We need to rebuilt Templates and all their TemplateSteps first
        	 */
        	
	        templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATES_AND_STEPS_BY_ID); 
	        
        	//Now add the appropriate template reference to each assignment 
    		if (templates.contains(templateId)) {
    			template = templates.get(templateId); 
    		}
    		else {
    			System.out.println("SELECT_TEMPLATE_AND_STEPS_BY_ID calling setValues()");
    			Utilities.setValues(templateStatement, templateId);
        		ResultSet templateStepResults =  templateStatement.executeQuery();
        		
        		//DEBUG
    	        rsmd = templateStepResults.getMetaData();
    	        columnCount = rsmd.getColumnCount();

    	        System.out.println("AssignmentTaskStorage: getAssignmentById: template/steps query column names are: ");
    	        // The column count starts from 1
    	        for (int i = 1; i < columnCount + 1; i++ ) {
    	          String name = rsmd.getColumnName(i);
    	          System.out.println("\t" + name);
    	        }
    		    //DEBUG
        		
    	        
        		while (templateStepResults.next()) {
        			System.out.println("AssignmentTaskStorage: getAssignmentById: template/steps whilte loop");
        			//Reconstructing the template step
            		String stepName = templateStepResults.getString("STEP_NAME");
            		double stepPercentTotal = templateStepResults.getDouble("STEP_PERCENT_TOTAL");
            		int stepStepNumber = templateStepResults.getInt("STEP_STEP_NUMBER");
            		int stepNumDays = templateStepResults.getInt("STEP_NUM_DAYS");
            		double stepHoursPerDay = templateStepResults.getDouble("STEP_HOURS_PER_DAY");
            		String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
            		TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
            		
            		String templId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");  
            		TemplateStep step = new TemplateStep(stepName, stepPercentTotal, stepStepNumber, 
            				stepNumDays, stepHoursPerDay, stepTimeOfDay);
            		
            		if (template == null) {
            			String templateName = templateStepResults.getString("TEMPLATE_NAME");
                		double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
                		ArrayList<ITemplateStep> templateList = new ArrayList<>();   
                		templateList.add(step); 
                		
                		template = new Template(templId, templateName, templateList, templateConsecutiveHours); 
                		result.setTemplate(template);
                		System.out.println("\tAssignmentTaskStorage: getAssignmentById: just set assignment's template: " + result.getTemplate().toString()); 
            		}
            		else {
            			template.addStep(step); 
            		}
        		}
        		
        		templates.insert(templateId, template); 
    		}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: getAssignmentById: db drive class not found", e);
		} 
	    catch (SQLException e) {
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
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("AssignmentTaskStorage: getAssignmentById: could not close resource", x);
            }
	    }
		
		return result; 
	}
	
	protected static synchronized List<Assignment> getAllAssignmentsWithinRange(Date date1, Date date2, Cache<Template> templates) {
		PreparedStatement assignmentStatement = null;
		PreparedStatement templateStatement = null; 
	    Connection con = null; 
	    HashMap<String,Assignment> idToAssignment = new HashMap<>();   
	    HashMap<String,List<Assignment>> templateIdToAssignmentList = new HashMap<>();
	    HashMap<String,Template> idToTemplate = new HashMap<>(); 
	    ArrayList<Assignment> results = new ArrayList<>(); 
	    
	    //Defensive programming in case the date1 is not earlier than date2
	    Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2; 
	    Date later = (date2.compareTo(date1) > 0) ? date2 : date1; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        assignmentStatement = con.prepareStatement(Utilities.SELECT_ASGNS_TASKS_BY_DATE); 
        	Utilities.setValues(assignmentStatement, earlier.getTime(), later.getTime());
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
        		if (!idToAssignment.containsKey(asgnId)) {
            		String asgnName = asgnTaskResults.getString("ASGN_NAME"); 
            		Date asgnDueDate = new Date(asgnTaskResults.getLong("ASGN_DATE")); 
            		int asgnExpectedHours = asgnTaskResults.getInt("ASGN_EXPECTED_HOURS");
            		ArrayList<ITask> asgnTaskList = new ArrayList<>(); 
            		
            		asgnTaskList.add(task);
            		asgn = new Assignment(asgnId, asgnName, asgnDueDate, asgnExpectedHours, asgnTaskList); 
            		idToAssignment.put(asgnId, asgn);
        		}
        		//if the assignment has already been reconstructed, just add task to its task list
        		else {
        			asgn = idToAssignment.get(asgnId);
        			asgn.addTask(task);
        		}
        		
        		if (templateIdToAssignmentList.containsKey(asgnTemplateId)) {
        			templateIdToAssignmentList.get(asgnTemplateId).add(asgn);
        		}
        		else {
        			ArrayList<Assignment> asgnList = new ArrayList<>(); 
        			asgnList.add(asgn); 
        			templateIdToAssignmentList.put(asgnTemplateId, asgnList); 
        		}
        	}
        	
        	/*
        	 * Assignments are still missing a reference to their Template.
        	 * We need to rebuilt Templates and all their TemplateSteps first
        	 */
        	
	        templateStatement = con.prepareStatement(Utilities.SELECT_TEMPLATES_AND_STEPS_BY_ID); 
	        
        	//Now add the appropriate template reference to each assignment 
        	for (Map.Entry<String, List<Assignment>> entry : templateIdToAssignmentList.entrySet()) {
        		String listTemplateId = entry.getKey(); 
        		List<Assignment> associatedAssignments = entry.getValue(); 
        		
        		if (templates.contains(listTemplateId)) {
        			for (Assignment a : associatedAssignments) {
            			a.setTemplate(templates.get(listTemplateId));
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
                		
                		if (!idToTemplate.containsKey(templateId)) {
                			String templateName = templateStepResults.getString("TEMPLATE_NAME");
                    		double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
                    		ArrayList<ITemplateStep> templateList = new ArrayList<>();   
                    		templateList.add(step); 
                    		
                    		Template template = new Template(templateId, templateName, templateList, templateConsecutiveHours); 
                    		idToTemplate.put(templateId, template);
                    		
                    		for (Assignment a : associatedAssignments) {
                    			a.setTemplate(template);
                    		}
                		}
                		else {
                			idToTemplate.get(templateId).addStep(step); 
                		}
            		}
            		
            		templates.insert(listTemplateId, idToTemplate.get(listTemplateId)); 
        		}
        	}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("AssignmentTaskStorage: getAllAssignmentsWithinRange: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("AssignmentTaskStorage: getAllAssignmentsWithinRange: " +
	        		"could not retrieve assignments", e);
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
                Utilities.printSQLException("AssignmentTaskStorage: getAllAssignmentsWithinRange: " +
                		"could not close resource", x);
            }
	    }
		
	    results.addAll(idToAssignment.values()); 
		return results; 
	}

	protected static synchronized List<ITask> getAllTasksWithinRange(Date date1, Date date2) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    ArrayList<ITask> results = new ArrayList<>(); 
	    
	    //Defensive programming in case the date1 is not earlier than date2
	    Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2; 
	    Date later = (date2.compareTo(date1) > 0) ? date2 : date1; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        statement = con.prepareStatement(Utilities.SELECT_TASKS_BY_DATE); 
        	Utilities.setValues(statement, earlier.getTime(), later.getTime());
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
			Utilities.printException("AssignmentTaskStorage: getAllTasksWithinRange: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("AssignmentTaskStorage: getAllTasksWithinRange: " +
	        		"could not retrieve assignments", e);
	    } 
	    finally {
	    	try {
	    		if (statement != null) {
		            statement.close();
		        }
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("AssignmentTaskStorage: getAllTasksWithinRange: " +
                		"could not close resource", x);
            }
	    }
		
		return results; 
	}
}
