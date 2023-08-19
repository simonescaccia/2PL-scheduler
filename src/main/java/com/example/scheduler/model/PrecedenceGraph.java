package com.example.scheduler.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.scheduler.exception.InternalErrorException;

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
	
	public void addNode(String transaction) {
		if(!this.adjacencyList.containsKey(transaction)) {
			this.adjacencyList.put(transaction, new ArrayList<String>());
		}
	}

	public List<String> getTopologicalOrder() throws InternalErrorException {
		List<String> topologicalOrder = new ArrayList<String>();
		HashMap<String, List<String>> adjacencyListCopy = new HashMap<String, List<String>>(this.adjacencyList);
		while(adjacencyListCopy.keySet().size()>0) {
			List<String> remainingTransactionsCopy = new ArrayList<String>(adjacencyListCopy.keySet());
			List<String> notFreeTransactions = new ArrayList<String>();
			for(String transaction: adjacencyListCopy.keySet()) {
				for(String destinationTransaciton: adjacencyListCopy.get(transaction)) {
					if(!notFreeTransactions.contains(destinationTransaciton)) {
						notFreeTransactions.add(destinationTransaciton);
					}
				}
			}
			remainingTransactionsCopy.removeAll(notFreeTransactions);
			if(remainingTransactionsCopy.size()==0) {
				throw new InternalErrorException("During getTopologicalOrder, there is no topological order");
			}
			topologicalOrder.add(String.format("T%s", remainingTransactionsCopy.get(0)));
			adjacencyListCopy.remove(remainingTransactionsCopy.get(0));
		}
		
		return topologicalOrder;
	}
}