package com.example.scheduler.controller;

import com.example.scheduler.exception.DeadlockException;
import com.example.scheduler.exception.InternalErrorException;
import com.example.scheduler.exception.LockAnticipationException;
import com.example.scheduler.exception.TransactionBlockedException;
import com.example.scheduler.model.RequiredLocksToUnlockObject;

public abstract class UnableToUnlockExceptionCallback {
	
	String operation;
	String transactionLock;
	
	public UnableToUnlockExceptionCallback() {}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public void setTransactionLock(String transactionLock) {
		this.transactionLock = transactionLock;
	}
	
	public abstract void run(RequiredLocksToUnlockObject requiredLocksToUnlockObject) 
			throws InternalErrorException, TransactionBlockedException, DeadlockException;
}