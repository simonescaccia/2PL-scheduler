package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class WaitForGraph {
	// T1 -> [(T2, X), (T3, Y)]
	HashMap<String, List<Entry<String, String>>> adjacencyList;
	
	public WaitForGraph() {
		adjacencyList = new HashMap<String, List<Entry<String, String>>>();
	}
	
	public void addEdge(String sourceTransaction, String destinationTransaction, String conflictingObject) {
		this.addVertex(sourceTransaction);
		this.addVertex(destinationTransaction);
	}
	
	private void addVertex(String transactionNumber) {
		if(!this.adjacencyList.containsKey(transactionNumber)) {
			List<Entry<String, String>> adjacentTransactions = new ArrayList<Entry<String, String>>();
			this.adjacencyList.put(transactionNumber, adjacentTransactions);
		}
	}
}