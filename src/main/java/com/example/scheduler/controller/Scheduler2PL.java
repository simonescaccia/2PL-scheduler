package com.example.scheduler.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.scheduler.exception.DeadlockException;
import com.example.scheduler.exception.InternalErrorException;
import com.example.scheduler.exception.TransactionBlockedException;
import com.example.scheduler.model.WaitForGraph;
import com.example.scheduler.view.InputBean;
import com.example.scheduler.view.OutputBean;

public class Scheduler2PL {
	// Debug
	Logger logger = Logger.getLogger(Scheduler2PL.class.getName());
	
	// View
	List<String> schedule;			// Input schedule
	Boolean isLockAnticipation;		// Enable lock anticipation
	Boolean isLockShared;			// Use both exclusive and shared locks
	HashMap<String, List<String>> transactions;
	
	// Internal computation
	HashMap<String, Boolean> isShrinkingPhase;
	HashMap<String, Integer> countOperations;
	HashMap<String, Entry<List<String>, String>> lockTable;
	WaitForGraph waitForGraph;
	HashMap<String, List<String>> blockedOperations;	// For each blocked transaction store the operations to execute after unblocking
	
	// Output
	List<String> scheduleWithLocks;
	List<String> log;
	Boolean result;
	
	public Scheduler2PL(InputBean iB) {
		this.schedule = iB.getSchedule();
		this.isLockAnticipation = iB.getIsLockAnticipation();
		this.isLockShared = iB.getIsLockShared();
		this.transactions = iB.getTransactions();
		
		// Build the lock table, one row for each object
		HashMap<String, Entry<List<String>, String>> lockTable = new HashMap<String, Entry<List<String>, String>>();
		for(String operation: schedule) {
			if(OperationUtils.isCommit(operation)) {
				// skip operations without objects
				continue;
			}
			String objectName = OperationUtils.getObjectName(operation);
			
			if(!lockTable.containsKey(objectName)) {
				List<String> sharedLocks = new ArrayList<String>();
				// first column represents the transactions which get the shared lock
				// second column represents the transaction which gets the exclusive lock
				Entry<List<String>, String> locks = Map.entry(sharedLocks, "");
				lockTable.put(objectName, locks);	
			}
		}
		this.lockTable = lockTable;
		
		// Initialize the counter and the shrinking phase for each transaction
		this.countOperations = new HashMap<String, Integer>();
		this.isShrinkingPhase = new HashMap<String, Boolean>();
		for(String transaction: this.transactions.keySet()) {
			this.countOperations.put(transaction, 0);
			this.isShrinkingPhase.put(transaction, false);
		}
		
		this.scheduleWithLocks = new ArrayList<String>();
		this.waitForGraph = new WaitForGraph();
		this.blockedOperations = new HashMap<String, List<String>>();
		this.log = new ArrayList<String>();
		this.result = true;
	}
	
	public OutputBean check() throws InternalErrorException {
		
		try {
			this.executeSchedule(this.schedule);
			this.unlockAllObjects();
		} catch (DeadlockException e) {
			this.result = false;
		}
		
		// return the schedule and the log
		OutputBean outputBean = new OutputBean(this.scheduleWithLocks, this.log, this.result);
		return outputBean;
	}

	/**
	 * @param schedule: the list of operations to execute
	 * @throws DeadlockException 
	 */
	private void executeSchedule(List<String> schedule) throws DeadlockException {
		for(String operation: schedule) {
			logger.log(Level.INFO, String.format("Trying to execute %s", operation));
			// execute operation			
			try {
				this.lock(operation);
				logger.log(Level.INFO, "Lock completed");
			} catch (TransactionBlockedException e) {
				// if an operation is blocked we can't execute them
				logger.log(Level.INFO, "Unable to lock, transaction blocked");
				continue;
			} catch (DeadlockException e) {
				logger.log(Level.INFO, "Deadlock");
				throw new DeadlockException();
			}
			this.execute(operation);
			this.incrementExecutedOperation(operation);
			
			// resume blocked transaction if possible (only when lock anticipation is not used)
			if(!this.isLockAnticipation) {
				this.resume();
			}
		}
	}
	
