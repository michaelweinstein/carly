package backend.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;

import data.AssignmentBlock;
import data.ITimeBlockable;
import data.Task;
import data.TimeOfDay;
import data.UnavailableBlock;


public class TimeBlockStorage {
	
	/**
	 * Builds the create table string and returns to Storage Service
	 * 
	 * @param queries list of create table queries to execute by initialize in StorageService
	 */
	protected static void buildTable(ArrayList<String> queries) {
		//TimeBlock table 
        ArrayList<String> blockCols = new ArrayList<>();
        blockCols.add(StorageService.concatColumn("BLOCK_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
        blockCols.add(StorageService.concatColumn("TASK_ID", "VARCHAR(255)"));
        blockCols.add(StorageService.concatColumn("BLOCK_START", "BIGINT"));
        blockCols.add(StorageService.concatColumn("BLOCK_END", "BIGINT"));
        blockCols.add(StorageService.concatColumn("BLOCK_MOVABLE", "BOOLEAN"));
        blockCols.add(StorageService.concatColumn("BLOCK_DEFAULT", "BOOLEAN"));
        queries.add(Utilities.buildCreateString("TIME_BLOCK", blockCols)); 
	}
	
	/**
	 * Gets all Unavailable Blocks within the specified range
	 * 
	 * @param date1 Lower bound for the date range
	 * @param date2 Upper bound for the date range
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return List of all the blocks that fall COMPLETELY within these bounds 
	 */
	protected static List<UnavailableBlock> getAllUnavailableBlocksWithinRange(
			Date date1, Date date2, JdbcConnectionPool pool) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    ArrayList<UnavailableBlock> results = new ArrayList<>(); 
	    
	    //Defensive programming in case the date1 is not earlier than date2
	    Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2; 
	    Date later = (date2.compareTo(date1) > 0) ? date2 : date1; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
			
	        statement = con.prepareStatement(Utilities.SELECT_UNAVAILABLE_BLOCKS_BY_DATE); 
        	Utilities.setValues(statement, earlier.getTime(), later.getTime(),
        			earlier.getTime(), later.getTime(),
        			earlier.getTime(), later.getTime());
        	ResultSet blockResults = statement.executeQuery();
        	
        	while (blockResults.next()) {
        		String blockId = blockResults.getString("BLOCK_ID");
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
	    		if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: getAllUnavailableBlocksWithinRange: " +
                		"could not close resource", x);
            }
	    }
		
