package com.example.scheduler.exception;

/**
 * The transaction can't be unlocked, then call blockTransaction to block it if not already blocked
 *
 */
public class BlockTransactionException extends Exception{
	
	private static final long serialVersionUID = 6100616968481336593L;

	public BlockTransactionException() {
		super();
	}
	
	public BlockTransactionException(String errorMessage) {
		super(errorMessage);
	}
	
}