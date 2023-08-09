package com.example.scheduler.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.scheduler.controller.OperationUtils;

public class OutputBean {
	String scheduleWithLocks;			// Output schedule
	List<String> log;					// Description of the computation
	Boolean result;						// Define if the schedule follows the 2PL protocol
	
	HashMap<String, List<String>> transactionsWithLocks = new HashMap<String, List<String>>();
	
	public OutputBean(List<String> scheduleWithLocks, List<String> log, Boolean result) {
		this.setTransactionsWithLocks(scheduleWithLocks);
		this.formatScheduleWithLocks(scheduleWithLocks);
		this.log = log;
		this.result = result;
	}

	public String getSchedleWithLocks() {
		return this.scheduleWithLocks;
	}
	
	public List<String> getLog() {
		return this.log;
	}
	
	public Boolean getResult() {
		return this.result;
	}
	
	public HashMap<String, List<String>> getTransactionsWithLocks(){
		return this.transactionsWithLocks;
	}
	
	private void formatScheduleWithLocks(List<String> scheduleWithLocks) {
		this.scheduleWithLocks = scheduleWithLocks.get(0);
		for(String operation: scheduleWithLocks.subList(1, scheduleWithLocks.size())) {
			this.scheduleWithLocks = this.scheduleWithLocks.concat(" ".concat(operation));
		}
	}
	
	private void setTransactionsWithLocks(List<String> scheduleWithLocks) {
		for(String operation: scheduleWithLocks) {
			String transactionNumber = OperationUtils.getTransactionNumber(operation);
			if(!this.transactionsWithLocks.containsKey(transactionNumber)) {
				// add the new transaction
				List<String> transactionOperations = new ArrayList<String>();
				transactionOperations.add(operation);
				this.transactionsWithLocks.put(transactionNumber, transactionOperations);
			} else {
				// append operation
				this.transactionsWithLocks.get(transactionNumber).add(operation);
			}
		}
	}
}