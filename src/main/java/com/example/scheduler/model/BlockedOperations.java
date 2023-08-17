package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import com.example.scheduler.controller.OperationUtils;

public class BlockedOperations {
	
	List<String> blockedOperationsSchedule;
	
	public BlockedOperations() {
		blockedOperationsSchedule = new ArrayList<String>();
	}
	
	public void append(String operation) {
		this.blockedOperationsSchedule.add(operation);
	}
	
	public List<String> getTransactions() {
		List<String> transactions = new ArrayList<String>();
		for(String operation: this.blockedOperationsSchedule) {
			String operationTransaction = OperationUtils.getTransactionNumber(operation);
			if(!transactions.contains(operationTransaction)) {
				transactions.add(operationTransaction);
			}
		}
		return transactions;
	}
	
	public List<String> popTransactionOperations(String transaction) {
		List<String> operations = new ArrayList<String>();
		ListIterator<String> iter = this.blockedOperationsSchedule.listIterator();
		while(iter.hasNext()) {
			String operation = iter.next();
			String operationTransaction = OperationUtils.getTransactionNumber(operation);
			if(operationTransaction.equals(transaction)) {
				operations.add(operation);
				iter.remove();
			}
		}		
		return operations;
	}
	
	public boolean containsTransaction(String transaction) {
		return this.getTransactions().contains(transaction);
	}
	
	public List<String> getblockedOperationsSchedule() {
		return this.blockedOperationsSchedule;
	}
}