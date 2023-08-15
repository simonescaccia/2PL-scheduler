package com.example.scheduler.controller;

import java.util.regex.Pattern;

public class OperationUtils {
	static String regexRead = "r[0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	static String regexWrite = "w[0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	static String regexCommit = "c[0-9]+";
	static String regexGenericLock = "l[0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	static String regexUnlock = "u[0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	static String regexExclusiveLock = "xl[0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	static String regexSharedLock = "sl[0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	
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
	
	public static boolean isGenericLock(String operation) {
		return Pattern.matches(regexGenericLock, operation);
	}
	
	public static boolean isUnlock(String operation) {
		return Pattern.matches(regexUnlock, operation);
	}
	
	public static boolean isExclusiveLock(String operation) {
		return Pattern.matches(regexExclusiveLock, operation);
	}
	
	public static boolean isSharedLock(String operation) {
		return Pattern.matches(regexSharedLock, operation);
	}
	
	public static boolean isLock(String operation) {
		return OperationUtils.isExclusiveLock(operation) || 
			   OperationUtils.isSharedLock(operation) || 
			   OperationUtils.isGenericLock(operation);
	}
	
	public static String getTransactionNumber(String operation) {
		int indexNumberStart = 0;
		int indexNumberEnd = 0;
		if(OperationUtils.isReadOrWrite(operation) || OperationUtils.isGenericLock(operation) || OperationUtils.isUnlock(operation)) {
			indexNumberStart = 1;
			indexNumberEnd = operation.indexOf('(');
		} else if(OperationUtils.isCommit(operation)) {
			indexNumberStart = 1;
			indexNumberEnd = operation.length();
		} else if(OperationUtils.isExclusiveLock(operation) || OperationUtils.isSharedLock(operation)) {
			indexNumberStart = 2;
			indexNumberEnd = operation.indexOf('(');
		}
		return operation.substring(indexNumberStart, indexNumberEnd);	
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