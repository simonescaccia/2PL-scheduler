package com.example.scheduler.controller;

import static org.junit.Assert.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.scheduler.exception.InputBeanException;
import com.example.scheduler.exception.InternalErrorException;
import com.example.scheduler.view.InputBean;
import com.example.scheduler.view.OutputBean;

@SpringBootTest
public class Scheduler2PLTest {
	String noLockAnticipation = "";
	String lockAnticipation = "True";
	String exclusiveLockType = "xl";
	String sharedLockType = "sl";
		
	@Test
	public void checkReadNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x)";
		String outputSchedule = "l1(x) r1(x) u1(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkReadWriteNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkCommitNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "c1 c2";
		String outputSchedule = "c1 c2";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkReadCommitNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) c1";
		String outputSchedule = "l1(x) r1(x) c1 u1(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkRead1Read2NoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x)";
		String outputSchedule = "l1(x) r1(x) u1(x) l2(x) r2(x) u2(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2NoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x) l2(x) r2(x) u2(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
}