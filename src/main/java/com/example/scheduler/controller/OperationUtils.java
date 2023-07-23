package com.example.scheduler.controller;

import java.util.regex.Pattern;

public class OperationUtils {
	
	static String regexReadWrite = "[rw][0-9]+\\([a-zA-Z]+([0-9]+)?\\)";
	static String regexCommit = "c[0-9]+";
	
	public static boolean isReadOrWrite(String operation) {
		return Pattern.matches(regexReadWrite, operation);
	}
	public static boolean isCommit(String operation) {
		return Pattern.matches(regexCommit, operation);
	}
	public static String getTransactionNumber(String operation, boolean isReadOrWrite, boolean isCommit) {
		int numberEnd = 0;
		if(isReadOrWrite) {
			numberEnd = operation.indexOf('(');
		}
		if(isCommit) {
			numberEnd = operation.length();
		}
		return operation.substring(1, numberEnd);	
	}
}