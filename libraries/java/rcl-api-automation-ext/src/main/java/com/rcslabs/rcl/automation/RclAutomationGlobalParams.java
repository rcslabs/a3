package com.rcslabs.rcl.automation;

import java.util.ArrayList;
import java.util.List;

public class RclAutomationGlobalParams {
	//phone numbers on which to perform automation
	private List<String> phoneNumbers = new ArrayList<String>();
	
	//password to register
	private String password = "";

	public List<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(List<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
