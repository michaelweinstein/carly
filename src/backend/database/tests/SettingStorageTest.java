package backend.database.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backend.database.StorageService;

public class SettingStorageTest {
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(true);
	}

	@After
	public void cleanUp() {
		StorageService.cleanup();
	}
	
	/*
	 * Testing Setting related functionality 
	 */
	
	@Test
	public void addSetting() {
		String name = "Name"; 
		String val = "Value"; 
		StorageService.mergeSetting(name, val);
		
		assertEquals(val, StorageService.getSetting(name));
	}
	
	@Test
	public void mergeSetting() {
		String name = "Name"; 
		String val = "Value"; 
		StorageService.mergeSetting(name, val);
		assertEquals(val, StorageService.getSetting(name));
		
		val = "New value"; 
		StorageService.mergeSetting(name, val);
		assertEquals(val, StorageService.getSetting(name));
	}
	
	@Test
	public void mergeAllSettings() {
		String name1 = "Setting 1"; 
		String value1 = "Value 1"; 
		String name2 = "Setting 2"; 
		String value2 = "Value 2";
		String name3 = "Setting 3"; 
		String value3 = "Value 3";
		String name4 = "Setting 4"; 
		String value4 = "Value 4";
		String name5 = "Setting 5"; 
		String value5 = "Value 5";
		
		StorageService.mergeSetting(name1, value1);
		StorageService.mergeSetting(name2, value2); 
		StorageService.mergeSetting(name3, value3); 
		
		assertEquals(value1, StorageService.getSetting(name1));
		assertEquals(value2, StorageService.getSetting(name2));
		assertEquals(value3, StorageService.getSetting(name3));
		
		value2 = "New " + value2; 
		value3 = "New " + value3; 
		
		HashMap<String,String> settings = new HashMap<>();
		settings.put(name1, value1);
		settings.put(name2, value2);
		settings.put(name3, value3);
		settings.put(name4, value4);
		settings.put(name5, value5);
		
		StorageService.mergeAllSettings(settings);
		assertEquals(value1, StorageService.getSetting(name1));
		assertEquals(value2, StorageService.getSetting(name2));
		assertEquals(value3, StorageService.getSetting(name3));
		assertEquals(value4, StorageService.getSetting(name4));
		assertEquals(value5, StorageService.getSetting(name5));
	}
	
	@Test
	public void getAllSettings() {
		String name1 = "Setting 1"; 
		String value1 = "Value 1"; 
		String name2 = "Setting 2"; 
		String value2 = "Value 2";
		String name3 = "Setting 3"; 
		String value3 = "Value 3";
		String name4 = "Setting 4"; 
		String value4 = "Value 4";
		String name5 = "Setting 5"; 
		String value5 = "Value 5";
		
		StorageService.mergeSetting(name1, value1);
		StorageService.mergeSetting(name2, value2); 
		StorageService.mergeSetting(name3, value3);
		StorageService.mergeSetting(name4, value4);
		StorageService.mergeSetting(name5, value5);
		
		Map<String,String> settings = StorageService.getAllSettings();
		assertEquals(value1, settings.get(name1));
		assertEquals(value2, settings.get(name2));
		assertEquals(value3, settings.get(name3));
		assertEquals(value4, settings.get(name4));
		assertEquals(value5, settings.get(name5));
	}
}
