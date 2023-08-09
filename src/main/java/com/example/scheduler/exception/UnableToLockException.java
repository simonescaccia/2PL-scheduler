package com.example.scheduler.exception;

import com.example.scheduler.model.RequiredLocksToUnlockObject;

/**
 * The transaction can't be unlocked, then call blockTransaction to block it if not already blocked
 *
 */
public class UnableToLockException extends Exception{
	
	private static final long serialVersionUID = 6100616968481336593L;
	
	RequiredLocksToUnlockObject requiredLocksToUnlock;

	public UnableToLockException(RequiredLocksToUnlockObject requiredLocksToUnlock) {
		super();
		this.requiredLocksToUnlock = requiredLocksToUnlock;
	}
	
	public RequiredLocksToUnlockObject getRequiredLocksToUnlock() {
		return this.requiredLocksToUnlock;
	}
	
}