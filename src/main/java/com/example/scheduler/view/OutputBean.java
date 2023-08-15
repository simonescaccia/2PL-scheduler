package com.example.scheduler.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.scheduler.controller.OperationUtils;
import com.example.scheduler.exception.InternalErrorException;

public class OutputBean {
	String scheduleWithLocks;			// Output schedule
	List<String> log;					// Description of the computation
	Boolean result;						// Define if the schedule follows the 2PL protocol
	
	HashMap<String, List<String>> transactionsWithLocks = new HashMap<String, List<String>>();
	
	public OutputBean(List<String> scheduleWithLocks, List<String> log, Boolean result) throws InternalErrorException {
		this.log = log;
		this.result = result;
		this.setTransactionsWithLocks(scheduleWithLocks);
		this.formatScheduleWithLocks(scheduleWithLocks);
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
	
	private void setTransactionsWithLocks(List<String> scheduleWithLocks) throws InternalErrorException {
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

		// Check 2PL
		for(String transaction: this.transactionsWithLocks.keySet()) {
			Boolean transactionShrinkingPhase = false;
			for(String operation: this.transactionsWithLocks.get(transaction)) {
				if(OperationUtils.isUnlock(operation)) {
					transactionShrinkingPhase = true;
					continue;
				}
				if(OperationUtils.isLock(operation) && transactionShrinkingPhase) {
					throw new InternalErrorException(
							String.format("Internal error, output transaction %s doesn't follows the 2PL protocol: %s. ",
									transaction,
									String.join(" ", this.transactionsWithLocks.get(transaction)))
							+ "Log:"
							+ String.join(". ", this.log));
				}
			}
		}
	}
}