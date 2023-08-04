package com.example.scheduler.exception;

public class DeadlockException extends Exception{

	private static final long serialVersionUID = 403243191764942115L;

	public DeadlockException(String errorMessage) {
		super(errorMessage);
	}
	
}