package com.example.scheduler.controller;

import com.example.scheduler.exception.DeadlockException;
import com.example.scheduler.exception.TransactionBlockedException;

public abstract class BlockTransactionExceptionCallback {
	
	String operation;
	String transactionLock;
	
	public abstract void run() throws TransactionBlockedException, DeadlockException;
}