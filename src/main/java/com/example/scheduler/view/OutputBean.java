package com.example.scheduler.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.scheduler.controller.OperationUtils;
import com.example.scheduler.exception.InternalErrorException;

public class OutputBean {
	String scheduleWithLocks;			// Output schedule
	String dataActionProjection;
	List<String> log;					// Description of the computation
	Boolean result;						// Define if the schedule follows the 2PL protocol
	
	HashMap<String, String> transactionsWithLocks = new HashMap<String, String>();
	HashMap<String, String> transactions = new HashMap<String, String>();
	
	public OutputBean(
			List<String> schedule,
			List<String> scheduleWithLocks, 
			List<String> log, 
			Boolean result, 
			List<String> dataActionProjection) 
					throws InternalErrorException {
		this.log = log;
		this.result = result;
		this.setTransactionsWithLocks(scheduleWithLocks);
		this.setTransactions(schedule);
		this.formatScheduleWithLocks(scheduleWithLocks);
		this.setDataActionProjection(dataActionProjection);
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
	
	public HashMap<String, String> getTransactionsWithLocks(){
		return this.transactionsWithLocks;
	}
	
	public HashMap<String, String> getTransactions(){
		return this.transactions;
	}
	
	public String getDataActionProjection() {
		return this.dataActionProjection;
	}
	
	private void formatScheduleWithLocks(List<String> scheduleWithLocks) {
		this.scheduleWithLocks = scheduleWithLocks.get(0);
		for(String operation: scheduleWithLocks.subList(1, scheduleWithLocks.size())) {
			this.scheduleWithLocks = this.scheduleWithLocks.concat(" ".concat(operation));
		}
	}
	
	private HashMap<String, List<String>> splitScheduleIntoTransaction(List<String> schedule) {
		HashMap<String, List<String>> transactionSchedules = new HashMap<String, List<String>>();
		// parse the schedule to split operations
		for(String operation: schedule) {
			String transactionNumber = OperationUtils.getTransactionNumber(operation);
			if(!transactionSchedules.containsKey(transactionNumber)) {
				// add the new transaction
				List<String> transactionOperations = new ArrayList<String>();
				transactionOperations.add(operation);
				transactionSchedules.put(transactionNumber, transactionOperations);
			} else {
				// append operation
				transactionSchedules.get(transactionNumber).add(operation);
			}
		}
		return transactionSchedules;
	}
	
	private HashMap<String, String> transactionScheduleToList(HashMap<String, List<String>> transactionSchedules) {
		HashMap<String, String> transactionStringSchedules = new HashMap<String, String>();
		// join lists to set string schedules
		for(String transaction: transactionSchedules.keySet()) {
			transactionStringSchedules.put(
					transaction, 
					String.join(" ", transactionSchedules.get(transaction)));
		}
		return transactionStringSchedules;
	}
	
	private void setTransactionsWithLocks(List<String> scheduleWithLocks) throws InternalErrorException {
		HashMap<String, List<String>> transactionsWithLocks = this.splitScheduleIntoTransaction(scheduleWithLocks);

		// check 2PL
		HashMap<String, List<String>> unlocks = new HashMap<String, List<String>>();
		for(String transaction: transactionsWithLocks.keySet()) {
			Boolean transactionShrinkingPhase = false;
			unlocks.put(transaction, new ArrayList<String>());
			for(String operation: transactionsWithLocks.get(transaction)) {
				if(OperationUtils.isUnlock(operation)) {
					transactionShrinkingPhase = true;
					if(unlocks.get(transaction).contains(operation)) {
						// already unlocked this object
						throw new InternalErrorException(
								String.format("Internal error, output transaction %s doesn't follows the 2PL protocol, duplicated unlocks: %s. ",
										transaction,
										String.join(" ", transactionsWithLocks.get(transaction)))
								+ "Log: "
								+ String.join(". ", this.log));
					}
					unlocks.get(transaction).add(operation);
					continue;
				}
				if(OperationUtils.isLock(operation) && transactionShrinkingPhase) {
					throw new InternalErrorException(
							String.format("Internal error, output transaction %s doesn't follows the 2PL protocol: %s. ",
									transaction,
									String.join(" ", transactionsWithLocks.get(transaction)))
							+ "Log: "
							+ String.join(". ", this.log));
				}
			}
		}
		
		// join lists to set string schedules
		this.transactionsWithLocks = this.transactionScheduleToList(transactionsWithLocks);
	}
	
	private void setTransactions(List<String> schedule) {
		HashMap<String, List<String>> transactions = this.splitScheduleIntoTransaction(schedule);
		this.transactions = this.transactionScheduleToList(transactions);
	}
	

	private void setDataActionProjection(List<String> dataActionProjection) {
		if(dataActionProjection.size() == 0) {
			this.dataActionProjection = "";		
		} else {
			this.dataActionProjection = String.join(" ", dataActionProjection);
		}
	}
}