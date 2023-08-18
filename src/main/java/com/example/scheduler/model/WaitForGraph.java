package com.example.scheduler.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.example.scheduler.exception.DeadlockException;

public class WaitForGraph {
	// T1 -> (T2, X)
	HashMap<String, Entry<String, String>> adjacencyList;
	
	public WaitForGraph() {
		adjacencyList = new HashMap<String, Entry<String, String>>();
	}
	
	public void addEdge(String sourceTransaction, String destinationTransaction, String conflictingObject) throws DeadlockException {
		// create a new entry representing the edge
		Entry<String, String> newEdge = Map.entry(destinationTransaction, conflictingObject);
		// add the edge to the adjacency list
		this.adjacencyList.put(sourceTransaction, newEdge);
		
		if(this.adjacencyList.containsKey(destinationTransaction)) {
			throw new DeadlockException(String.format("Deadlock detected, caused by transaction %s on transaction %s", 
					sourceTransaction, destinationTransaction));
		}
	}
	
	public HashMap<String, Entry<String, String>> getAdjacencyList() {
		return this.adjacencyList;
	}

	public void removeEdge(String blockedTransaction) {
		String waitForTransaction = this.adjacencyList.get(blockedTransaction).getKey();
		String waitForObject = this.adjacencyList.get(blockedTransaction).getValue();
		// search for waitForEdges (T2, X) in other blocked transactions and update T2 with blockedTransaction
		this.adjacencyList.remove(blockedTransaction);
	}
}