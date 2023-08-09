package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.List;

public class RequiredLocksToUnlockObject {
	
	String objectToUnlock;
	List<String> requiredLocksOperations;
	
	public RequiredLocksToUnlockObject(String object) {
		this.objectToUnlock = object;
		this.requiredLocksOperations = new ArrayList<String>();
	}
	
	public List<String> getRequiredLocksOperations() {
		return this.requiredLocksOperations;
	}
}