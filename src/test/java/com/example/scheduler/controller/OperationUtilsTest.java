package com.example.scheduler.controller;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OperationUtilsTest {
	
	@Test
	public void getReadTransactionNumberTest() {
		String readOperation = "r1(x)";
		String transaction = "1";
		assertTrue(OperationUtils.getTransactionNumber(readOperation).equals(transaction));
	}
	
	@Test
	public void getWriteTransactionNumberTest() {
		String writeOperation = "w1(x)";
		String transaction = "1";
		assertTrue(OperationUtils.getTransactionNumber(writeOperation).equals(transaction));
	}
}