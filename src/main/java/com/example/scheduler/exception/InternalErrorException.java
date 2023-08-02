package com.example.scheduler.exception;

public class InternalErrorException extends Exception {

	private static final long serialVersionUID = 4254207302358514569L;
	
	public InternalErrorException(String errorMessage) {
		super(errorMessage);
	}
}
