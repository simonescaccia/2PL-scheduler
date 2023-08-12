package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.example.scheduler.controller.OperationUtils;
import com.example.scheduler.exception.LockAnticipationException;

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
	
	public void checkLockAnticipation(String lockOperation) throws LockAnticipationException {
		// compute the check for each object to unlock
		for(String object: this.otherRequiredLocks.keySet()) {
			Boolean isOtherTransactionsOperationWrite = false;
			Boolean isTransactionOperationWrite = false;
			String otherTransactionFirstOperation = "";
			String otherTransactionFirstRead = "";
			String otherTransactionFirstWrite = "";
			String transactionOperation = "";
			
			for(String operation: this.otherRequiredLocks.get(object)) {
				String transaction = OperationUtils.getTransactionNumber(operation);
				Boolean isWrite = OperationUtils.isWrite(operation);
				
				if(transaction.equals(this.transactionToUnlock)) {
					transactionOperation = operation;
					isTransactionOperationWrite = isWrite; 
				} else if (!transaction.equals(this.transactionToUnlock)) {
					if(otherTransactionFirstOperation.equals("")) {
						otherTransactionFirstOperation = operation;
					}
					if(isWrite && otherTransactionFirstWrite.equals("")) {
						otherTransactionFirstWrite = operation;
					}
					if(!isWrite && otherTransactionFirstRead.equals("")) {
						otherTransactionFirstRead = operation;
					}
					if(isWrite && !isOtherTransactionsOperationWrite) {
						isOtherTransactionsOperationWrite = true;
					}
				}
				
				// live check
				if(!transactionOperation.equals("")) {
					String errorMessage = "During locking %s, unable to unlock the object %s locked by transaction %s. "
							+ "Unable to anticipate lock for %s due to %s";
					if(!this.isLockShared) {
						if(!otherTransactionFirstOperation.equals("")) {
							throw new LockAnticipationException(
									String.format(errorMessage,
											operation,
											object,
											transaction,
											transactionOperation,
											otherTransactionFirstOperation));
						}
					} else {
						if(isOtherTransactionsOperationWrite) {
							throw new LockAnticipationException(
									String.format(errorMessage, 
											operation,
											object,
											transaction,
											transactionOperation,
											otherTransactionFirstWrite));
						} else if(!isOtherTransactionsOperationWrite && isTransactionOperationWrite) {
							throw new LockAnticipationException(
									String.format(errorMessage, 
											operation,
											object,
											transaction,
											transactionOperation,
											otherTransactionFirstRead));
						}
					}
					
					// live check passed, then reset settings
					transactionOperation = "";
				}
			}
		}
	}
	
	public void setLastUsageFalse(String operation) {
		this.isLastUsage = false;
		this.otherUsageOperations.add(operation);
	}
	
	public List<String> getOtherUsageOperations() {
		return this.otherUsageOperations;
	}
	
	public Set<String> getOtherRequiredLocks() {
		return this.otherRequiredLocks.keySet();
	}
	
	public Boolean getIsLastUsage() {
		return this.isLastUsage;
	}
	
	public String getTransacitonToUnlock() {
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