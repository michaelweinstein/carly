package backend.database.tests;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backend.database.StorageService;
import backend.database.Utilities;


public class SettingStorageTest {
	
	Connection _con; 
	
	@Before
	public void setUp() throws Exception {
		StorageService.initialize(false);
		try {
			_con = DriverManager.getConnection(Utilities.DB_URL, Utilities.DB_USER, Utilities.DB_PWD);
			Class.forName("org.h2.Driver");
		}
		catch (ClassNotFoundException e) {
			fail("StorageServiceTest: setUp: db drive class not found: " + e.getMessage()); 
		}
	}
	
	@After
	public void tearDown() throws Exception {
		_con.close(); 
	}
	
	/*
	 * Testing Setting related functionality 
	 */
	
	@Test
	public void addSetting() {
		
	}
	
	@Test
	public void getSetting() {
		
	}
	
	@Test
	public void getAllSettings() {
		
	}
	
}
