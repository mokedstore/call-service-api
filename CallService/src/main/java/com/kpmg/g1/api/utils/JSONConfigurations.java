package com.kpmg.g1.api.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 * Class that reads JSONConfigurations from configurations.json file in resources.
 * This class is a Singelton which is being initialized during service startup and served when needed to MailListenerServiceImplementation class.
 * */
public class JSONConfigurations {
	
	final static Logger log = LogManager.getLogger(JSONConfigurations.class.getName());
	
	private static JSONConfigurations instance;
	private JSONObject configurations;
	
	private JSONConfigurations(JSONObject configurations) {
		this.configurations = configurations;
	}
	
	// Singelton of JSONConfigurations
	public static JSONConfigurations getInstance() {
		if(instance == null) {
			JSONObject configurations = null;
			// get configurations as json string and create JSONObject
			String configurationAsString = readFile();
			if (configurationAsString != null) {
				// load environment specific configuration
				configurations = new JSONObject(configurationAsString);
			}
			instance = new JSONConfigurations(configurations);
		}
		return instance;
	}

	public JSONObject getConfigurations() {
		return configurations;
	}
	
	public void setConfigurations(JSONObject configurations) {
		this.configurations = configurations;
	}
	
	// read file as JSON string configurations folder
	public static String readFile() throws  RuntimeException {
		String filePath = System.getenv(Constants.CONFIGURATION_FILE_PATH);
		if (filePath == null) { throw new RuntimeException("Could not find Environment variable CALL_SERVICE_CONFIGURATION_FILE_PATH. configurations were not loaded"); }
		String result = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			result = sb.toString();
		} catch (Exception e) {
			log.error("error occured when trying to read file configurations: " + e.getMessage() + " " + e.getCause());
			// nullify result if error occurs
			result = null;
		} finally {
			try {
				br.close();
			} catch (Exception e) {
				log.error("error occured when trying to close configurations file: " + e.getMessage() + " " + e.getCause());
			}
		}
		return result;
	}
	
	/**
	 * reads the value of configurations file and updates configurations accordingly
	 * */
	public static void updateConfigurations() {
		// store old confiurations in case new configurations are malformed
		JSONObject oldConfigurations = instance.configurations;
		try {
			String newConfigurationsString = readFile();
			instance.configurations = new JSONObject(newConfigurationsString);
			
		} catch (Exception e) {
			log.error("Error occured while trying to apply configurations. using old configurations: " + e.getMessage() + " " + e.getStackTrace());
			instance.configurations = oldConfigurations;
		}
	}
	
}