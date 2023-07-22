package com.example.scheduler.view;

import com.example.scheduler.exception.InputBeanException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.*;  

public class InputBean {
	String 	schedule;				// Input schedule
	Boolean isLockAnticipation;		// Enable lock anticipation
	Boolean isLockShared;			// Use both exclusive and shared locks
	
	HashMap<String, List<String>> transactions = new HashMap<String, List<String>>();
			
	public InputBean(String schedule, String lockAnticipation, String lockType) throws InputBeanException {
		setIsLockAnticipation(lockAnticipation);
		setIsLockShared(lockType);		
		setSchedule(schedule);
	}

	private void setSchedule(String schedule) throws InputBeanException {
		String regexReadWrite = "[rw][0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
		String regexCommit = "c[0-9]+";
		
		String[] operations = schedule.split(" ");
		
		for(String operation: operations) {
			if(!(Pattern.matches(regexReadWrite, operation) || Pattern.matches(regexCommit, operation))) {
				// only reads, writes and commits are allowed
				throw new InputBeanException(String.format(
						"Invalid operation: %s"
						, operation
				)); 
			} else {
				int numberEnd = operation.indexOf('(');
				if (numberEnd == -1) {
					// check if the operation is a commit
					numberEnd = operation.length();
				}
				String transactionNumber = operation.substring(1, numberEnd);
				if (!this.transactions.containsKey(transactionNumber)) {
					// add the new transaction
					List<String> transactionOperations = new ArrayList<String>();
					transactionOperations.add(operation);
					this.transactions.put(transactionNumber, transactionOperations);
				} else {
					// check if the operation is legal
					if (this.transactions.get(transactionNumber).contains(String.format("c%s", transactionNumber))) {
						throw new InputBeanException(String.format(
								"Invalid transaction %s: it has already committed"
								, transactionNumber
						)); 
					}
					// append operation
					this.transactions.get(transactionNumber).add(operation);
				}
			}
		}
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
	
	public String getSchedule() {
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