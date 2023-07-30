package com.example.scheduler.view;

import com.example.scheduler.controller.OperationUtils;
import com.example.scheduler.exception.InputBeanException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InputBean {
	List<String> schedule;				// Input schedule
	Boolean isLockAnticipation;		// Enable lock anticipation
	Boolean isLockShared;			// Use both exclusive and shared locks
	
	HashMap<String, List<String>> transactions = new HashMap<String, List<String>>();
			
	public InputBean(String schedule, String lockAnticipation, String lockType) throws InputBeanException {
		setIsLockAnticipation(lockAnticipation);
		setIsLockShared(lockType);		
		setSchedule(schedule);
	}

	private void setSchedule(String schedule) throws InputBeanException {
	
		String[] operations = schedule.split(" ");
		
		int index = 0;
		for(String operation: operations) {
			boolean isReadOrWrite = OperationUtils.isReadOrWrite(operation);
			boolean isCommit = OperationUtils.isCommit(operation);
			
			if(!(isReadOrWrite || isCommit)) {
				// only reads, writes and commits are allowed
				throw new InputBeanException(String.format(
						"Invalid operation: %s, index: %d"
						, operation, index
				)); 
			} else {
				
				String transactionNumber = OperationUtils.getTransactionNumber(
						operation
						);
				if (!this.transactions.containsKey(transactionNumber)) {
					// add the new transaction
					List<String> transactionOperations = new ArrayList<String>();
					transactionOperations.add(operation);
					this.transactions.put(transactionNumber, transactionOperations);
				} else {
					// check if the operation is legal
					if (this.transactions.get(transactionNumber).contains(String.format("c%s", transactionNumber))) {
						throw new InputBeanException(String.format(
								"Invalid transaction %s: already committed, operation %s at index %d"
								,transactionNumber, operation, index
						)); 
					}
					// append operation
					this.transactions.get(transactionNumber).add(operation);
				}
			}			
			index++;
		}
		this.schedule = Arrays.asList(operations);
	}

	private void setIsLockAnticipation(String lockAnticipation) throws InputBeanException {
		if(lockAnticipation.equals("True")) {
			this.isLockAnticipation = true;
		} else if (lockAnticipation.equals("")) {
			this.isLockAnticipation = false;
		} else {
			throw new InputBeanException("Invalid Lock Anticipation");
		}
	}
	
	private void setIsLockShared(String lockType) throws InputBeanException {
		if(lockType.equals("xl")) {
			this.isLockShared = false;
		} else if(lockType.equals("sl")) {
			this.isLockShared = true;
		} else {
			throw new InputBeanException("Invalid Lock Type");
		}
	}
	
	public List<String> getSchedule() {
		return this.schedule;
	}
	
	public Boolean getIsLockAnticipation() {
		return this.isLockAnticipation;
	}
	
	public Boolean getIsLockShared() {
		return this.isLockShared;
	}
	
	public HashMap<String, List<String>> getTransactions(){
		return this.transactions;
	}
	
}