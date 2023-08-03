package com.example.scheduler.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WaitForGraph {
	// T1 -> (T2, X)
	HashMap<String, Entry<String, String>> adjacencyList;
	
	public WaitForGraph() {
		adjacencyList = new HashMap<String, Entry<String, String>>();
	}
	
	public void addEdge(String sourceTransaction, String destinationTransaction, String conflictingObject) {
		// create a new entry representing the edge
		Entry<String, String> newEdge = Map.entry(destinationTransaction, conflictingObject);
		// add the edge to the adjacency list
		this.adjacencyList.put(sourceTransaction, newEdge);
	}
	
	public HashMap<String, Entry<String, String>> getAdjacencyList() {
		return this.adjacencyList;
	}
}