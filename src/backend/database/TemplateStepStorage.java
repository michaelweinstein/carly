package backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import data.ITemplate;
import data.ITemplateStep;
import data.Template;
import data.TemplateStep;
import data.TimeOfDay;


public class TemplateStepStorage {
	
	protected static void buildTable(ArrayList<String> queries) {
		//Template table
        ArrayList<String> templateCols = new ArrayList<>();
        templateCols.add(StorageService.concatColumn("TEMPLATE_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
        templateCols.add(StorageService.concatColumn("TEMPLATE_NAME", "VARCHAR(255)"));
        templateCols.add(StorageService.concatColumn("TEMPLATE_CONSECUTIVE_HOURS", "DOUBLE"));
        queries.add(Utilities.buildCreateString("TEMPLATE", templateCols));	
        
        //Template step table
        ArrayList<String> stepCols = new ArrayList<>();
        stepCols.add(StorageService.concatColumn("TEMPLATE_ID", 
        		"VARCHAR(255) NOT NULL"));
        stepCols.add(StorageService.concatColumn("FOREIGN KEY (TEMPLATE_ID)", 
        		"REFERENCES TEMPLATE (TEMPLATE_ID) ON DELETE CASCADE"));
        stepCols.add(StorageService.concatColumn("STEP_NAME", "VARCHAR(255)"));  
        stepCols.add(StorageService.concatColumn("STEP_PERCENT_TOTAL", "DOUBLE"));
        stepCols.add(StorageService.concatColumn("STEP_STEP_NUMBER", "INT"));
        stepCols.add(StorageService.concatColumn("STEP_NUM_DAYS", "INT"));
        stepCols.add(StorageService.concatColumn("STEP_HOURS_PER_DAY", "DOUBLE"));
        stepCols.add(StorageService.concatColumn("STEP_TIME_OF_DAY", "VARCHAR(255)"));
        stepCols.add(StorageService.concatColumn("PRIMARY KEY", "(TEMPLATE_ID, STEP_NAME)"));
        queries.add(Utilities.buildCreateString("TEMPLATE_STEP", stepCols));
	}
	
	protected static synchronized Template getTemplate(String id, Cache<Template> templates) {
		if (templates.contains(id)) {
			return templates.get(id); 
		}
		
		PreparedStatement statement = null; 
	    Connection con = null; 
	    Template template = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        statement = con.prepareStatement(Utilities.SELECT_TEMPLATES_AND_STEPS_BY_ID); 
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
        		
    		templates.insert(id, template); 
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
	
	protected static synchronized ITemplate addTemplate(ITemplate temp) throws StorageServiceException {
		if (temp.getAllSteps().size() == 0) {
			throw new StorageServiceException("TemplateStepStorage: addTemplate: " +
        			"Template must have at least one Template Step"); 
		}
		
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
        	
        	//insert assignment
            Utilities.setValues(templateStatement, templateId, temp.getName(), temp.getPreferredConsecutiveHours());
            templateStatement.execute();
                        
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
			Utilities.printException("StorageService: addTemplate: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("StorageService: addTemplate: attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } catch(SQLException x) {
	                Utilities.printSQLException("StorageService: addTemplate: could not roll back transaction", x);
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
	
	protected static synchronized ITemplate updateTemplate(ITemplate temp) {
		PreparedStatement templateStatement = null;
		PreparedStatement deleteStepStatement = null;
		PreparedStatement insertStepStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
	        con.setAutoCommit(false);

	        //Update the existing template, if any of the values have changed
	        templateStatement = con.prepareStatement(Utilities.UPDATE_TEMPLATE);
        	Utilities.setValues(templateStatement, temp.getName(), 
        			 temp.getPreferredConsecutiveHours(), temp.getID());
            templateStatement.execute();
            
            //Delete all template steps from before
            deleteStepStatement = con.prepareStatement(Utilities.DELETE_TEMPLATE_STEPS_BY_ID);
            Utilities.setValues(deleteStepStatement, temp.getID());
            deleteStepStatement.execute(); 
            
            //Insert new template steps
            insertStepStatement = con.prepareStatement(Utilities.INSERT_TEMPLATE_STEP);
            for (ITemplateStep step : temp.getAllSteps()) {
            	Utilities.setValues(insertStepStatement, temp.getID(), step.getName(), step.getPercentOfTotal(), step.getStepNumber(), 
            			step.getNumberOfDays(), step.getHoursPerDay(), step.getBestTimeToWork().name());
            	insertStepStatement.addBatch();
            }
            insertStepStatement.executeBatch(); 
            
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
	            } 
	            catch(SQLException x) {
	                Utilities.printSQLException("could not roll back transaction", x);
	            }
	        }
	    } 
	    finally {
	    	try {
	    		if (templateStatement != null) {
		            templateStatement.close();
		        }
	    		if (insertStepStatement != null) {
		            insertStepStatement.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("could not close resource", x);
            }
	    }
	    
	    return temp; 
	}
	
	/**
	 * 
	 * @return
	 */
	protected static synchronized List<ITemplate> getAllTemplates() {
		ArrayList<ITemplate> results = new ArrayList<>(); 
		HashMap<String,Template> _idToTemplate = new HashMap<>(); 
		PreparedStatement statement = null; 
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        statement = con.prepareStatement(Utilities.SELECT_ALL_TEMPLATES_AND_STEPS); 
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
}
