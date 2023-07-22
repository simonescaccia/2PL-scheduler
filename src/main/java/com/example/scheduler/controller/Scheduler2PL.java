package com.example.scheduler.controller;

import java.util.HashMap;
import java.util.List;

import com.example.scheduler.view.InputBean;

public class Scheduler2PL {
	String 	schedule;				// Input schedule
	Boolean isLockAnticipation;		// Enable lock anticipation
	Boolean isLockShared;			// Use both exclusive and shared locks
	
	HashMap<String, List<String>> transactionNumbers;  
	
	public Scheduler2PL(InputBean iB) {
		this.schedule = iB.getSchedule();
		this.isLockAnticipation = iB.getIsLockAnticipation();
		this.isLockShared = iB.getIsLockShared();
		this.transactionNumbers = iB.getTransactions();
	}

	public void check() {
		
	}
	
	
}