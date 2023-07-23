package com.example.scheduler.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.example.scheduler.view.InputBean;

public class Scheduler2PL {
	List<String> schedule;				// Input schedule
	Boolean isLockAnticipation;		// Enable lock anticipation
	Boolean isLockShared;			// Use both exclusive and shared locks
	
	HashMap<String, List<String>> transactionNumbers;
	
	HashMap<String, List<String>> lockTable;
	List<String> scheduleWithLocks;
	
	public Scheduler2PL(InputBean iB) {
		this.schedule = iB.getSchedule();
		this.isLockAnticipation = iB.getIsLockAnticipation();
		this.isLockShared = iB.getIsLockShared();
		this.transactionNumbers = iB.getTransactions();
		
		for(String transaction: transactionNumbers.keySet()) {
			List<String> locks = new ArrayList<String>();
			// first entry represents the transaction which gets the shared lock
			// second entry represents the transaction which gets the exclusive lock  
			locks.addAll(Arrays.asList("",""));
			this.lockTable.put(transaction, locks);
		}
	}

	public void check() {
		for(String operation: schedule) {
			
		}
	}
	
	
}