package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrecedenceGraph {
	// T1 -> T2, T3
	HashMap<String, List<String>> adjacencyList;
	
	public PrecedenceGraph() {
		this.adjacencyList = new HashMap<String, List<String>>();
	}
	
	public void addEdge(String sourceTransaction, String destinationTransaction) {
		if(!this.adjacencyList.containsKey(sourceTransaction)) {
			List<String> destinationTransactions = new ArrayList<String>();
			destinationTransactions.add(destinationTransaction);
			this.adjacencyList.put(sourceTransaction, destinationTransactions);
		} else if(!this.adjacencyList.get(sourceTransaction).contains(destinationTransaction)) {
			this.adjacencyList.get(sourceTransaction).add(destinationTransaction);
		}
	}
}