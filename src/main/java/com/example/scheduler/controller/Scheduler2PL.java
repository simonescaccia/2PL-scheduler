package com.example.scheduler.controller;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.scheduler.exception.UnableToLockException;
import com.example.scheduler.exception.DeadlockException;
import com.example.scheduler.exception.InternalErrorException;
import com.example.scheduler.exception.LockAnticipationException;
import com.example.scheduler.exception.TransactionBlockedException;
import com.example.scheduler.model.RequiredLocksToUnlockObject;
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
	HashMap<String, Integer> countOperations; // index of the last executed operation for each transaction
	Integer countOperationsSchedule;	// index of the last executed operation in the schedule
	HashMap<String, SimpleEntry<List<String>, String>> lockTable;
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
		this.lockTable = new HashMap<String, SimpleEntry<List<String>, String>>();
		for(String operation: schedule) {
			if(OperationUtils.isCommit(operation)) {
				// skip operations without objects
				continue;
			}
			String objectName = OperationUtils.getObjectName(operation);
			
			if(!this.lockTable.containsKey(objectName)) {
				List<String> sharedLocks = new ArrayList<String>();
				// first column represents the transactions which get the shared lock
				// second column represents the transaction which gets the exclusive lock
				SimpleEntry<List<String>, String> locks = new AbstractMap.SimpleEntry<List<String>, String>(sharedLocks, "");
				this.lockTable.put(objectName, locks);
			}
		}
		
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
		this.countOperationsSchedule = 0;
	}
	
	public OutputBean check() throws InternalErrorException {
		try {
			this.executeSchedule(this.schedule, false);
			this.unlockAllObjects();
		} catch (DeadlockException | LockAnticipationException e) {
			this.result = false;
		}
		
		// return the schedule and the log
		OutputBean outputBean = new OutputBean(this.scheduleWithLocks, this.log, this.result);
		return outputBean;
	}

	/**
	 * @param schedule: the list of operations to execute
	 * @throws DeadlockException 
	 * @throws InternalErrorException 
	 * @throws LockAnticipationException 
	 */
	private void executeSchedule(List<String> schedule, Boolean areBlockedOperations) 
			throws DeadlockException, InternalErrorException, LockAnticipationException {
		for(String operation: schedule) {
			logger.log(Level.INFO, String.format("Trying to execute %s", operation));
			// execute operation			
			try {
				this.lock(operation);
				logger.log(Level.INFO, "Lock completed");
			} catch (TransactionBlockedException e) {
				// if an operation is blocked we can't execute them
				logger.log(Level.INFO, "Unable to lock, transaction blocked");
				this.incrementScheduleCounter(areBlockedOperations);
				continue;
			} catch (DeadlockException e) {
				logger.log(Level.INFO, "Deadlock");
				throw new DeadlockException();
			}
			this.execute(operation);
			this.incrementExecutedOperation(operation);
			this.incrementScheduleCounter(areBlockedOperations);
			
			// resume blocked transaction if possible
			this.resume();
		}
	}

	/**
	 * Try to resume blocked transactions
	 * @throws DeadlockException 
	 * @throws InternalErrorException 
	 * @throws  
	 */
	private void resume() throws DeadlockException, InternalErrorException {
		HashMap<String, Entry<String, String>> adjacencyList = this.waitForGraph.getAdjacencyList();
		for(String blockedTransaction: adjacencyList.keySet()) {
			logger.log(Level.INFO, String.format("Try to resuming blocked transaction %s", blockedTransaction));
			// try to unlock blocked transaction
			String waitForTransaction = adjacencyList.get(blockedTransaction).getKey();
			String object = adjacencyList.get(blockedTransaction).getValue();
			
			UnableToLockExceptionCallback callback = new UnableToLockExceptionCallback() {
				@Override
				public void run(RequiredLocksToUnlockObject requiredLocksToUnlockObject) {
					// the transaction can't be resumed, it is also already blocked
					return;
				}
			};
			
			try {
				if(!this.unlockSetExceptionCallback(waitForTransaction, object, callback)) {
					continue;
				}
			} catch (TransactionBlockedException | LockAnticipationException e) {}
			
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
			try {
				this.executeSchedule(blockedOperations, true);
			} catch (LockAnticipationException e) {}
			// after resuming the first transaction the adjacencyList may changes, then we continue to resume transaction recursively  
			break;
		}
	}

	private void unlockAllObjects() throws InternalErrorException {
		for(String object: this.lockTable.keySet()) {
			UnableToLockExceptionCallback callback;
			// unlock exclusive lock
			callback = new UnableToLockExceptionCallback() {
				@Override
				public void run(RequiredLocksToUnlockObject requiredLocksToUnlockObject) throws InternalErrorException {
					throw new InternalErrorException("Internal error during the unlocking phase of exclusive locks");
				}
			};
			try {
				this.unlockExclusiveLock(object, callback);
			} catch (TransactionBlockedException | DeadlockException | LockAnticipationException e) {}
			
			// unlock shared locks
			callback = new UnableToLockExceptionCallback() {
				@Override
				public void run(RequiredLocksToUnlockObject requiredLocksToUnlockObject) throws InternalErrorException {
					throw new InternalErrorException("Internal error during the unlocking phase shared locks");
				}
			};
			try {
				this.unlockAllSharedLocks(object, callback);
			} catch (TransactionBlockedException | DeadlockException | LockAnticipationException e) {}
		}
	}
	
	private void incrementExecutedOperation(String operation) {
		if(!this.isLockAnticipation) {
			String transaction = OperationUtils.getTransactionNumber(operation);
			this.countOperations.put(transaction, this.countOperations.get(transaction) + 1);
		}
	}
	
	private void incrementScheduleCounter(Boolean areBlockedOperations) {
		if(this.isLockAnticipation && !areBlockedOperations) {
			// blocked operations are already counted
			this.countOperationsSchedule += 1;
		}	
	}

	private void execute(String operation) {
		this.scheduleWithLocks.add(operation);
	}
	
	private void lock(String operation) throws TransactionBlockedException, DeadlockException, InternalErrorException, LockAnticipationException {
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
		UnableToLockExceptionCallback unableToLockCallback = new UnableToLockExceptionCallback() {
			@Override
			public void run(RequiredLocksToUnlockObject requiredLocksToUnlockObject) 
					throws TransactionBlockedException, DeadlockException, LockAnticipationException, InternalErrorException {
				// can't unlock the object
				if(!Scheduler2PL.this.isLockAnticipation) {
					Scheduler2PL.this.blockTransaction(
							this.operation,
							this.transactionLock);
				} else {
					Scheduler2PL.this.anticipateLocks(
							this.operation,
							requiredLocksToUnlockObject);
				}
			}	
		};
		unableToLockCallback.setOperation(operation);
		String objectName = OperationUtils.getObjectName(operation);
		Boolean isRead = OperationUtils.isRead(operation);
		Integer objectState = this.getObjectState(transactionNumber, objectName, isRead);
		logger.log(Level.INFO, String.format("Object state: %d", objectState));
		switch(objectState) {
			case 0:
				// object already locked by the same transaction
				return;
			case 1:
				// free object, lock
				this.insertLock(transactionNumber, objectName, isRead, operation);
				return;
			case 2:
				// the object is already locked by another transaction through an exclusive lock, try to unlock
				this.unlockExclusiveLock(objectName, unableToLockCallback);
				this.insertLock(transactionNumber, objectName, isRead, operation);
				return;
			case 3:
				// upgrade lock
				this.upgradeLock(transactionNumber, objectName, isRead, operation);
				return;
			case 4:
				// the object is already locked by another transaction through a shared lock, try to unlock
				this.unlockAllSharedLocks(objectName, unableToLockCallback);
				this.insertLock(transactionNumber, objectName, isRead, operation);
				return;
			default:
				throw new InternalErrorException("Internal error: locking situation not managed");
		}
	}
	
	private void anticipateLocks(String operation, RequiredLocksToUnlockObject requiredLocksToUnlockObject) 
			throws LockAnticipationException, InternalErrorException, TransactionBlockedException, DeadlockException {
		// check if lock anticipation is possible
		if(!requiredLocksToUnlockObject.getIsLastUsage()) {
			String lockOperation = requiredLocksToUnlockObject.getOtherUsageOperations().get(0);
			logger.log(Level.INFO, String.format("anticipateLocks operation %s", operation));
			this.blockTransaction(operation, OperationUtils.getTransactionNumber(lockOperation));
		} else {
			// check if lock anticipation is possible
			this.checkLockAnticipation(operation, requiredLocksToUnlockObject);
			
			UnableToLockExceptionCallback callback = new UnableToLockExceptionCallback() {
				@Override
				public void run(RequiredLocksToUnlockObject requiredLocksToUnlockObject) 
						throws LockAnticipationException, InternalErrorException, TransactionBlockedException, DeadlockException {
					Scheduler2PL.this.anticipateLocks(
							this.operation,
							requiredLocksToUnlockObject);			
				}
			};
			for(String object: requiredLocksToUnlockObject.getOtherRequiredLocks().keySet()) {
				this.unlockSetExceptionCallback(requiredLocksToUnlockObject.getTransactionToUnlock(), object, callback);
			}
		}
	}

	private void checkLockAnticipation(String lockOperation, RequiredLocksToUnlockObject requiredLocksToUnlockObject) 
			throws LockAnticipationException {
		// compute the check for each object to unlock
		for(String object: requiredLocksToUnlockObject.getOtherRequiredLocks().keySet()) {
			Boolean isOtherTransactionsOperationWrite = false;
			Boolean isTransactionOperationWrite = false;
			String otherTransactionFirstOperation = "";
			String otherTransactionFirstRead = "";
			String otherTransactionFirstWrite = "";
			String transactionOperation = "";
			
			for(String operation: requiredLocksToUnlockObject.getOtherRequiredLocks().get(object)) {
				String transaction = OperationUtils.getTransactionNumber(operation);
				Boolean isWrite = OperationUtils.isWrite(operation);
				
				if(transaction.equals(requiredLocksToUnlockObject.getTransactionToUnlock())) {
					transactionOperation = operation;
					isTransactionOperationWrite = isWrite; 
				} else if (!transaction.equals(requiredLocksToUnlockObject.getTransactionToUnlock())) {
					if(otherTransactionFirstOperation.equals("")) {
						otherTransactionFirstOperation = operation;
					}
					if(isWrite && otherTransactionFirstWrite.equals("")) {
						otherTransactionFirstWrite = operation;
					}
					if(!isWrite && otherTransactionFirstRead.equals("")) {
						otherTransactionFirstRead = operation;
					}
					if(isWrite && !isOtherTransactionsOperationWrite) {
						isOtherTransactionsOperationWrite = true;
					}
				}
				
				// live check
				if(!transactionOperation.equals("")) {
					String errorMessage = "During locking %s, unable to unlock the object %s locked by transaction %s. "
							+ "Unable to anticipate lock for %s due to %s";
					if(!this.isLockShared) {
						if(!otherTransactionFirstOperation.equals("")) {
							this.log.add(String.format(errorMessage,
									operation,
									object,
									transaction,
									transactionOperation,
									otherTransactionFirstOperation));
							throw new LockAnticipationException();
						}
					} else {
						if(isOtherTransactionsOperationWrite) {
							this.log.add(String.format(errorMessage, 
											operation,
											object,
											transaction,
											transactionOperation,
											otherTransactionFirstWrite));
							throw new LockAnticipationException();
						} else if(!isOtherTransactionsOperationWrite && isTransactionOperationWrite) {
							this.log.add(String.format(errorMessage, 
											operation,
											object,
											transaction,
											transactionOperation,
											otherTransactionFirstRead));
							throw new LockAnticipationException();
						}
					}
					
					// live check passed, then reset settings
					transactionOperation = "";
				}
			}
		}
	}
	
	private void upgradeLock(String transactionNumber, String objectName, Boolean isRead, String operation) 
			throws TransactionBlockedException, DeadlockException, LockAnticipationException {
		// remove the shared lock from the lock table
		this.lockTable.get(objectName).getKey().remove(transactionNumber);
		
		UnableToLockExceptionCallback unableToLockCallback = new UnableToLockExceptionCallback() {
			@Override
			public void run(RequiredLocksToUnlockObject requiredLocksToUnlockObject) throws TransactionBlockedException, DeadlockException {
				// can't unlock the object
				Scheduler2PL.this.blockTransaction(
						this.operation,
						this.transactionLock);
			}	
		};
		unableToLockCallback.setOperation(operation);
		
		try {
			this.unlockAllSharedLocks(objectName, unableToLockCallback);
		} catch (InternalErrorException e) {}
		// update the exclusive lock
		this.insertLock(transactionNumber, objectName, isRead, operation);
	}

	/**
	 * 
	 * @param transactionLock
	 * @param objectName
	 * @param onExceptionCallback
	 * @return true if the catch block is not executed, else false
	 * @throws TransactionBlockedException
	 * @throws DeadlockException
	 * @throws InternalErrorException 
	 * @throws LockAnticipationException 
	 */
	private Boolean unlockSetExceptionCallback(
			String transactionLock, 
			String objectName,  
			UnableToLockExceptionCallback onExceptionCallback) 
					throws TransactionBlockedException, DeadlockException, InternalErrorException, LockAnticipationException {
		try {
			this.unlock(transactionLock, objectName);
		} catch (UnableToLockException e) {
			onExceptionCallback.run(e.getRequiredLocksToUnlock());
			return false;
		}
		return true;
	}
	
	private void unlockExclusiveLock(
			String objectName, 
			UnableToLockExceptionCallback callback
			) throws InternalErrorException, TransactionBlockedException, DeadlockException, LockAnticipationException {
		// the object is already locked by another transaction through an exclusive lock, try to unlock
		String exclusiveTransactionLock = this.lockTable.get(objectName).getValue();
		if(exclusiveTransactionLock.equals("")) {
			return;
		}
		callback.setTransactionLock(exclusiveTransactionLock);
		this.unlockSetExceptionCallback(exclusiveTransactionLock, objectName, callback);	

	}
	
	private void unlockAllSharedLocks(String objectName, UnableToLockExceptionCallback callback) throws TransactionBlockedException, DeadlockException, InternalErrorException, LockAnticipationException {
		// unlock all the other shared locks
		List<String> sharedLocks = new ArrayList<String>(this.lockTable.get(objectName).getKey());
		for(String transactionWithSharedLock: sharedLocks) {
			callback.setTransactionLock(transactionWithSharedLock);
			this.unlockSetExceptionCallback(transactionWithSharedLock, objectName, callback);
		}
	}
	
	private void insertLock(String transactionNumber, String objectName, Boolean isRead, String operation) {
		if(!isRead) {
			// write operation
			this.lockTable.get(objectName).setValue(transactionNumber);	// update lockTable's exclusive lock
		} else {
			// read operation
			if(this.isLockShared) {
				this.lockTable.get(objectName).getKey().add(transactionNumber); // update lockTable's shared lock
			} else {
				this.lockTable.get(objectName).setValue(transactionNumber);	// update lockTable's exclusive lock
			}
		}
		this.addLockToFinalSchedule(operation);			// add the write lock to the final schedule
	}

	/**
	 * Check if the object is already locked by the same transaction, not locked or locked by another transaction 
	 * @param objectName
	 * @param transactionNumber
	 * @return 0 if the object is already locked by the same transaction, 
	 * 1 if the object is free, 2 if the object is locked by another transaction, 3 for upgrading lock need
	 */
	private Integer getObjectState(String transactionNumber, String objectName, Boolean isRead) {
		String exclusiveTransactionLock = this.lockTable.get(objectName).getValue();	// transaction who has the exclusive lock
		List<String> sharedTransactionsLock = this.lockTable.get(objectName).getKey();	// transaction who has the exclusive lock
		
		if(!exclusiveTransactionLock.equals("") && 
		   !exclusiveTransactionLock.equals(transactionNumber)) {
			// object already locked by another transaction: exclusive lock
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
				if(sharedTransactionsLock.contains(transactionNumber)) {
					// upgrading lock
					return 3;
				}
				if(sharedTransactionsLock.size() > 0) {
					// object already locked through a shared lock
					return 4;
				}
				if(sharedTransactionsLock.size() == 0) {
					// free object
					return 1;
				}
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

	public void blockTransaction(String operation, String transactionLock) throws TransactionBlockedException, DeadlockException {
		logger.log(Level.INFO, String.format("operation: %s", operation));

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
	 * If the transaction doesn't need anymore the lock on objectName and it has already all the locks for the other objects, 
	 * execute the unlock
	 * @param transactionLock: the transaction who has the lock on objectName
	 * @param objectName: object locked
	 * @throws TransactionBlockedException 
	 */
	private void unlock(String transactionLock, String objectName) throws UnableToLockException {
		List<String> remainingSchedule = this.getRemainingSchedule(transactionLock);
		logger.log(Level.INFO, String.format("RemainingOperations: %s", remainingSchedule));

		RequiredLocksToUnlockObject requiredLocksToUnlock = new RequiredLocksToUnlockObject(
				transactionLock, objectName, this.isLockShared);
		Boolean unlock = true;
		Boolean isShrinkingPhasePossible = true;
		HashMap<String, List<String>> objectSubschedule = new HashMap<String, List<String>>();

		for(String operation: remainingSchedule) {
			
			if(OperationUtils.isCommit(operation)) {
				continue;
			}
			
			String object = OperationUtils.getObjectName(operation);
			String transaction = OperationUtils.getTransactionNumber(operation);
			Boolean opUseObj = OperationUtils.use(operation, objectName);
			
			if(this.isLockAnticipation) {
				// save operations for future lock anticipation checks 
				if(!objectSubschedule.containsKey(object)) {
					objectSubschedule.put(object, new ArrayList<String>());	
				}
				objectSubschedule.get(object).add(operation);
				// skip not concurrent operations or operations from different transactions
				if(!object.equals(objectName) && 
				   !transaction.equals(transactionLock)) {
					continue;
				}
			}
			
			// check if remainingOperations contains operations on the object
			if(opUseObj && transaction.equals(transactionLock)) {
				// we need to use the object
				if(this.isLockAnticipation) {
					requiredLocksToUnlock.setLastUsageFalse(operation);
				}
				unlock = false;
			}
			// check if we can start the shrinking phase if it is not already started
			if(!this.isShrinkingPhase.get(transactionLock) &&	// shrinking phase not started
			    !opUseObj && 	// operation on an object, different from the object to unlock 
				this.getObjectState(
						transaction,
						object,
						OperationUtils.isRead(operation)
						) != 0 // check if the object is already locked
			 ) {
				// we need to lock another object first
				if(this.isLockAnticipation) {
					requiredLocksToUnlock.addRequiredLock(object, new ArrayList<String>(objectSubschedule.get(object)));
				}
				isShrinkingPhasePossible = false;
			}
		}
		
		if(!this.isShrinkingPhase.get(transactionLock) && isShrinkingPhasePossible) {
			// start the shrinking phase
			this.isShrinkingPhase.put(transactionLock, true);
		}
		
		if(!unlock || !this.isShrinkingPhase.get(transactionLock)) {
			if(!unlock) {
				logger.log(Level.INFO, String.format("Unable to lock the object %s: lock must be reaccessed", objectName));
			} else {
				logger.log(Level.INFO, String.format("Unable to lock the object %s, can't start the shrinking phase", objectName));
			}
			throw new UnableToLockException(requiredLocksToUnlock);
		}
		
		// unlock the object
		String exclusiveTransactionLock = this.lockTable.get(objectName).getValue();
		if(exclusiveTransactionLock.equals(transactionLock)) {
			this.lockTable.get(objectName).setValue("");
		} else {
			this.lockTable.get(objectName).getKey().remove(transactionLock);
		}
		this.scheduleWithLocks.add(OperationUtils.createOperation("u", transactionLock, objectName));
		
	}

	private List<String> getRemainingSchedule(String transactionLock) {
		if(!this.isLockAnticipation) {
			// transaction remaining operations
			List<String> operations = this.transactions.get(transactionLock);
			Integer executedOperations = this.countOperations.get(transactionLock);
			Integer operationsListLenght = operations.size(); 
			return operations.subList(executedOperations, operationsListLenght);
		} else {
			// schedule remaining operations
			return schedule.subList(this.countOperationsSchedule, this.schedule.size());
		}
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