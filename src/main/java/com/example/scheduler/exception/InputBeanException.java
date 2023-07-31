package com.example.scheduler.exception;

public class InputBeanException extends Exception{

	private static final long serialVersionUID = -7985788363387627139L;

	public InputBeanException(String errorMessage) {
		super(errorMessage);
	}
}