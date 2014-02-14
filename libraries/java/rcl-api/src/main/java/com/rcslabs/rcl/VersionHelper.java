package com.rcslabs.rcl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class VersionHelper {
	private static final String PROPERTIES_FILE_PATH = "/user-agent.properties";
	
	private String name;
	private String version;
	private List<String> userAgentList;

	public VersionHelper() {
		InputStream resourceStream = getClass().getResourceAsStream(PROPERTIES_FILE_PATH);
		if(resourceStream == null) {
			throw new IllegalStateException("Resource " + PROPERTIES_FILE_PATH + " not found.");
		}
		try {
			Properties prop = new Properties();
			prop.load(resourceStream);

			name = getProperty(prop, "name");
			version = getProperty(prop, "version");
			
			userAgentList = Collections.unmodifiableList(
					Collections.singletonList(
							String.format(
									"%s/%s", 
									name,
									version
							)
					)
			);
		}
		catch(IOException e) {
			throw new IllegalStateException(e);
		}
		finally {
			try {
				resourceStream.close();
			} catch (IOException e) {
				//ignore
			}
		}
	}
	
	private String getProperty(Properties prop, String propertyName) {
		String ret = prop.getProperty(propertyName);
		if(ret == null) {
			throw new IllegalStateException("\"" + propertyName + "\" property not found in " + PROPERTIES_FILE_PATH);
		}
		
		return ret;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public List<String> getUserAgentList() {
		return userAgentList;
	}
}
