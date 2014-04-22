package backend.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import data.AssignmentBlock;
import data.ITimeBlockable;
import data.Task;
import data.TimeOfDay;
import data.UnavailableBlock;


public class TimeBlockStorage {
	
	protected static void buildTable(ArrayList<String> queries) {
		//TimeBlock table 
        ArrayList<String> blockCols = new ArrayList<>();
        blockCols.add(StorageService.concatColumn("BLOCK_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
        blockCols.add(StorageService.concatColumn("TASK_ID", "VARCHAR(255)"));
        blockCols.add(StorageService.concatColumn("BLOCK_START", "BIGINT"));
        blockCols.add(StorageService.concatColumn("BLOCK_END", "BIGINT"));
        blockCols.add(StorageService.concatColumn("BLOCK_MOVABLE", "BOOLEAN"));
        queries.add(Utilities.buildCreateString("TIME_BLOCK", blockCols)); 
	}
	
	protected static synchronized List<UnavailableBlock> getAllUnavailableBlocksWithinRange(Date date1, Date date2) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    ArrayList<UnavailableBlock> results = new ArrayList<>(); 
	    
	    //Defensive programming in case the date1 is not earlier than date2
	    Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2; 
	    Date later = (date2.compareTo(date1) > 0) ? date2 : date1; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        statement = con.prepareStatement(Utilities.SELECT_UNAVAILABLE_BLOCKS_BY_DATE); 
        	Utilities.setValues(statement, earlier.getTime(), later.getTime());
        	ResultSet blockResults = statement.executeQuery();
        	
        	while (blockResults.next()) {
        		String blockId = blockResults.getString("BLOCK_ID");
        		String taskId = blockResults.getString("TASK_ID");
        		Date blockStart = new Date(blockResults.getLong("BLOCK_START"));
        		Date blockEnd = new Date(blockResults.getLong("BLOCK_END"));
        		boolean blockMovable = blockResults.getBoolean("BLOCK_MOVABLE");
        		
        		results.add(new UnavailableBlock(blockId, blockStart, blockEnd, null, blockMovable)); 
        	}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: getAllUnavailableBlocksWithinRange: " +
					"db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TimeBlockStorage: getAllUnavailableBlocksWithinRange: " +
	        		"could not retrieve unavailable blocks", e);
	    } 
	    finally {
	    	try {
	    		if (statement != null) {
		            statement.close();
		        }
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: getAllUnavailableBlocksWithinRange: " +
                		"could not close resource", x);
            }
	    }
		
		return results; 
	}
	
	protected static synchronized List<AssignmentBlock> getAllAssignmentBlocksWithinRange(Date date1, Date date2) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    ArrayList<AssignmentBlock> results = new ArrayList<>(); 
	    
	    //Defensive programming in case the date1 is not earlier than date2
	    Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2; 
	    Date later = (date2.compareTo(date1) > 0) ? date2 : date1; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			
	        statement = con.prepareStatement(Utilities.SELECT_ASSIGNMENT_BLOCKS_BY_DATE); 
        	Utilities.setValues(statement, earlier.getTime(), later.getTime());
        	ResultSet blockResults = statement.executeQuery();
        	
        	while (blockResults.next()) {
        		String blockId = blockResults.getString("BLOCK_ID");
        		String taskId = blockResults.getString("TIME_BLOCK.TASK_ID");
        		Date blockStart = new Date(blockResults.getLong("BLOCK_START"));
        		Date blockEnd = new Date(blockResults.getLong("BLOCK_END"));
        		boolean blockMovable = blockResults.getBoolean("BLOCK_MOVABLE");
        		
        		String asgnId = blockResults.getString("ASGN_ID");
        		String taskName = blockResults.getString("TASK_NAME");
        		double taskPercentTotal = blockResults.getDouble("TASK_PERCENT_TOTAL");
        		double taskPercentComplete = blockResults.getDouble("TASK_PERCENT_COMPLETE");
        		String timeOfDay = blockResults.getString("TASK_TIME_OF_DAY");
        		TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay); 
        		double taskSuggestedLength = blockResults.getDouble("TASK_SUGGESTED_LENGTH");
        		
        		Task task = new Task(taskId, taskName, taskPercentTotal, asgnId, taskPercentComplete, 
        				taskTimeOfDay, taskSuggestedLength);
        		results.add(new AssignmentBlock(blockId, blockStart, blockEnd, task, blockMovable)); 
        	}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: getAllAssignmentBlocksWithinRange: " +
					"db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TimeBlockStorage: getAllAssignmentBlocksWithinRange: " +
	        		"could not retrieve assignment blocks", e);
	    } 
	    finally {
	    	try {
	    		if (statement != null) {
		            statement.close();
		        }
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: getAllAssignmentBlocksWithinRange: " +
                		"could not close resource", x);
            }
	    }
		
		return results;
	}
	
	protected static synchronized void addTimeBlock(ITimeBlockable block) throws StorageServiceException {
		PreparedStatement blockStatement = null;
		PreparedStatement taskStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
	    	
	        con.setAutoCommit(false);
	        blockStatement = con.prepareStatement(Utilities.INSERT_TIME_BLOCK); 
            Utilities.setValues(blockStatement, block.getId(), block.getTaskId(), block.getStart().getTime(),
            		block.getEnd().getTime(), block.isMovable());
            blockStatement.execute();
            
            //Only perform this check for assignment blocks
            if (!block.getTaskId().matches("")) {
            	//Check to see that the task associated with the time block has been added to the db
                taskStatement = con.prepareStatement(Utilities.SELECT_TASK_BY_ID); 
                Utilities.setValues(taskStatement, block.getTaskId());
                ResultSet rs = taskStatement.executeQuery();
                int numTasks = 0; 
                while (rs.next()) {
                	numTasks++; 
                }
                
                //If the associated task is not in the db, rollback and throw and exception
                if (numTasks == 0) {
                	try {
    	                con.rollback();
    	            } 
                	catch(SQLException x) {
    	                Utilities.printSQLException("TimeBlockStorage: addTimeBlock: " +
    	                		"could not roll back transaction", x);
    	            }
                	
                	throw new StorageServiceException("TimeBlockStorage: addTimeBlock: " +
                			"TimeBlock's associated Task must be in the database."); 
                }
            }
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: addTimeBlock: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TimeBlockStorage: addTimeBlock: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } 
	            catch(SQLException x) {
	                Utilities.printSQLException("TimeBlockStorage: addTimeBlock: " +
	                		"could not roll back transaction", x);
	            }
	        }
	    } 
	    finally {
	    	try {
	    		if (blockStatement != null) {
		            blockStatement.close();
		        }
	    		if (taskStatement != null) {
		            taskStatement.close();
		        }
		        con.setAutoCommit(true);
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: addTimeBlock: could not close resource", x);
            }
	    } 
	}
	
	protected static synchronized void mergeAllTimeBlocks(List<ITimeBlockable> block) {
		
	}
	
	protected static synchronized ITimeBlockable updateTimeBlock(ITimeBlockable block) {
		return block; 
	}
	
	protected static synchronized ITimeBlockable removeTimeBlock(ITimeBlockable block) {
		return block; 
	}
}
