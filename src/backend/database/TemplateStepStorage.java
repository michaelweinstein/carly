package backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import data.ITemplate;
import data.ITemplateStep;
import data.Template;
import data.TemplateStep;
import data.TimeOfDay;


public class TemplateStepStorage {
	
	/**
	 * Builds the create table strings for the initialize method in StorageService
	 * 
	 * @param queries List of create tables queries for StorageService to execute
	 */
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
        stepCols.add(StorageService.concatColumn("STEP_TIME_OF_DAY", "VARCHAR(255)"));
        stepCols.add(StorageService.concatColumn("PRIMARY KEY", "(TEMPLATE_ID, STEP_NAME)"));
        queries.add(Utilities.buildCreateString("TEMPLATE_STEP", stepCols));
	}
	
	/**
	 * Get Template corresponding to the provided Id
	 * 
	 * @param id Id of the template to be found
	 * @param templates Cache of templates recently searched for
	 * @return Found template
	 */
	protected static synchronized ITemplate getTemplate(String id, Cache<ITemplate> templates) {
		if (templates.contains(id)) {
			return templates.get(id); 
		}
		
		PreparedStatement statement = null; 
	    Connection con = null; 
	    Template template = null; 
	    ArrayList<ArrayList<ITemplateStep>> listOfTaskLists = new ArrayList<>(); 
	    
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
        		String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
        		TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
        		
        		String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");  
        		TemplateStep step = new TemplateStep(stepName, stepPercentTotal, 
        				stepStepNumber, stepTimeOfDay);
        		
        		//If the template hasn't already been reconstructed
        		if (template == null) {
        			String templateName = templateStepResults.getString("TEMPLATE_NAME");
            		double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
            		ArrayList<ITemplateStep> templateList = new ArrayList<>();   
            		templateList.add(step); 
            		listOfTaskLists.add(templateList); 
            		
            		template = new Template(templateId, templateName, templateList, templateConsecutiveHours); 
        		}
        		//If the template has been reconstructed, add the reconstructed TemplateStep to its list
        		else {
        			template.addStep(step); 
        		}
    		}
        	
    		//Null check in case the template with the corresponding id is not found
    		if (template != null) {
    			templates.insert(id, template);
    			for (ArrayList<ITemplateStep> list : listOfTaskLists) {
    				Collections.sort(list, new Comparator<ITemplateStep>() {
						@Override
						public int compare(ITemplateStep arg0, ITemplateStep arg1) {
							int stepNum1 = arg0.getStepNumber();
							int stepNum2 = arg1.getStepNumber(); 
							
							if (stepNum1 > stepNum2) {
								return 1; 
							}
							else if (stepNum1 < stepNum2) {
								return -1; 
							}
							else {
								return 0;
							}
						}
    				});
    			}
    		}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: getTemplate: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TemplateStepStorage: getTemplate: "
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
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TemplateStepStorage: getTemplate: "
                	+ "could not close resource", x);
            }
	    }
		
		return template; 
	}
	
	/**
	 * Add a template to the database
	 * 
	 * @param temp Template to be added
	 * @return Template that was added, for chaining calls
	 * @throws StorageServiceException Thrown when the Template has zero TemplateSteps
	 */
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
            			step.getStepNumber(), step.getBestTimeToWork().name());
            	stepStatement.addBatch();
            }
            stepStatement.executeBatch();
                        
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: addTemplate: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TemplateStepStorage: addTemplate: attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } catch(SQLException x) {
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
	    	}
	    	catch(SQLException x) {
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
	 * @return Template that was updated, for chaining calls
	 * @throws StorageServiceException Thrown when the Template has zero TemplateSteps
	 */
	protected static synchronized ITemplate updateTemplate(ITemplate temp, Cache<ITemplate> templates) 
			throws StorageServiceException {
		if (temp.getAllSteps().size() == 0) {
			throw new StorageServiceException("TemplateStepStorage: updateTemplate: " +
        			"Template must have at least one Template Step"); 
		}
		
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
            	Utilities.setValues(insertStepStatement, temp.getID(), step.getName(), 
            			step.getPercentOfTotal(), step.getStepNumber(), step.getBestTimeToWork().name());
            	insertStepStatement.addBatch();
            }
            insertStepStatement.executeBatch(); 
            
            templates.insert(temp.getID(), temp); 
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: updateTemplate: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TemplateStepStorage: updateTemplate: "
	        	+ "attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } 
	            catch(SQLException x) {
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
	    		if (insertStepStatement != null) {
		            insertStepStatement.close();
		        }
		        con.setAutoCommit(true);
		        if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TemplateStepStorage: updateTemplate: could not close resource", x);
            }
	    }
	    
	    return temp; 
	}
	
	/**
	 * Remove a template from the database
	 * 
	 * @param temp Template to be removed
	 * @return Template that was removed, for chaining method calls
	 */
	protected static synchronized ITemplate removeTemplate(ITemplate temp) {
		PreparedStatement statement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
	    	
	        con.setAutoCommit(false);
	        statement = con.prepareStatement(Utilities.DELETE_TEMPLATE); 
            Utilities.setValues(statement, temp.getID());
            statement.execute();
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: removeTemplate: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TemplateStepStorage: removeTemplate: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } 
	            catch(SQLException x) {
	                Utilities.printSQLException("TemplateStepStorage: removeTemplate: " +
	                		"could not roll back transaction", x);
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
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TemplateStepStorage: removeTemplate: could not close resource", x);
            }
	    } 
		return temp; 
	}
	
	/**
	 * Get all templates stored in the database
	 * 
	 * @return List containing all templates stored in the database
	 */
	protected static synchronized List<ITemplate> getAllTemplates() {
		ArrayList<ITemplate> results = new ArrayList<>(); 
		HashMap<String,Template> idToTemplate = new HashMap<>(); 
		ArrayList<ArrayList<ITemplateStep>> listOfTaskLists = new ArrayList<>();
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
        		String timeOfDay = templateStepResults.getString("STEP_TIME_OF_DAY");
        		TimeOfDay stepTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
        		
        		String templateId = templateStepResults.getString("TEMPLATE.TEMPLATE_ID");  
        		TemplateStep step = new TemplateStep(stepName, stepPercentTotal, 
        				stepStepNumber, stepTimeOfDay);
        		Template template; 
        		
        		if (!idToTemplate.containsKey(templateId)) {
        			String templateName = templateStepResults.getString("TEMPLATE_NAME");
            		double templateConsecutiveHours = templateStepResults.getDouble("TEMPLATE_CONSECUTIVE_HOURS");
            		ArrayList<ITemplateStep> templateList = new ArrayList<>();   
            		templateList.add(step); 
            		listOfTaskLists.add(templateList);
            		
            		template = new Template(templateId, templateName, templateList, templateConsecutiveHours); 
            		idToTemplate.put(templateId, template);
        		}
        		else {
        			template = idToTemplate.get(templateId); 
        			template.addStep(step); 
        		}
    		}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TemplateStepStorage: getAllTemplates: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TemplateStepStorage: getAllTemplates: "
	        	+ "could not retrieve all templates", e);
	    } 
	    finally {
	    	try {
	    		if (statement != null) {
		            statement.close();
		        }
	    		if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TemplateStepStorage: getAllTemplates: "
                	+ "could not close resource", x);
            }
	    }
	    
	    //Sort the steps in the step list so that they are in the correct order
	    for (ArrayList<ITemplateStep> list : listOfTaskLists) {
			Collections.sort(list, new Comparator<ITemplateStep>() {
				@Override
				public int compare(ITemplateStep arg0, ITemplateStep arg1) {
					int stepNum1 = arg0.getStepNumber();
					int stepNum2 = arg1.getStepNumber(); 
					
					if (stepNum1 > stepNum2) {
						return 1; 
					}
					else if (stepNum1 < stepNum2) {
						return -1; 
					}
					else {
						return 0;
					}
				}
			});
	    }
		
	    results.addAll(idToTemplate.values()); 
		return results; 
	}
}
