package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.scheduler.exception.DeadlockException;

public class WaitForGraph {
	// T1 -> (T2, X)
	HashMap<String, Entry<String, String>> adjacencyList;
	
	public WaitForGraph() {
		this.adjacencyList = new HashMap<String, Entry<String, String>>();
	}
	
	public void addEdge(String sourceTransaction, String destinationTransaction, String conflictingObject) 
			throws DeadlockException {
		this.checkCycles(sourceTransaction, destinationTransaction);
		
		// create a new entry representing the edge
		Entry<String, String> newEdge = Map.entry(destinationTransaction, conflictingObject);
		// add the edge to the adjacency list
		this.adjacencyList.put(sourceTransaction, newEdge);
	}

	// T2->T1, T3->T2, T1->T3
	private void checkCycles(String sourceTransaction, String destinationTransaction) 
			throws DeadlockException {
		String waitForTransaction = destinationTransaction;
		List<String> waitForCycle = new ArrayList<String>();
		waitForCycle.add(sourceTransaction);
		waitForCycle.add(destinationTransaction);
		
		while(waitForTransaction != null) {
			if(this.adjacencyList.containsKey(waitForTransaction)) {
				waitForTransaction = this.adjacencyList.get(waitForTransaction).getKey();
				waitForCycle.add(waitForTransaction);
				if(waitForTransaction.equals(sourceTransaction)) { 
					throw new DeadlockException(
							String.format("Deadlock detected, the Wait-For-Graph contains the following cycle %s", 
							this.formatCycle(waitForCycle)));
				}
			} else {
				waitForTransaction = null;
			}
		}
	}

	private Object formatCycle(List<String> waitForCycle) {
		return "T" + String.join(" T", waitForCycle);
	}

	public HashMap<String, Entry<String, String>> getAdjacencyList() {
		return this.adjacencyList;
	}

	public void removeEdge(String blockedTransaction) {
		if(this.adjacencyList.containsKey(blockedTransaction)) {
			String waitForTransaction = this.adjacencyList.get(blockedTransaction).getKey();
			String waitForObject = this.adjacencyList.get(blockedTransaction).getValue();
			this.adjacencyList.remove(blockedTransaction);
			// search for waitForEdges (T2, X) in other blocked transactions and update T2 with blockedTransaction
			for(String transactionWaitFor: this.adjacencyList.keySet()) {
				if(this.adjacencyList.get(transactionWaitFor).getValue().equals(waitForObject) &&
				   this.adjacencyList.get(transactionWaitFor).getKey().equals(waitForTransaction)) {
					this.adjacencyList.put(transactionWaitFor, Map.entry(blockedTransaction, waitForObject));
				}
			}
		}
	}
}