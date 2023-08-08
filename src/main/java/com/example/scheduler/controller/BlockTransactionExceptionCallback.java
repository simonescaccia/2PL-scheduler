package com.example.scheduler.controller;

import com.example.scheduler.exception.DeadlockException;
import com.example.scheduler.exception.InternalErrorException;
import com.example.scheduler.exception.TransactionBlockedException;

public abstract class BlockTransactionExceptionCallback {
	
	String operation;
	String transactionLock;
	
	public BlockTransactionExceptionCallback() {}
	
	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public void setTransactionLock(String transactionLock) {
		this.transactionLock = transactionLock;
	}
	
	public abstract void run() throws InternalErrorException, TransactionBlockedException, DeadlockException;
}