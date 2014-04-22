package backend.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SettingStorage {
	
	protected static void buildTable(ArrayList<String> queries) {
		
	}
	
	protected static synchronized void addSetting(String name, String val) {
		
	}
	
	protected static synchronized String getSetting(String name) {
		return ""; 
	}
	
	protected static synchronized Map<String, String> getAllSettings() {
		return new HashMap<String, String>(0); 
	}
}
