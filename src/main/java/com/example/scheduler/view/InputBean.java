package com.example.scheduler.view;

import com.example.scheduler.exception.InputBeanException;

public class InputBean {
	String 	schedule;				// Input schedule
	Boolean isLockAnticipation;	// Enable lock anticipation
	Boolean isLockShared;			// Use both exclusive and shared locks
	
	public InputBean(String schedule, String lockAnticipation, String lockType) throws InputBeanException {
		this.schedule = schedule;
		setIsLockAnticipation(lockAnticipation);
		setIsLockShared(lockType);
	}

	private void setIsLockAnticipation(String lockAnticipation) throws InputBeanException {
		if(lockAnticipation.equals("True")) {
			this.isLockAnticipation = true;
		} else if (lockAnticipation.equals("")) {
			this.isLockAnticipation = false;
		} else {
			throw new InputBeanException("Invalid Lock Anticipation");
		}
	}
	
	private void setIsLockShared(String lockType) throws InputBeanException {
		if(lockType.equals("xl")) {
			this.isLockShared = false;
		} else if(lockType.equals("sl")) {
			this.isLockShared = true;
		} else {
			throw new InputBeanException("Invalid Lock Type");
		}
	}
	
}