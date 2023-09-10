package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import com.example.scheduler.controller.OperationUtils;

public class BlockedOperations {
	List<String> blockedTransactions;
	List<String> blockedOperationsSchedule;
	
	public BlockedOperations() {
		this.blockedOperationsSchedule = new ArrayList<String>();
		this.blockedTransactions = new ArrayList<String>();
	}
	
	public void append(String operation) {
		// add operation
		this.blockedOperationsSchedule.add(operation);
		// add transaction
		String transaction = OperationUtils.getTransactionNumber(operation);
		if(!this.containsTransaction(transaction)) {
			this.blockedTransactions.add(transaction);
		}
	}
	
	public List<String> getTransactions() {
		return this.blockedTransactions;
	}
	
	public List<String> getTransactionsByObject(String object) {
		List<String> transactions = new ArrayList<String>();
		for(String operation: this.blockedOperationsSchedule) {
			String operationTransaction = OperationUtils.getTransactionNumber(operation);
			String operationObject = OperationUtils.getObjectName(operation);
			if(!transactions.contains(operationTransaction) && operationObject.equals(object)) {
				transactions.add(operationTransaction);
			}
		}
		return transactions;
	}
	
	/**
	 * Remove the first operation of the transaction
	 * @param transaction
	 */
	public List<String> popOperationByTransaction(String transaction) {
		List<String> operations = new ArrayList<String>();
		ListIterator<String> iter = this.blockedOperationsSchedule.listIterator();
		while(iter.hasNext()) {
			String operation = iter.next();
			String operationTransaction = OperationUtils.getTransactionNumber(operation);
			if(operationTransaction.equals(transaction)) {
				operations.add(operation);
				iter.remove();
				break;
			}
		}		
		return operations;
	}
	
	public List<String> removeBlockedTransaction(String transaction) {
		// remove blocked transaction
		this.blockedTransactions.remove(transaction);
		
		// return all the blocked operations
		List<String> operations = new ArrayList<String>();
		for(String operation: this.blockedOperationsSchedule) {
			String operationTransaction = OperationUtils.getTransactionNumber(operation);
			if(operationTransaction.equals(transaction)) {
				operations.add(operation);
			}
		}
		return operations;
	}
	
	public boolean containsTransaction(String transaction) {
		return this.blockedTransactions.contains(transaction);
	}
	
	public List<String> getblockedOperationsSchedule() {
		return this.blockedOperationsSchedule;
	}
}