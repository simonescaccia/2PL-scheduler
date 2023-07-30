package com.example.scheduler.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.example.scheduler.view.InputBean;

public class Scheduler2PL {
	List<String> schedule;			// Input schedule
	Boolean isLockAnticipation;		// Enable lock anticipation
	Boolean isLockShared;			// Use both exclusive and shared locks
	
	HashMap<String, List<String>> lockTable;
	
	List<String> scheduleWithLocks;
	
	public Scheduler2PL(InputBean iB) {
		this.schedule = iB.getSchedule();
		this.isLockAnticipation = iB.getIsLockAnticipation();
		this.isLockShared = iB.getIsLockShared();
		
		// Build the lock table, one row for each object
		HashMap<String, List<String>> lockTable = new HashMap<String, List<String>>();
		for(String operation: schedule) {
			if(OperationUtils.isCommit(operation)) {
				// skip operations without objects
				continue;
			}
			String objectName = OperationUtils.getObjectName(operation);
			
			if(!this.lockTable.containsKey(objectName)) {
				List<String> locks = new ArrayList<String>();
				// first column represents the transaction which gets the shared lock
				// second column represents the transaction which gets the exclusive lock  
				locks.addAll(Arrays.asList("",""));
				lockTable.put(objectName, locks);	
			}
		}
		this.lockTable = lockTable;
		
		this.scheduleWithLocks = new ArrayList<String>();
	}
	
	public void check() {
		
		for(String operation: schedule) {
			// execute operation
			if(OperationUtils.isCommit(operation)) {
				continue;
			}
			
			this.lock(operation);
			
		}
	}
	
	/*
	 * @operation: read or write operation
	 */
	private void lock(String operation) {
		String object = OperationUtils.getObjectName(operation);
		String transaction = OperationUtils.getTransactionNumber(operation);
		
		if(this.isLockAnticipation) {
			this.lockWithLockAnticipation(transaction, object);
		} else {
			this.lockWithoutLockAnticipation(transaction, object);
		}
	}
	
	private void lockWithLockAnticipation(String transactionNumber, String objectName) {
		
	}
	
	private void lockWithoutLockAnticipation(String transactionNumber, String objectName) {
		// Check lock type
		if(this.isLockShared) {
			// shared lock  logic
		} else {
			// exclusive lock logic
			if(this.lockTable.get(objectName).get(1).equals(transactionNumber)) {
				// object already locked by the same transaction
				return;
			} else if(this.lockTable.get(objectName).get(1).equals("")) {
				// free object
				List<String> newLocks = new ArrayList<String>(); 
				newLocks.addAll(Arrays.asList("",transactionNumber));
				this.lockTable.put(objectName, newLocks);		// update lockTable's exclusive lock
				addWriteLock(transactionNumber, objectName);	// add the write lock to the final schedule
				return;
			} else {
				
			}
		}
	}
	
	private void addWriteLock(String transactionNumber, String objectName) {
		if(this.isLockShared) {
			this.scheduleWithLocks.add(OperationUtils.createOperation("xl", transactionNumber, objectName));
		} else {
			this.scheduleWithLocks.add(OperationUtils.createOperation("l", transactionNumber, objectName));
		}
	}
	
	private void addReadLock(String transactionNumber, String objectName) {
		if(this.isLockShared) {
			this.scheduleWithLocks.add(OperationUtils.createOperation("sl", transactionNumber, objectName));
		} else {
			this.scheduleWithLocks.add(OperationUtils.createOperation("l", transactionNumber, objectName));
		}
	}
	
}