		return results; 
	}
	
	/**
	 * Gets all Assignment Blocks within the specified range
	 * 
	 * @param date1 Lower bound for the date range
	 * @param date2 Upper bound for the date range
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return List of all the blocks that fall COMPLETELY within these bounds 
	 */
	protected static List<AssignmentBlock> getAllAssignmentBlocksWithinRange(
			Date date1, Date date2, JdbcConnectionPool pool) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    ArrayList<AssignmentBlock> results = new ArrayList<>(); 
	    
	    //Defensive programming in case the date1 is not earlier than date2
	    Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2; 
	    Date later = (date2.compareTo(date1) > 0) ? date2 : date1; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
			
	        statement = con.prepareStatement(Utilities.SELECT_ASSIGNMENT_BLOCKS_BY_DATE); 
        	Utilities.setValues(statement, earlier.getTime(), later.getTime(), 
        			earlier.getTime(), later.getTime(), 
        			earlier.getTime(), later.getTime());
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
	    		if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: getAllAssignmentBlocksWithinRange: " +
                		"could not close resource", x);
            }
	    }
		
		return results;
	}
	
	/**
	 * Gets an Assignment Block
	 * 
	 * @param blockId Id of the Assignment Block in question 
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return AssignmentBlock corresponding to the given id 
	 */
	protected static AssignmentBlock getAssignmentBlock(String blockId, JdbcConnectionPool pool) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    AssignmentBlock block = null; 
	    	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
			
	        statement = con.prepareStatement(Utilities.SELECT_ASSIGNMENT_BLOCK_BY_ID); 
        	Utilities.setValues(statement, blockId);
        	ResultSet blockResults = statement.executeQuery();
        	
        	//Should only have one result since the blockId is a unique primary key
        	blockResults.next();
        	
        	String id = blockResults.getString("BLOCK_ID");
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
    		
			block = new AssignmentBlock(id, blockStart, blockEnd, task, blockMovable);
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: getAssignmentBlock: " +
					"db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TimeBlockStorage: getAssignmentBlock: " +
	        		"could not retrieve time block", e);
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
                Utilities.printSQLException("TimeBlockStorage: getAssignmentBlock: " +
                		"could not close resource", x);
            }
	    }
		
		return block;
	}
	
	/**
	 * Get an Unavailable Block
	 * 
	 * @param blockId Block id of the unavailable block
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return UnavailableBlock found corresponding to the given id 
	 */
	protected static UnavailableBlock getUnavailableBlock(String blockId, JdbcConnectionPool pool) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    UnavailableBlock block = null; 
	    	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
			
	        statement = con.prepareStatement(Utilities.SELECT_UNAVAILABLE_BLOCK_BY_ID); 
        	Utilities.setValues(statement, blockId);
        	ResultSet blockResults = statement.executeQuery();
        	
        	//Should only have one result since the blockId is a unique primary key
        	blockResults.next();
        	
        	String id = blockResults.getString("BLOCK_ID");
    		Date blockStart = new Date(blockResults.getLong("BLOCK_START"));
    		Date blockEnd = new Date(blockResults.getLong("BLOCK_END"));
    		boolean blockMovable = blockResults.getBoolean("BLOCK_MOVABLE");
    		
			block = new UnavailableBlock(id, blockStart, blockEnd, null, blockMovable);
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: getUnavailableBlock: " +
					"db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TimeBlockStorage: getUnavailableBlock: " +
	        		"could not retrieve time block", e);
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
                Utilities.printSQLException("TimeBlockStorage: getUnavailableBlock: " +
                		"could not close resource", x);
            }
	    }
		
		return block;
	}
	
	/**
	 * Adds a Time Block
	 * 
	 * @param block Block to be stored in the database
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @throws StorageServiceException When the TimeBlock's associated Task is not in the database 
	 */
	protected static void addTimeBlock(ITimeBlockable block, JdbcConnectionPool pool) 
			throws StorageServiceException {
		PreparedStatement blockStatement = null;
		PreparedStatement taskStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
	    	
	        con.setAutoCommit(false);
	        blockStatement = con.prepareStatement(Utilities.INSERT_TIME_BLOCK); 
            Utilities.setValues(blockStatement, block.getId(), block.getTaskId(), block.getStart().getTime(),
            		block.getEnd().getTime(), block.isMovable(), false);
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
		        if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: addTimeBlock: could not close resource", x);
            }
	    } 
	}
	
	/**
	 * Adds VALID TimeBlocks if they don't already exist in the database
	 * Updates any TimeBlocks that already have been stored in the database
	 * 
	 * @param blockList List of blocks to to be added to or updated in the database
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return A list of INVALID TimeBlocks (that is, those whose associated Task cannot be found in the database) that were NOT added/updated
	 */
	protected static List<ITimeBlockable> mergeAllTimeBlocks(List<ITimeBlockable> blockList, JdbcConnectionPool pool) {
		List<ITimeBlockable> blocksNotAdded = new ArrayList<>();
		List<ITimeBlockable> blocksToAdd = new ArrayList<>();
		PreparedStatement blockStatement = null;
		PreparedStatement taskStatement = null;
	    Connection con = null;
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
	    	
	        con.setAutoCommit(false);
	        
	        for (ITimeBlockable block : blockList) {
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
	                	blocksNotAdded.add(block);  
	                }
	                else {
	                	blocksToAdd.add(block); 
	                }
	            }
	            else {
	            	blocksToAdd.add(block);
	            }
	        }
	        
	        for (ITimeBlockable block: blocksToAdd) {
	        	blockStatement = con.prepareStatement(Utilities.MERGE_TIME_BLOCK); 
	            Utilities.setValues(blockStatement, block.getId(), block.getTaskId(), block.getStart().getTime(),
	            		block.getEnd().getTime(), block.isMovable(), false);
	            blockStatement.execute();
	        }
	        
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: mergeAllTimeBlocks: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TimeBlockStorage: mergeAllTimeBlocks: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } 
	            catch(SQLException x) {
	                Utilities.printSQLException("TimeBlockStorage: mergeAllTimeBlocks: " +
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
		        if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: mergeAllTimeBlocks: could not close resource", x);
            }
	    }
	    
	    return blocksNotAdded; 
	}
	
	/**
	 * Update TimeBlock with new start date, end date and associated task values
	 * 
	 * @param block Updated block
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Block that was passed in, for chaining calls
	 * @throws StorageServiceException Thrown when the TimeBlock's associated Task cannot be found in the database
	 */
	protected static ITimeBlockable updateTimeBlock(ITimeBlockable block, JdbcConnectionPool pool) 
			throws StorageServiceException {
		PreparedStatement blockStatement = null;
		PreparedStatement taskStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
	    	
	        con.setAutoCommit(false);
	        
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
    	                Utilities.printSQLException("TimeBlockStorage: updateTimeBlock: " +
    	                		"could not roll back transaction", x);
    	            }
                	
                	throw new StorageServiceException("TimeBlockStorage: updateTimeBlock: " +
                			"TimeBlock's associated Task must be in the database."); 
                }
            }

	        blockStatement = con.prepareStatement(Utilities.UPDATE_TIME_BLOCK); 
            Utilities.setValues(blockStatement, block.getStart().getTime(),
            		block.getEnd().getTime(), block.getTaskId(), block.getId());
            blockStatement.execute();
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: updateTimeBlock: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TimeBlockStorage: updateTimeBlock: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } 
	            catch(SQLException x) {
	                Utilities.printSQLException("TimeBlockStorage: updateTimeBlock: " +
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
		        if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: updateTimeBlock: could not close resource", x);
            }
	    } 
		return block; 
	}
	
	/**
	 * Remove TimeBlock from the database
	 * 
	 * @param block Block to remove from the database
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Block that was removed, for chaining callsA
	 */
	protected static ITimeBlockable removeTimeBlock(ITimeBlockable block, JdbcConnectionPool pool) {
		PreparedStatement blockStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
	    	
	        con.setAutoCommit(false);
	        blockStatement = con.prepareStatement(Utilities.DELETE_TIME_BLOCK); 
            Utilities.setValues(blockStatement, block.getId());
            blockStatement.execute();
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: removeTimeBlock: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("TimeBlockStorage: removeTimeBlock: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } 
	            catch(SQLException x) {
	                Utilities.printSQLException("TimeBlockStorage: removeTimeBlock: " +
	                		"could not roll back transaction", x);
	            }
	        }
	    } 
	    finally {
	    	try {
	    		if (blockStatement != null) {
		            blockStatement.close();
		        }
		        con.setAutoCommit(true);
		        if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("TimeBlockStorage: removeTimeBlock: could not close resource", x);
            }
	    } 
		return block; 
	}
	
	protected static void addAllDefaultUnavailableBlocks(List<UnavailableBlock> blockList) {
		
	}
}
