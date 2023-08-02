package com.example.scheduler.controller;

import java.util.regex.Pattern;

public class OperationUtils {
	static String regexRead = "[r][0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	static String regexWrite = "[w][0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	static String regexCommit = "c[0-9]+";
	
	public static boolean isRead(String operation) {
		return Pattern.matches(regexRead, operation);
	}
	
	public static boolean isWrite(String operation) {
		return Pattern.matches(regexWrite, operation);
	}
	
	public static boolean isReadOrWrite(String operation) {
		return OperationUtils.isRead(operation) || OperationUtils.isWrite(operation);
	}
	
	public static boolean isCommit(String operation) {
		return Pattern.matches(regexCommit, operation);
	}
	
	public static String getTransactionNumber(String operation) {
		int numberEnd = 0;
		if(OperationUtils.isReadOrWrite(operation)) {
			numberEnd = operation.indexOf('(');
		}
		if(OperationUtils.isCommit(operation)) {
			numberEnd = operation.length();
		}
		return operation.substring(1, numberEnd);	
	}
	
	public static String getObjectName(String operation) {
		return operation.substring(operation.indexOf('(') + 1, operation.indexOf(')'));
	}
	
	public static String createOperation(String operationType, String transactionNumber, String objectName) {
		return String.format("%s%s(%s)", operationType, transactionNumber, objectName);
	}
	
	public static Boolean use(String operation, String object) {
		return OperationUtils.getObjectName(operation).equals(object);
	}
}