	/**
	 * Try to resume blocked transactions
	 * @throws DeadlockException 
	 */
	private void resume() throws DeadlockException {
		HashMap<String, Entry<String, String>> adjacencyList = this.waitForGraph.getAdjacencyList();
		for(String blockedTransaction: adjacencyList.keySet()) {
			// try to unlock blocked transaction
			String waitForTransaction = adjacencyList.get(blockedTransaction).getKey();
			String object = adjacencyList.get(blockedTransaction).getValue();
			try {
				this.unlock(waitForTransaction, object);
				logger.log(Level.INFO, String.format("Resuming blocked transaction %s", blockedTransaction));
				// remove the blocked transaction to the waitForGraph
				this.waitForGraph.removeEdge(blockedTransaction);
				this.log.add(String.format(
						"Transaction %s resumed, object %s unlocked by transaction %s", 
						blockedTransaction, object, waitForTransaction
						));
				// unlock done, then remove the transaction form blocked transactions and execute it
				List<String> blockedOperations = this.blockedOperations.get(blockedTransaction);
				this.blockedOperations.remove(blockedTransaction);
				this.executeSchedule(blockedOperations);
				// after resuming the first transaction the adjacencyList may changes, then we continue to resume transaction recursively  
				break;
			} catch (TransactionBlockedException e) {
				// the transaction can't be resumed
				continue;
			}
		}
	}

	private void unlockAllObjects() throws InternalErrorException {
		for(String object: this.lockTable.keySet()) {
			if(this.isLockAnticipation) {
				 
			} else {
				// only exclusive locks
				String transactionLock = this.lockTable.get(object).get(1);
				if(!transactionLock.equals("")) {
					// unlock
					try {
						this.unlock(transactionLock, object);
					} catch (TransactionBlockedException e) {
						throw new InternalErrorException("Internal error during the unlocking phase");
					}
				}
			}
		}
	}

	private void incrementExecutedOperation(String operation) {
		String transaction = OperationUtils.getTransactionNumber(operation);
		this.countOperations.put(transaction, this.countOperations.get(transaction) + 1);
	}

	private void execute(String operation) {
		this.scheduleWithLocks.add(operation);
	}
	
	private void lock(String operation) throws TransactionBlockedException, DeadlockException {
		String transactionNumber = OperationUtils.getTransactionNumber(operation);
		
		// check if the transaction is blocked
		if(this.blockedOperations.containsKey(transactionNumber)) {
			this.blockedOperations.get(transactionNumber).add(operation);
			throw new TransactionBlockedException();
		}
		
		// commits don't need to lock objects
		if(OperationUtils.isCommit(operation)) {
			return;				
		}
		
		// lock object
		String objectName = OperationUtils.getObjectName(operation);
		Boolean isRead = OperationUtils.isRead(operation);
		Integer objectState = this.getObjectState(objectName, transactionNumber, isRead);
		switch(objectState) {
			case 0:
				// object already locked by the same transaction
				return;
			case 1:
				// free object, lock
				//TODO
				break;
			case 2:
				// the object is already locked by another transaction, try to unlock
				String transactionLock = this.lockTable.get(objectName).getValue();
				try {
					this.unlock(transactionLock, objectName);
				} catch (TransactionBlockedException e) {
					// can't unlock the object
					this.blockTransaction(operation, transactionLock);
				}
				break;
		}
		
		// case: transactionLock.equals("") or unlock completed. Free object
		List<String> newLocks = new ArrayList<String>(); 
		newLocks.addAll(Arrays.asList("",transactionNumber));
		this.lockTable.put(objectName, newLocks);		// update lockTable's exclusive lock
		addLockToFinalSchedule(operation);				// add the write lock to the final schedule

	}
	
