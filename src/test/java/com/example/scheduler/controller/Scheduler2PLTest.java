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
	
	@Test
	public void checkBlock2MoreTimesNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r1(y) r2(x) r2(y) w1(x) w1(y)";
		String outputSchedule = "l1(x) r1(x) l1(y) r1(y) w1(x) u1(x) l2(x) r2(x) w1(y) u1(y) l2(y) r2(y) u2(x) u2(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2DelayResumeNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "l1(x) r1(x) l1(y) r1(y) w1(x) u1(x) l2(x) r2(x) c1 c2 u2(x) u1(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlockMultipleTransactionsNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) r3(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x) l2(x) r2(x) u2(x) l3(x) r3(x) u3(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkDeadlockNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(y) w2(x) w1(y)";
		String outputSchedule = "l1(x) w1(x) l2(y) w2(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule) && !oB.getResult());
	}
	
	@Test
	public void checkReadNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x)";
		String outputSchedule = "sl1(x) r1(x) u1(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkWriteNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x)";
		String outputSchedule = "xl1(x) w1(x) u1(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkLockUpgradeNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) xl1(x) w1(x) u1(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkRead1Read2NoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x)";
		String outputSchedule = "sl1(x) r1(x) sl2(x) r2(x) u1(x) u2(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkLockUpgradeMoreSharedLocksNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) sl2(x) r2(x) u2(x) xl1(x) w1(x) u1(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2NoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) xl1(x) w1(x) u1(x) xl2(x) w2(x) u2(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2MoreTimesNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(y) w2(x) w1(x) w2(y) r1(y)";
		String outputSchedule = "sl1(x) r1(x) xl1(y) w1(y) xl1(x) w1(x) u1(x) xl2(x) w2(x) r1(y) u1(y) xl2(y) w2(y) u2(x) u2(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2DelayResumeNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r1(y) w2(x) w1(x) w2(y) w1(y)";
		String outputSchedule = "sl1(x) r1(x) sl1(y) r1(y) xl1(x) w1(x) xl1(y) w1(y) u1(x) xl2(x) w2(x) u1(y) xl2(y) w2(y) u2(x) u2(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}	
	
	@Test
	public void checkDeadlockNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(y) w2(x) w1(y)";
		String outputSchedule = "xl1(x) w1(x) xl2(y) w2(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule) && !oB.getResult());
	}
	
	@Test
	public void checkReadAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x)";
		String outputSchedule = "l1(x) r1(x) u1(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkReadWriteAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkCommitAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "c1 c2";
		String outputSchedule = "c1 c2";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkReadCommitAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) c1";
		String outputSchedule = "l1(x) r1(x) c1 u1(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkRead1Read2AnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x)";
		String outputSchedule = "l1(x) r1(x) u1(x) l2(x) r2(x) u2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2AnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x) l2(x) r2(x) u2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2MoreTimesAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r1(y) r2(x) r2(y) w1(x) w1(y)";
		String outputSchedule = "l1(x) r1(x) l1(y) r1(y) w1(x) u1(x) l2(x) r2(x) w1(y) u1(y) l2(y) r2(y) u2(x) u2(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2DelayResumeAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "l1(x) r1(x) l1(y) r1(y) w1(x) u1(x) l2(x) r2(x) c1 c2 u2(x) u1(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkAnticipateLockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(x) w1(y)";
		String outputSchedule = "l1(x) w1(x) l1(y) u1(x) l2(x) w2(x) w1(y) u2(x) u1(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkMultipleAnticipateLockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(x) w1(y) w1(z)";
		String outputSchedule = "l1(x) w1(x) l1(y) l1(z) u1(x) l2(x) w2(x) w1(y) w1(z) u2(x) u1(y) u1(z)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkUnableToAnticipateLockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(x) w3(y) w1(y)";
		String outputSchedule = "l1(x) w1(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule) && !oB.getResult());
	}
	
	@Test
	public void checkDeadlockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(y) w2(x) w1(y)";
		String outputSchedule = "l1(x) w1(x) l2(y) w2(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule) && !oB.getResult());
	}
	
	@Test
	public void checkBlockAndLockAnticipationDeadlockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(y) r2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "l1(x) r1(x) l2(y) w2(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule) && !oB.getResult());
	}
	
	@Test
	public void checkReadAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x)";
		String outputSchedule = "sl1(x) r1(x) u1(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkLockUpgradeAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) xl1(x) w1(x) u1(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkCommitAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "c1 c2";
		String outputSchedule = "c1 c2";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkReadCommitAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) c1";
		String outputSchedule = "sl1(x) r1(x) c1 u1(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkRead1Read2AnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x)";
		String outputSchedule = "sl1(x) r1(x) sl2(x) r2(x) u1(x) u2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkLockUpgradeMoreSharedLocksAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) sl2(x) r2(x) u2(x) xl1(x) w1(x) u1(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2AnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) xl1(x) w1(x) u1(x) xl2(x) w2(x) u2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2MoreTimesAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(y) w2(x) w1(x) w2(y) r1(y)";
		String outputSchedule = "sl1(x) r1(x) xl1(y) w1(y) xl1(x) w1(x) u1(x) xl2(x) w2(x) r1(y) u1(y) xl2(y) w2(y) u2(x) u2(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkAnticipateLockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) w1(y)";
		String outputSchedule = "sl1(x) r1(x) xl1(y) u1(x) xl2(x) w2(x) w1(y) u2(x) u1(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkAnticipateLockWithUpgradeAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r2(x) r1(x) w2(x) w1(y)";
		String outputSchedule = "sl2(x) r2(x) sl1(x) r1(x) xl1(y) u1(x) xl2(x) w2(x) w1(y) u2(x) u1(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkMultipleAnticipateLockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) r1(y) w1(z)";
		String outputSchedule = "sl1(x) r1(x) sl1(y) xl1(z) u1(x) xl2(x) w2(x) r1(y) w1(z) u2(x) u1(y) u1(z)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkBlock2DelayResumeAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "sl1(x) r1(x) sl1(y) r1(y) xl1(x) w1(x) u1(x) xl2(x) w2(x) c1 c2 u2(x) u1(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule));
	}
	
	@Test
	public void checkDeadlockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(y) w2(x) w1(y)";
		String outputSchedule = "xl1(x) w1(x) xl2(y) w2(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule) && !oB.getResult());
	}
	
	@Test
	public void checkBlockAndLockAnticipationDeadlockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(y) w2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "sl1(x) r1(x) xl2(y) w2(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(oB.getSchedleWithLocks().equals(outputSchedule) && !oB.getResult());
	}

}