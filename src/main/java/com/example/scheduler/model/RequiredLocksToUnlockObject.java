package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequiredLocksToUnlockObject {
	
	String transactionToUnlock;
	String objectToUnlock;			// object to unlock
	Boolean isLastUsage;				// the transaction doesn't need other accesses to the object 
	List<String> otherUsageOperations;	// if isLastUsage is false, contains the operations that use the object
	HashMap<String, List<String>> otherRequiredLocks;
	Boolean isLockShared;
	
	public RequiredLocksToUnlockObject(String transactionToUnlock, String objectToUnlock, Boolean isLockShared) {
		this.transactionToUnlock = transactionToUnlock;
		this.objectToUnlock = objectToUnlock;
		this.isLockShared = isLockShared;
		this.isLastUsage = true;
		this.otherUsageOperations = new ArrayList<String>();
		this.otherRequiredLocks = new HashMap<String, List<String>>();
	}
	
	public void setLastUsageFalse(String operation) {
		this.isLastUsage = false;
		this.otherUsageOperations.add(operation);
	}
	
	public List<String> getOtherUsageOperations() {
		return this.otherUsageOperations;
	}
	
	public HashMap<String, List<String>> getOtherRequiredLocks() {
		return this.otherRequiredLocks;
	}
	
	public Boolean getIsLastUsage() {
		return this.isLastUsage;
	}
	
	public String getTransactionToUnlock() {
		return this.transactionToUnlock;
	}
	
	public String getObjectToUnlock() {
		return this.objectToUnlock;
	}

	public void addRequiredLock(String object, List<String> operationsOnTheSameObject) {
		// overwrite precedent list for the same object
		this.otherRequiredLocks.put(object, operationsOnTheSameObject);
	}
}