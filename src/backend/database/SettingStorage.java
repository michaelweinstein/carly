package backend.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.h2.jdbcx.JdbcConnectionPool;

public class SettingStorage {
	
	/**
	 * Builds the create table string and returns to Storage Service
	 * 
	 * @param queries list of create table queries to execute by initialize in StorageService
	 */
	protected static void buildTable(ArrayList<String> queries) {
		//Setting table
        ArrayList<String> settingCols = new ArrayList<>();
        settingCols.add(StorageService.concatColumn("SETTING_NAME", "VARCHAR(255) NOT NULL PRIMARY KEY"));
        settingCols.add(StorageService.concatColumn("SETTING_VALUE", "VARCHAR(255)"));
        queries.add(Utilities.buildCreateString("SETTING", settingCols)); 
	}
	
	/**
	 * Merge one setting into the database
	 * 
	 * @param name String name of the setting to be merged
	 * @param val String value of the setting to be merged
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 */
	protected static void mergeSetting(String name, String val, JdbcConnectionPool pool) {
		PreparedStatement settingStatement = null;
	    Connection con = null; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
	        con.setAutoCommit(false);
	        
	        settingStatement = con.prepareStatement(Utilities.MERGE_SETTING); 
            Utilities.setValues(settingStatement, name, val);
            settingStatement.execute();
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("SettingStorage: addSetting: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("SettingStorage: addSetting: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } 
	            catch(SQLException x) {
	                Utilities.printSQLException("SettingStorage: addSetting: " +
	                		"could not roll back transaction", x);
	            }
	        }
	    } 
	    finally {
	    	try {
	    		if (settingStatement != null) {
		            settingStatement.close();
		        }
		        con.setAutoCommit(true);
		        if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("SettingStorage: addSetting: could not close resource", x);
            }
	    } 
	}
	
	/**
	 * Merge all settings to the database 
	 * 
	 * @param settings Map of String to String where key is the name of the setting and value is the info of the setting
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Boolean indicating if ALL settings were merged successfully
	 */
	protected static boolean mergeAllSettings(Map<String,String> settings, JdbcConnectionPool pool) {
		PreparedStatement settingStatement = null;
	    Connection con = null; 
	    boolean allSuccess = true; 
	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
	        con.setAutoCommit(false);
	        
	        settingStatement = con.prepareStatement(Utilities.MERGE_SETTING); 
	        for (Entry<String,String> setting : settings.entrySet()) {
	            Utilities.setValues(settingStatement, setting.getKey(), setting.getValue());
	            settingStatement.addBatch(); 
	        }
	        
            int[] statuses = settingStatement.executeBatch();
            for (int status : statuses) {
            	if (status == Statement.EXECUTE_FAILED) {
            		allSuccess = false; 
            	}
            }
            
            //commit to the database
            con.commit();
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("SettingStorage: addAllSettings: db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("SettingStorage: addAllSettings: " +
	        		"attempting to roll back transaction", e);
	        if (con != null) {
	            try {
	                con.rollback();
	            } 
	            catch(SQLException x) {
	                Utilities.printSQLException("SettingStorage: addAllSettings: " +
	                		"could not roll back transaction", x);
	            }
	        }
	    } 
	    finally {
	    	try {
	    		if (settingStatement != null) {
		            settingStatement.close();
		        }
		        con.setAutoCommit(true);
		        if (con != null) {
	    			con.close(); 
	    		}
	    	}
	    	catch(SQLException x) {
                Utilities.printSQLException("SettingStorage: addAllSettings: could not close resource", x);
            }
	    } 
	    
	    return allSuccess; 
	}
	
	/**
	 * Get the setting information corresponding to the passed-in setting name
	 * 
	 * @param name Setting name to retrieve information for
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return String value for the setting name passed in 
	 */
	protected static String getSetting(String name, JdbcConnectionPool pool) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    String value = ""; 
	    	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
			
	        statement = con.prepareStatement(Utilities.SELECT_SETTING_BY_NAME); 
        	Utilities.setValues(statement, name);
        	ResultSet settingResults = statement.executeQuery();
        	
        	//Should only have one result since the setting name is a unique primary key
        	settingResults.next();
        	
        	value = settingResults.getString("SETTING_VALUE");
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("SettingStorage: getSetting: " +
					"db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("SettingStorage: getSetting: " +
	        		"could not retrieve value based on the setting name \"" + name + "\"", e);
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
                Utilities.printSQLException("SettingStorage: getSetting: " +
                		"could not close resource", x);
            }
	    }
		
		return value; 
	}
	
	/**
	 * Gets all the settings stored in the database
	 * 
	 * @param pool JdbcConnectionPool for retrieving connection to the database
	 * @return Mapping of String to String where key is the name of the setting, and value is the info of the setting
	 */
	protected static Map<String, String> getAllSettings(JdbcConnectionPool pool) {
		PreparedStatement statement = null; 
	    Connection con = null; 
	    HashMap<String, String> result = new HashMap<>(); 
	    	    
	    try {
	    	Class.forName("org.h2.Driver");
	    	con = pool.getConnection();
			
	        statement = con.prepareStatement(Utilities.SELECT_ALL_SETTINGS); 
        	ResultSet settingResults = statement.executeQuery();
        	
        	//Should only have one result since the setting name is a unique primary key
        	while (settingResults.next()) {
        		String name = settingResults.getString("SETTING_NAME");
        		String value = settingResults.getString("SETTING_VALUE");
        		result.put(name, value); 
        	}
	    } 
	    catch (ClassNotFoundException e) {
			Utilities.printException("SettingStorage: getAllSettings: " +
					"db drive class not found", e);
		} 
	    catch (SQLException e) {
	        Utilities.printSQLException("SettingStorage: getAllSettings: " +
	        		"could not retrieve all the settings", e);
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
                Utilities.printSQLException("SettingStorage: getAllSettings: " +
                		"could not close resource", x);
            }
	    }
		
		return result; 
	}
}
