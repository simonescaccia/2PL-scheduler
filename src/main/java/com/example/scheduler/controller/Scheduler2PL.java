package com.example.scheduler.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.example.scheduler.exception.InternalErrorException;
import com.example.scheduler.exception.TransactionBlockedException;
import com.example.scheduler.model.WaitForGraph;
import com.example.scheduler.view.InputBean;
import com.example.scheduler.view.OutputBean;

public class Scheduler2PL {
	// View
	List<String> schedule;			// Input schedule
	Boolean isLockAnticipation;		// Enable lock anticipation
	Boolean isLockShared;			// Use both exclusive and shared locks
	HashMap<String, List<String>> transactions;
	
	// Internal computation
	HashMap<String, Boolean> isShrinkingPhase;
	HashMap<String, Integer> countOperations;
	HashMap<String, List<String>> lockTable;
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
		HashMap<String, List<String>> lockTable = new HashMap<String, List<String>>();
		for(String operation: schedule) {
			if(OperationUtils.isCommit(operation)) {
				// skip operations without objects
				continue;
			}
			String objectName = OperationUtils.getObjectName(operation);
			
			if(!lockTable.containsKey(objectName)) {
				List<String> locks = new ArrayList<String>();
				// first column represents the transaction which gets the shared lock
				// second column represents the transaction which gets the exclusive lock  
				locks.addAll(Arrays.asList("",""));
				lockTable.put(objectName, locks);	
			}
		}
		this.lockTable = lockTable;
		
		// Initialize the counter and the shrinking phase for each transaction
		this.countOperations = new HashMap<String, Integer>();
		this.isShrinkingPhase = new HashMap<String, Boolean>();
		for(String transaction: transactions.keySet()) {
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
		
		for(String operation: this.schedule) {
			this.incrementCountOperation(operation);
			// execute operation			
			try {
				this.lock(operation);
			} catch (TransactionBlockedException e) {
				// if an operation is blocked we can't execute them
				continue;
			}
			this.execute(operation);
			
			// resume blocked transaction if possible (only when lock anticipation is not used)
			if(!this.isLockAnticipation) {
				this.resume();
			}
		}
		this.unlockAllObjects();
		
		// return the schedule and the log
		OutputBean outputBean = new OutputBean(this.scheduleWithLocks, this.log, this.result);
		return outputBean;
	}

	/**
	 * Try to resume blocked transactions
	 */
	private void resume() {
		HashMap<String, Entry<String, String>> adjacencyList = this.waitForGraph.getAdjacencyList();
		for(String blockedTransaction: adjacencyList.keySet()) {
			// try to unlock blocked transaction
			String object = adjacencyList.get(blockedTransaction).getKey();
			String waitForTransaction = adjacencyList.get(blockedTransaction).getValue();
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

	private void incrementCountOperation(String operation) {
		String transaction = OperationUtils.getTransactionNumber(operation);
		this.countOperations.put(transaction, this.countOperations.get(transaction) + 1);
	}

	private void execute(String operation) {
		this.scheduleWithLocks.add(operation);
	}

	private void lock(String operation) throws TransactionBlockedException {
		
		if(this.isLockAnticipation) {
			this.lockWithLockAnticipation(operation);
		} else {
			this.lockWithoutLockAnticipation(operation);
		}
	}
	
	private void lockWithLockAnticipation(String operation) {
		
	}
	
	private void lockWithoutLockAnticipation(String operation) throws TransactionBlockedException {
		String transactionNumber = OperationUtils.getTransactionNumber(operation);
		
		// Check lock type
		if(this.isLockShared) {
			// shared lock  logic
		} else {
			// exclusive lock logic
			
			// check if the transaction is blocked
			if(this.blockedOperations.containsKey(transactionNumber)) {
				this.blockedOperations.get(transactionNumber).add(operation);
				throw new TransactionBlockedException();
			}
			
			if(OperationUtils.isCommit(operation)) {
				// commit doesn't need to lock objects
				return;				
			}
			
			// lock object
			String objectName = OperationUtils.getObjectName(operation);
			String transactionLock = this.lockTable.get(objectName).get(1);
			if(transactionLock.equals(transactionNumber)) {
				// object already locked by the same transaction
				return;
			}
			if(!transactionLock.equals("")) {
				// the object is already locked by another transaction, try to unlock
				try {
					this.unlock(transactionLock, objectName);
				} catch (TransactionBlockedException e) {
					// can't unlock the object
					this.waitForGraph.addEdge(transactionNumber, transactionLock, objectName);	// transaction is blocked
					this.log.add(String.format(
							"Transaction %s blocked, waiting for transaction %s on object %s", 
							transactionNumber, transactionLock, objectName
							));
					
					// operation is blocked
					List<String> blockedOperationsList = new ArrayList<String>();
					blockedOperationsList.add(operation);
					this.blockedOperations.put(transactionNumber, blockedOperationsList);
					throw new TransactionBlockedException();
				}
			}
			
			// case: transactionLock.equals("") or unlock completed. Free object
			List<String> newLocks = new ArrayList<String>(); 
			newLocks.addAll(Arrays.asList("",transactionNumber));
			this.lockTable.put(objectName, newLocks);		// update lockTable's exclusive lock
			addLockToFinalSchedule(operation);				// add the write lock to the final schedule

		}
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
			if(OperationUtils.use(operation, objectName) && this.isShrinkingPhase.get(transactionLock)) {
				// we need to use the object
				unlock = false;
				break;
			}
			// check if we can start the shrinking phase if it is not already started
			if(!this.isShrinkingPhase.get(transactionLock) ||
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
		
		if(unlock == false || !this.isShrinkingPhase.get(transactionLock)) {
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