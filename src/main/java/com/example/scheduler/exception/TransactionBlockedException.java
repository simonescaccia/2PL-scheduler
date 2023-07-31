package com.example.scheduler.exception;

public class TransactionBlockedException extends Exception{

	private static final long serialVersionUID = 5539537614218629439L;
	
	public TransactionBlockedException() {
		super();
	}
	
	public TransactionBlockedException(String errorMessage) {
		super(errorMessage);
	}
	
}