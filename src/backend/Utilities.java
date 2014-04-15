package backend;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class Utilities {
	/*
	 * DB configuration parameters
	 * TODO: factor out into external configuration file
	 */
	
	protected static final String DB_URL = "jdbc:h2:~/test";
	protected static final String DB_USER = "sa"; 
	protected static final String DB_PWD = ""; 
	protected static final String DB_NAME = "test"; 
	
	/*
	 * Error handling helper methods
	 */
	
	public static void printSQLException(String msg, SQLException ex) {
	    for (Throwable e : ex) {
	        if (e instanceof SQLException) {
                System.err.println("ERROR: " + msg + ": " + e.getMessage());
	        }
	    }
	}
	
	public static void printException(String msg, Throwable e) {
		System.err.println("ERROR: " + msg + ": " + e.getMessage()); 
	}
	
	/*
	 * SQL query builder methods
	 */
	
	public static String buildCreateString(String tableName, List<String> columns) {
		StringBuilder result = new StringBuilder("CREATE TABLE " + tableName + " (");

		for (String column : columns) {
			result.append(column + ", "); 
		}
		result.replace(result.length() - 1, result.length(), ")"); 
		
		return result.toString(); 
	}
	
	public static void setValues(PreparedStatement preparedStatement, Object... values) throws SQLException {
	    for (int i = 0; i < values.length; i++) {
	        preparedStatement.setObject(i + 1, values[i]);
	    }
	}
	
	public class Tuple<K,V> {
		private K _k; 
		private V _v; 
		
		public Tuple(K key, V value) {
			_k = key; 
			_v = value; 
		}
		
		private K getKey() {
			return _k; 
		}
		
		private V getValue() {
			return _v; 
		}
		
		private K setKey(K key) {
			_k = key; 
			return key; 
		}
		
		private V setValue(V value) {
			_v = value; 
			return value; 
		}
	}
}