	/**
	 * Check if the object is already locked by the same transaction, not locked or locked by another transaction 
	 * @param objectName
	 * @param transactionNumber
	 * @return 0 if the object is already locked by the same transaction, 
	 * 1 if the object is free or 2 if the object is locked by another transaction
	 */
	private Integer getObjectState(String objectName, String transactionNumber, Boolean isRead) {
		String exclusiveTransactionLock = this.lockTable.get(objectName).getValue();	// transaction who has the exclusive lock
		List<String> sharedTransactionsLock = this.lockTable.get(objectName).getKey();	// transaction who has the exclusive lock
		
		if(!exclusiveTransactionLock.equals(transactionNumber) && !exclusiveTransactionLock.equals("")) {
			// object already locked by another transaction
			return 2;
		}
		if(exclusiveTransactionLock.equals(transactionNumber)) {
			// object already locked by the same transaction
			return 0;
		}
		if(exclusiveTransactionLock.equals("")) {
			if(!this.isLockShared) {
				// exclusive lock case: free object
				return 1;
			}
			// shared lock case
			if(!isRead) {
				// shared lock case, write operation: free object
				return 1;
			}
			// read case
			if(sharedTransactionsLock.contains(transactionNumber)) {
				// object already locked
				return 0;
			}
			// free object
			return 1;
		}
		return null;
	}

	private void blockTransaction(String operation, String transactionLock) throws TransactionBlockedException, DeadlockException {
		String objectName = OperationUtils.getObjectName(operation);
		String transactionNumber = OperationUtils.getTransactionNumber(operation);

		this.log.add(String.format(
				"Transaction %s blocked, waiting for transaction %s on object %s", 
				transactionNumber, transactionLock, objectName
				));
		
		// operation is blocked
		List<String> blockedOperationsList = new ArrayList<String>();
		blockedOperationsList.add(operation);
		this.blockedOperations.put(transactionNumber, blockedOperationsList);
			
		try {
			this.waitForGraph.addEdge(transactionNumber, transactionLock, objectName); // transaction is blocked
		} catch (DeadlockException e) {
			this.log.add(e.getMessage());
			throw new DeadlockException();
		}	
	
		throw new TransactionBlockedException();
	}

	/**
	 * If the transaction doesn't need anymore the lock on objectName and it has already all the locks, execute the unlock
	 * @param transactionLock: the transaction who has the lock on objectName
	 * @param objectName: object locked
	 * @throws TransactionBlockedException 
	 */
	private void unlock(String transactionLock, String objectName) throws TransactionBlockedException {
		List<String> operations = this.transactions.get(transactionLock);
		Integer executedOperations = this.countOperations.get(transactionLock);
		Integer operationsListLenght = operations.size(); 
		List<String> remainingOperations = operations.subList(executedOperations, operationsListLenght);
		
		Boolean unlock = true;
		Boolean isShrinkingPhasePossible = true;
		
		for(String operation: remainingOperations) {
			if(OperationUtils.isCommit(operation)) {
				continue;
			}
			// check if remainingOperations contains operations on the object
			if(OperationUtils.use(operation, objectName)) {
				// we need to use the object
				unlock = false;
				break;
			}
			// check if we can start the shrinking phase if it is not already started
			if(!this.isShrinkingPhase.get(transactionLock) &&
			   (
			    !OperationUtils.use(operation, objectName) && 
				!this.lockTable.get(OperationUtils.getObjectName(operation)).get(1).equals(transactionLock)
			    )
			 ) {
				// we need to lock another object first
				isShrinkingPhasePossible = false;
				break;
			}
		}
		   
		if(!this.isShrinkingPhase.get(transactionLock) && isShrinkingPhasePossible) {
			// start the shrinking phase
			this.isShrinkingPhase.put(transactionLock, true);
		}
		
		if(!unlock || !this.isShrinkingPhase.get(transactionLock)) {
			throw new TransactionBlockedException();
		}	
		
		// unlock the object
		this.lockTable.get(objectName).set(1, "");
		this.scheduleWithLocks.add(OperationUtils.createOperation("u", transactionLock, objectName));
		
	}

	private void addLockToFinalSchedule(String operation) {
		String transaction = OperationUtils.getTransactionNumber(operation);
		String object = OperationUtils.getObjectName(operation);
		Boolean isRead = OperationUtils.isRead(operation);
		String lock;
		if(this.isLockShared) {
			if(isRead) {
				lock = "sl";
			} else {
				lock = "xl";
			}
		} else {
			lock = "l";
		}
		this.scheduleWithLocks.add(OperationUtils.createOperation(lock, transaction, object));
	}

	
}