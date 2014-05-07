package backend.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

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
	protected static void buildTable(final ArrayList<String> queries) {
		// TimeBlock table
		final ArrayList<String> blockCols = new ArrayList<>();
		blockCols.add(StorageService.concatColumn("BLOCK_ID", "VARCHAR(255) NOT NULL PRIMARY KEY"));
		blockCols.add(StorageService.concatColumn("TASK_ID", "VARCHAR(255)"));
		blockCols.add(StorageService.concatColumn("BLOCK_START", "BIGINT"));
		blockCols.add(StorageService.concatColumn("BLOCK_END", "BIGINT"));
		blockCols.add(StorageService.concatColumn("BLOCK_MOVABLE", "BOOLEAN"));
		blockCols.add(StorageService.concatColumn("BLOCK_DEFAULT", "BOOLEAN DEFAULT FALSE"));
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
	protected static List<UnavailableBlock> getAllUnavailableBlocksWithinRange(final Date date1, final Date date2,
			final JdbcConnectionPool pool) {
		PreparedStatement modifiedStatement = null;
		PreparedStatement defaultStatement = null;
		Connection con = null;
		final TreeSet<UnavailableBlock> results = new TreeSet<>();
		
		// Defensive programming in case the date1 is not earlier than date2
		final Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2;
		final Date later = (date2.compareTo(date1) > 0) ? date2 : date1;
		
		final ArrayList<DateRange> ranges = TimeBlockStorage.splitIntoWeekRanges(earlier, later);
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			for (final DateRange range : ranges) {
				modifiedStatement = con.prepareStatement(Utilities.SELECT_CUSTOM_UNAVAILABLE_BLOCKS_BY_DATE);
				Utilities.setValues(modifiedStatement, range.start.getTime(), range.end.getTime(),
						range.start.getTime(), range.end.getTime(), range.start.getTime(), range.end.getTime());
				ResultSet blockResults = modifiedStatement.executeQuery();
				
				int numResults = 0;
				while (blockResults.next()) {
					final String blockId = blockResults.getString("BLOCK_ID");
					final Date blockStart = new Date(blockResults.getLong("BLOCK_START"));
					final Date blockEnd = new Date(blockResults.getLong("BLOCK_END"));
					final boolean blockMovable = blockResults.getBoolean("BLOCK_MOVABLE");
					
					results.add(new UnavailableBlock(blockId, blockStart, blockEnd, null, blockMovable));
					numResults++;
				}
				
				// No custom unavailable blocks this week, so return the default set!
				if (numResults == 0) {
					defaultStatement = con.prepareStatement(Utilities.SELECT_DEFAULT_UNAVAILABLE_BLOCKS);
					blockResults = defaultStatement.executeQuery();
					
					// We need to bring the date of all of the default timeBlocks to this week
					final Calendar cal = Calendar.getInstance();
					cal.setFirstDayOfWeek(Calendar.SUNDAY);
					cal.setTime(range.start);
					cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					final long msWeekStartNow = cal.getTimeInMillis();
					cal.setTime(new Date(0));
					cal.add(Calendar.DAY_OF_YEAR, 3);
					final long msWeekStartDefault = cal.getTimeInMillis();
					final long msModifier = msWeekStartNow - msWeekStartDefault;
					
					while (blockResults.next()) {
						// Getting all of the required fields to reconstruct the UnavailableBlock
						final String blockId = blockResults.getString("BLOCK_ID");
						
						long blockStartTest = blockResults.getLong("BLOCK_START"); 
						long blockEndTest = blockResults.getLong("BLOCK_END");
						
						final Date blockStart = new Date(blockResults.getLong("BLOCK_START") + msModifier);
						final Date blockEnd = new Date(blockResults.getLong("BLOCK_END") + msModifier);
						final boolean blockMovable = blockResults.getBoolean("BLOCK_MOVABLE");
						
						boolean test1 = (blockStart.compareTo(range.start) >= 0 && blockStart.compareTo(range.end) <= 0);
						boolean test2 = (blockEnd.compareTo(range.start) >= 0 && blockEnd.compareTo(range.end) <= 0); 
						boolean test3 = (blockStart.compareTo(range.start) <= 0 && blockEnd.compareTo(range.end) >= 0); 
						
						if ((blockStart.compareTo(range.start) >= 0 && blockStart.compareTo(range.end) <= 0)
							|| (blockEnd.compareTo(range.start) >= 0 && blockEnd.compareTo(range.end) <= 0)
							|| (blockStart.compareTo(range.start) <= 0 && blockEnd.compareTo(range.end) >= 0)) {
							results.add(new UnavailableBlock(blockId, blockStart, blockEnd, null, blockMovable));
						}
					}
					
					int i = 0; 
				}
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: getAllUnavailableBlocksWithinRange: "
				+ "db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: getAllUnavailableBlocksWithinRange: "
				+ "could not retrieve unavailable blocks", e);
		}
		finally {
			try {
				if (modifiedStatement != null) {
					modifiedStatement.close();
				}
				if (defaultStatement != null) {
					defaultStatement.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException("TimeBlockStorage: getAllUnavailableBlocksWithinRange: "
					+ "could not close resource", x);
			}
		}
		
		return new ArrayList<UnavailableBlock>(results);
	}
	
	/**
	 * Gets all Assignment Blocks within the specified range
	 * 
	 * @param date1 Lower bound for the date range
	 * @param date2 Upper bound for the date range
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return List of all the blocks that fall COMPLETELY within these bounds
	 */
	protected static List<AssignmentBlock> getAllAssignmentBlocksWithinRange(final Date date1, final Date date2,
			final JdbcConnectionPool pool) {
		PreparedStatement statement = null;
		Connection con = null;
		final ArrayList<AssignmentBlock> results = new ArrayList<>();
		
		// Defensive programming in case the date1 is not earlier than date2
		final Date earlier = (date1.compareTo(date2) < 0) ? date1 : date2;
		final Date later = (date2.compareTo(date1) > 0) ? date2 : date1;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			statement = con.prepareStatement(Utilities.SELECT_ASSIGNMENT_BLOCKS_BY_DATE);
			Utilities.setValues(statement, earlier.getTime(), later.getTime(), earlier.getTime(), later.getTime(),
					earlier.getTime(), later.getTime());
			final ResultSet blockResults = statement.executeQuery();
			
			while (blockResults.next()) {
				final String blockId = blockResults.getString("BLOCK_ID");
				final String taskId = blockResults.getString("TIME_BLOCK.TASK_ID");
				final Date blockStart = new Date(blockResults.getLong("BLOCK_START"));
				final Date blockEnd = new Date(blockResults.getLong("BLOCK_END"));
				final boolean blockMovable = blockResults.getBoolean("BLOCK_MOVABLE");
				
				final String asgnId = blockResults.getString("ASGN_ID");
				final String taskName = blockResults.getString("TASK_NAME");
				final int taskNumber = blockResults.getInt("TASK_TASK_NUMBER");
				final double taskPercentTotal = blockResults.getDouble("TASK_PERCENT_TOTAL");
				final double taskPercentComplete = blockResults.getDouble("TASK_PERCENT_COMPLETE");
				final String timeOfDay = blockResults.getString("TASK_TIME_OF_DAY");
				final TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay);
				final double taskSuggestedLength = blockResults.getDouble("TASK_SUGGESTED_LENGTH");
				
				final Task task = new Task(taskId, taskName, taskNumber, taskPercentTotal, asgnId, taskPercentComplete,
						taskTimeOfDay, taskSuggestedLength);
				results.add(new AssignmentBlock(blockId, blockStart, blockEnd, task, blockMovable));
			}
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: getAllAssignmentBlocksWithinRange: "
				+ "db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: getAllAssignmentBlocksWithinRange: "
				+ "could not retrieve assignment blocks", e);
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
				Utilities.printSQLException("TimeBlockStorage: getAllAssignmentBlocksWithinRange: "
					+ "could not close resource", x);
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
	protected static AssignmentBlock getAssignmentBlock(final String blockId, final JdbcConnectionPool pool) {
		PreparedStatement statement = null;
		Connection con = null;
		AssignmentBlock block = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			statement = con.prepareStatement(Utilities.SELECT_ASSIGNMENT_BLOCK_BY_ID);
			Utilities.setValues(statement, blockId);
			final ResultSet blockResults = statement.executeQuery();
			
			// Should only have one result since the blockId is a unique primary key
			blockResults.next();
			
			final String id = blockResults.getString("BLOCK_ID");
			final String taskId = blockResults.getString("TIME_BLOCK.TASK_ID");
			final Date blockStart = new Date(blockResults.getLong("BLOCK_START"));
			final Date blockEnd = new Date(blockResults.getLong("BLOCK_END"));
			final boolean blockMovable = blockResults.getBoolean("BLOCK_MOVABLE");
			
			final String asgnId = blockResults.getString("ASGN_ID");
			final String taskName = blockResults.getString("TASK_NAME");
			final int taskNumber = blockResults.getInt("TASK_TASK_NUMBER");
			final double taskPercentTotal = blockResults.getDouble("TASK_PERCENT_TOTAL");
			final double taskPercentComplete = blockResults.getDouble("TASK_PERCENT_COMPLETE");
			final String timeOfDay = blockResults.getString("TASK_TIME_OF_DAY");
			final TimeOfDay taskTimeOfDay = TimeOfDay.valueOf(timeOfDay);
			final double taskSuggestedLength = blockResults.getDouble("TASK_SUGGESTED_LENGTH");
			
			final Task task = new Task(taskId, taskName, taskNumber, taskPercentTotal, asgnId, taskPercentComplete,
					taskTimeOfDay, taskSuggestedLength);
			
			block = new AssignmentBlock(id, blockStart, blockEnd, task, blockMovable);
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: getAssignmentBlock: " + "db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: getAssignmentBlock: " + "could not retrieve time block", e);
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
				Utilities.printSQLException("TimeBlockStorage: getAssignmentBlock: " + "could not close resource", x);
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
	protected static UnavailableBlock getUnavailableBlock(final String blockId, final JdbcConnectionPool pool) {
		PreparedStatement statement = null;
		Connection con = null;
		UnavailableBlock block = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			statement = con.prepareStatement(Utilities.SELECT_UNAVAILABLE_BLOCK_BY_ID);
			Utilities.setValues(statement, blockId);
			final ResultSet blockResults = statement.executeQuery();
			
			// Should only have one result since the blockId is a unique primary key
			blockResults.next();
			
			final String id = blockResults.getString("BLOCK_ID");
			final Date blockStart = new Date(blockResults.getLong("BLOCK_START"));
			final Date blockEnd = new Date(blockResults.getLong("BLOCK_END"));
			final boolean blockMovable = blockResults.getBoolean("BLOCK_MOVABLE");
			
			block = new UnavailableBlock(id, blockStart, blockEnd, null, blockMovable);
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: getUnavailableBlock: " + "db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: getUnavailableBlock: " + "could not retrieve time block", e);
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
				Utilities.printSQLException("TimeBlockStorage: getUnavailableBlock: " + "could not close resource", x);
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
	protected static void addTimeBlock(final ITimeBlockable block, final JdbcConnectionPool pool)
			throws StorageServiceException {
		PreparedStatement blockStatement = null;
		PreparedStatement taskStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			blockStatement = con.prepareStatement(Utilities.INSERT_TIME_BLOCK);
			Utilities.setValues(blockStatement, block.getId(), block.getTaskId(), block.getStart().getTime(), block
					.getEnd().getTime(), block.isMovable(), false);
			blockStatement.execute();
			
			// Only perform this check for assignment blocks
			if (!block.getTaskId().matches("")) {
				// Check to see that the task associated with the time block has been added to the db
				taskStatement = con.prepareStatement(Utilities.SELECT_TASK_BY_ID);
				Utilities.setValues(taskStatement, block.getTaskId());
				final ResultSet rs = taskStatement.executeQuery();
				int numTasks = 0;
				while (rs.next()) {
					numTasks++;
				}
				
				// If the associated task is not in the db, rollback and throw and exception
				if (numTasks == 0) {
					try {
						con.rollback();
					} catch (final SQLException x) {
						Utilities.printSQLException("TimeBlockStorage: addTimeBlock: "
							+ "could not roll back transaction", x);
					}
					
					throw new StorageServiceException("TimeBlockStorage: addTimeBlock: "
						+ "TimeBlock's associated Task must be in the database.");
				}
			}
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: addTimeBlock: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: addTimeBlock: " + "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TimeBlockStorage: addTimeBlock: " + "could not roll back transaction",
							x);
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
			} catch (final SQLException x) {
				Utilities.printSQLException("TimeBlockStorage: addTimeBlock: could not close resource", x);
			}
		}
	}
	
	/**
	 * Adds VALID AvailableBlocks if they don't already exist in the database Updates any TimeBlocks that already have been
	 * stored in the database
	 * 
	 * All unavailable blocks passed in will be ignored. 
	 * 
	 * @param blockList List of blocks to to be added to or updated in the database
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return A list of INVALID TimeBlocks (that is, those whose associated Task cannot be found in the database) that
	 *         were NOT added/updated
	 */
	protected static List<ITimeBlockable> mergeAllTimeBlocks(final List<ITimeBlockable> blockList,
			final JdbcConnectionPool pool) {
		final List<ITimeBlockable> blocksNotAdded = new ArrayList<>();
		final List<ITimeBlockable> blocksToAdd = new ArrayList<>();
		PreparedStatement blockStatement = null;
		PreparedStatement taskStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			
			for (final ITimeBlockable block : blockList) {
				// Only perform this check for assignment blocks
				if (!block.getTaskId().matches("")) {
					// Check to see that the task associated with the time block has been added to the db
					taskStatement = con.prepareStatement(Utilities.SELECT_TASK_BY_ID);
					Utilities.setValues(taskStatement, block.getTaskId());
					final ResultSet rs = taskStatement.executeQuery();
					int numTasks = 0;
					while (rs.next()) {
						numTasks++;
					}
					
					// If the associated task is not in the db, rollback and throw and exception
					if (numTasks == 0) {
						blocksNotAdded.add(block);
					} else {
						blocksToAdd.add(block);
					}
				}
			}
			
			blockStatement = con.prepareStatement(Utilities.MERGE_TIME_BLOCK);
			for (final ITimeBlockable block : blocksToAdd) {
				Utilities.setValues(blockStatement, block.getId(), block.getTaskId(), block.getStart().getTime(), block
						.getEnd().getTime(), block.isMovable());
				blockStatement.addBatch();
			}
			blockStatement.executeBatch();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: mergeAllTimeBlocks: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: mergeAllTimeBlocks: "
				+ "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TimeBlockStorage: mergeAllTimeBlocks: "
						+ "could not roll back transaction", x);
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
			} catch (final SQLException x) {
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
	protected static ITimeBlockable updateTimeBlock(final ITimeBlockable block, final JdbcConnectionPool pool)
			throws StorageServiceException {
		PreparedStatement blockStatement = null;
		PreparedStatement taskStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			
			// Only perform this check for assignment blocks
			if (!block.getTaskId().matches("")) {
				// Check to see that the task associated with the time block has been added to the db
				taskStatement = con.prepareStatement(Utilities.SELECT_TASK_BY_ID);
				Utilities.setValues(taskStatement, block.getTaskId());
				final ResultSet rs = taskStatement.executeQuery();
				int numTasks = 0;
				while (rs.next()) {
					numTasks++;
				}
				
				// If the associated task is not in the db, rollback and throw and exception
				if (numTasks == 0) {
					try {
						con.rollback();
					} catch (final SQLException x) {
						Utilities.printSQLException("TimeBlockStorage: updateTimeBlock: "
							+ "could not roll back transaction", x);
					}
					
					throw new StorageServiceException("TimeBlockStorage: updateTimeBlock: "
						+ "TimeBlock's associated Task must be in the database.");
				}
			}
			
			blockStatement = con.prepareStatement(Utilities.UPDATE_TIME_BLOCK);
			Utilities.setValues(blockStatement, block.getStart().getTime(), block.getEnd().getTime(),
					block.getTaskId(), block.getId());
			blockStatement.execute();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: updateTimeBlock: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: updateTimeBlock: " + "attempting to roll back transaction",
					e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TimeBlockStorage: updateTimeBlock: "
						+ "could not roll back transaction", x);
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
			} catch (final SQLException x) {
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
	protected static ITimeBlockable removeTimeBlock(final ITimeBlockable block, final JdbcConnectionPool pool) {
		PreparedStatement blockStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			blockStatement = con.prepareStatement(Utilities.DELETE_TIME_BLOCK);
			Utilities.setValues(blockStatement, block.getId());
			blockStatement.execute();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: removeTimeBlock: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: removeTimeBlock: " + "attempting to roll back transaction",
					e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TimeBlockStorage: removeTimeBlock: "
						+ "could not roll back transaction", x);
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
			} catch (final SQLException x) {
				Utilities.printSQLException("TimeBlockStorage: removeTimeBlock: could not close resource", x);
			}
		}
		return block;
	}
	
	/**
	 * Add all default unavailable blocks from the startup survey
	 * 
	 * @param blockList List of default unavailable blocks to add
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 */
	protected static void addAllDefaultUnavailableBlocks(final List<UnavailableBlock> blockList,
			final JdbcConnectionPool pool) {
		PreparedStatement blockStatement = null;
		Connection con = null;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			
			blockStatement = con.prepareStatement(Utilities.INSERT_TIME_BLOCK);
			for (final ITimeBlockable block : blockList) {
				Utilities.setValues(blockStatement, block.getId(), block.getTaskId(), block.getStart().getTime(), block
						.getEnd().getTime(), block.isMovable(), true);
				blockStatement.addBatch();
			}
			
			blockStatement.executeBatch();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: addAllDefaultUnavailableBlocks: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: addAllDefaultUnavailableBlocks: "
				+ "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TimeBlockStorage: addAllDefaultUnavailableBlocks: "
						+ "could not roll back transaction", x);
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
			} catch (final SQLException x) {
				Utilities.printSQLException("TimeBlockStorage: addAllDefaultUnavailableBlocks: "
					+ "could not close resource", x);
			}
		}
	}
	
	/**
	 * @param startDate
	 * @param endDate
	 * @param blockList
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 */
	public static void replaceUnavailableBlocks(final Date startDate, final Date endDate,
			final List<? extends ITimeBlockable> blockList, final JdbcConnectionPool pool) {
		PreparedStatement deleteBlockStatement = null;
		PreparedStatement blockStatement = null;
		Connection con = null;
		
		final Date earlier = (startDate.compareTo(endDate) < 0) ? startDate : endDate;
		final Date later = (endDate.compareTo(startDate) > 0) ? endDate : startDate;
		
		try {
			Class.forName("org.h2.Driver");
			con = pool.getConnection();
			
			con.setAutoCommit(false);
			
			// Delete all the blocks in the range
			deleteBlockStatement = con.prepareStatement(Utilities.DELETE_UNAVAILABLE_BLOCKS_BY_DATE);
			Utilities.setValues(deleteBlockStatement, earlier.getTime(), later.getTime(), earlier.getTime(),
					later.getTime(), earlier.getTime(), later.getTime());
			deleteBlockStatement.execute();
			
			// Insert all of the custom time blocks for that period
			blockStatement = con.prepareStatement(Utilities.INSERT_TIME_BLOCK);
			for (final ITimeBlockable block : blockList) {
				Utilities.setValues(blockStatement, block.getId(), block.getTaskId(), block.getStart().getTime(), block
						.getEnd().getTime(), block.isMovable(), false);
				blockStatement.addBatch();
			}
			
			blockStatement.executeBatch();
			
			// commit to the database
			con.commit();
		} catch (final ClassNotFoundException e) {
			Utilities.printException("TimeBlockStorage: replaceUnavailableBlocks: db drive class not found", e);
		} catch (final SQLException e) {
			Utilities.printSQLException("TimeBlockStorage: replaceUnavailableBlocks: "
				+ "attempting to roll back transaction", e);
			if (con != null) {
				try {
					con.rollback();
				} catch (final SQLException x) {
					Utilities.printSQLException("TimeBlockStorage: replaceUnavailableBlocks: "
						+ "could not roll back transaction", x);
				}
			}
		}
		finally {
			try {
				if (deleteBlockStatement != null) {
					deleteBlockStatement.close();
				}
				if (blockStatement != null) {
					blockStatement.close();
				}
				con.setAutoCommit(true);
				if (con != null) {
					con.close();
				}
			} catch (final SQLException x) {
				Utilities.printSQLException(
						"TimeBlockStorage: replaceUnavailableBlocks: " + "could not close resource", x);
			}
		}
	}
	
	/*
	 * Helper methods and classes
	 */
	// Public for testing
	public static ArrayList<DateRange> splitIntoWeekRanges(final Date earlier, final Date later) {
		final ArrayList<DateRange> ranges = new ArrayList<>();
		
		final Calendar cal = Calendar.getInstance();
		cal.setTime(earlier);
		
		DateRange range = new DateRange(earlier, earlier);
		DateRange currentRange = range;
		
		// set time to midnight the next day
		cal.setTime(earlier);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		while (later.after(currentRange.start) || (later.getTime() > currentRange.start.getTime())) {
			// Add one to move away from the starting Sunday
			cal.add(Calendar.DAY_OF_YEAR, 1);
			
			// Increment until nex Sunday
			while (cal.get(Calendar.DAY_OF_WEEK) != 1) {
				cal.add(Calendar.DAY_OF_YEAR, 1);
			}
			
			currentRange.end = cal.getTime();
			range = currentRange;
			ranges.add(range);
			
			currentRange = new DateRange(cal.getTime());
		}
		
		range.end = later;
		
		return ranges;
	}
	
	// Public for testing
	public static class DateRange {
		
		public Date	start;
		public Date	end;
		
		public DateRange(final Date start) {
			this.start = start;
		}
		
		public DateRange(final Date start, final Date end) {
			this.start = start;
			this.end = end;
		}
		
		@Override
		public String toString() {
			return "\n\tDateRange: [start: " + start.toString() + "; end: " + end.toString() + "]";
		}
	}
}
