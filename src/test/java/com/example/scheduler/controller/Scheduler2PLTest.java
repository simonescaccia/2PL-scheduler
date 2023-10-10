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
	String emptySerialSchedule = "";
	String emptyDeadlockCycle = "";
		
	private boolean getAssertion(
			OutputBean oB, 
			Boolean result, 
			String schedule, 
			String outputSchedule, 
			String dataActionProjection, 
			String serialSchedule,
			String deadlockCycle) {
		boolean checkResult = oB.getResult().equals(result);
		boolean checkOutputSchedule = oB.getSchedleWithLocks().equals(outputSchedule);
		boolean checkDataActionProjection = oB.getDataActionProjection().equals(dataActionProjection);
		boolean checkSerialSchedule = oB.getTopologicalOrder().equals(serialSchedule);
		boolean checkDeadlockCycle = oB.getDeadlockCycle().equals(deadlockCycle);
		return checkResult && checkOutputSchedule && checkDataActionProjection && checkSerialSchedule && checkDeadlockCycle;
	}
	
	@Test
	public void checkReadNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x)";
		String outputSchedule = "l1(x) r1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkReadWriteNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkReadCommitNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) c1";
		String outputSchedule = "l1(x) r1(x) u1(x) c1";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkRead1Read2NoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x)";
		String outputSchedule = "l1(x) r1(x) u1(x) l2(x) r2(x) u2(x)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2NoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x) l2(x) r2(x) u2(x)";
		String dataActionProjection = "r1(x) w1(x) r2(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2MoreTimesNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r1(y) r2(x) r2(y) w1(x) w1(y)";
		String outputSchedule = "l1(x) r1(x) l1(y) r1(y) w1(x) u1(x) l2(x) r2(x) w1(y) u1(y) l2(y) r2(y) u2(y) u2(x)";
		String dataActionProjection = "r1(x) r1(y) w1(x) r2(x) w1(y) r2(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2DelayResumeNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "l1(x) r1(x) l1(y) r1(y) u1(y) w1(x) u1(x) l2(x) r2(x) u2(x) c1 c2";
		String dataActionProjection = "r1(x) r1(y) w1(x) r2(x) c1 c2";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlockMultipleTransactionsNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) r3(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x) l2(x) r2(x) u2(x) l3(x) r3(x) u3(x)";
		String dataActionProjection = "r1(x) w1(x) r2(x) r3(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlockMultipleTransactionChainNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(z) w2(x) w3(z) w1(y)";
		String outputSchedule = "l1(x) w1(x) l2(z) w2(z) l1(y) w1(y) u1(y) u1(x) l2(x) w2(x) u2(x) u2(z) l3(z) w3(z) u3(z)";
		String dataActionProjection = "w1(x) w2(z) w1(y) w2(x) w3(z)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkDeadlockMultipleTransactionsNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(z) w2(x) w3(y) w3(z) w1(y)";
		String outputSchedule = "l1(x) w1(x) l2(z) w2(z) l3(y) w3(y)";
		String dataActionProjection = "w1(x) w2(z) w3(y)";
		String deadlcokCycle = "T1 T3 T2 T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, deadlcokCycle));
	}
	
	@Test
	public void checkDeadlockNoAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(y) w2(x) w1(y)";
		String outputSchedule = "l1(x) w1(x) l2(y) w2(y)";
		String dataActionProjection = "w1(x) w2(y)";
		String deadlcokCycle = "T1 T2 T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, deadlcokCycle));
	}
	
	@Test
	public void checkReadNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x)";
		String outputSchedule = "sl1(x) r1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkWriteNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x)";
		String outputSchedule = "xl1(x) w1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkLockUpgradeNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) xl1(x) w1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkRead1Read2NoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x)";
		String outputSchedule = "sl1(x) r1(x) u1(x) sl2(x) r2(x) u2(x)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkLockUpgradeMoreSharedLocksNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) sl2(x) r2(x) u2(x) xl1(x) w1(x) u1(x)";
		String serialSchedule = "T2 T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2NoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) xl1(x) w1(x) u1(x) xl2(x) w2(x) u2(x)";
		String dataActionProjection = "r1(x) w1(x) w2(x)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2MoreTimesNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(y) w2(x) w1(x) w2(y) r1(y)";
		String outputSchedule = "sl1(x) r1(x) xl1(y) w1(y) xl1(x) w1(x) r1(y) u1(y) u1(x) xl2(x) w2(x) xl2(y) w2(y) u2(y) u2(x)";
		String dataActionProjection = "r1(x) w1(y) w1(x) r1(y) w2(x) w2(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2DelayResumeNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r1(y) w2(x) w1(x) w2(y) w1(y)";
		String outputSchedule = "sl1(x) r1(x) sl1(y) r1(y) xl1(x) w1(x) xl1(y) w1(y) u1(y) u1(x) xl2(x) w2(x) xl2(y) w2(y) u2(y) u2(x)";		
		String dataActionProjection = "r1(x) r1(y) w1(x) w1(y) w2(x) w2(y)";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}	
	
	@Test
	public void checkDeadlockNoAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(y) w2(x) w1(y)";
		String outputSchedule = "xl1(x) w1(x) xl2(y) w2(y)";
		String dataActionProjection = "w1(x) w2(y)";
		String deadlcokCycle = "T1 T2 T1";
		InputBean iB = new InputBean(schedule, noLockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, deadlcokCycle));
	}
	
	@Test
	public void checkReadAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x)";
		String outputSchedule = "l1(x) r1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkReadWriteAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}

	@Test
	public void checkReadCommitAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) c1";
		String outputSchedule = "l1(x) r1(x) u1(x) c1";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkRead1Read2AnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x)";
		String outputSchedule = "l1(x) r1(x) u1(x) l2(x) r2(x) u2(x)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2AnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) w1(x)";
		String outputSchedule = "l1(x) r1(x) w1(x) u1(x) l2(x) r2(x) u2(x)";
		String dataActionProjection = "r1(x) w1(x) r2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2MoreTimesAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r1(y) r2(x) r2(y) w1(x) w1(y)";
		String outputSchedule = "l1(x) r1(x) l1(y) r1(y) w1(x) u1(x) l2(x) r2(x) w1(y) u1(y) l2(y) r2(y) u2(y) u2(x)";
		String dataActionProjection = "r1(x) r1(y) w1(x) r2(x) w1(y) r2(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2DelayResumeAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "l1(x) r1(x) l1(y) r1(y) u1(y) w1(x) u1(x) l2(x) r2(x) u2(x) c1 c2";
		String dataActionProjection = "r1(x) r1(y) w1(x) r2(x) c1 c2";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkAnticipateLockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(x) w1(y)";
		String outputSchedule = "l1(x) w1(x) l1(y) u1(x) l2(x) w2(x) u2(x) w1(y) u1(y)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkMultipleAnticipateLockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(x) w1(y) w1(z)";
		String outputSchedule = "l1(x) w1(x) l1(y) l1(z) u1(x) l2(x) w2(x) u2(x) w1(y) u1(y) w1(z) u1(z)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkUnableToAnticipateLockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(x) w3(y) w1(y)";
		String outputSchedule = "l1(x) w1(x) l3(y) w3(y) u3(y) l1(y) w1(y) u1(y) u1(x) l2(x) w2(x) u2(x)";
		String dataActionProjection = "w1(x) w3(y) w1(y) w2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkDeadlockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(y) w2(x) w1(y)";
		String outputSchedule = "l1(x) w1(x) l2(y) w2(y)";
		String dataActionProjection = "w1(x) w2(y)";
		String deadlcokCycle = "T1 T2 T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, deadlcokCycle));
	}
	
	@Test
	public void checkBlockAndLockAnticipationDeadlockAnticipationExclusive() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(y) r2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "l1(x) r1(x) l2(y) w2(y)";
		String dataActionProjection = "r1(x) w2(y)";
		String deadlcokCycle = "T1 T2 T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, exclusiveLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, deadlcokCycle));
	}
	
	@Test
	public void checkReadAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x)";
		String outputSchedule = "sl1(x) r1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkLockUpgradeAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) xl1(x) w1(x) u1(x)";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkReadCommitAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) c1";
		String outputSchedule = "sl1(x) r1(x) u1(x) c1";
		String serialSchedule = "T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkRead1Read2AnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x)";
		String outputSchedule = "sl1(x) r1(x) u1(x) sl2(x) r2(x) u2(x)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkLockUpgradeMoreSharedLocksAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) r2(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) sl2(x) r2(x) u2(x) xl1(x) w1(x) u1(x)";
		String serialSchedule = "T2 T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2AnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) w1(x)";
		String outputSchedule = "sl1(x) r1(x) xl1(x) w1(x) u1(x) xl2(x) w2(x) u2(x)";
		String dataActionProjection = "r1(x) w1(x) w2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2MoreTimesAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w1(y) w2(x) w1(x) w2(y) r1(y)";
		String outputSchedule = "sl1(x) r1(x) xl1(y) w1(y) xl1(x) w1(x) u1(x) xl2(x) w2(x) r1(y) u1(y) xl2(y) w2(y) u2(y) u2(x)";
		String dataActionProjection = "r1(x) w1(y) w1(x) w2(x) r1(y) w2(y)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkAnticipateLockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) w1(y)";
		String outputSchedule = "sl1(x) r1(x) xl1(y) u1(x) xl2(x) w2(x) u2(x) w1(y) u1(y)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkAnticipateLockWithUpgradeAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r2(x) r1(x) w2(x) w1(y)";
		String outputSchedule = "sl2(x) r2(x) sl1(x) r1(x) xl1(y) u1(x) xl2(x) w2(x) u2(x) w1(y) u1(y)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkUnableToAnticipate2LockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(x) r3(y) w1(y)";
		String outputSchedule = "xl1(x) w1(x) sl3(y) r3(y) u3(y) xl1(y) w1(y) u1(y) u1(x) xl2(x) w2(x) u2(x)";
		String dataActionProjection = "w1(x) r3(y) w1(y) w2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkMultipleAnticipateLockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) r1(y) w1(z)";
		String outputSchedule = "sl1(x) r1(x) sl1(y) xl1(z) u1(x) xl2(x) w2(x) u2(x) r1(y) u1(y) w1(z) u1(z)";
		String serialSchedule = "T1 T2";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkBlock2DelayResumeAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "sl1(x) r1(x) sl1(y) r1(y) xl1(x) w1(x) u1(x) u1(y) xl2(x) w2(x) u2(x) c1 c2";
		String dataActionProjection = "r1(x) r1(y) w1(x) w2(x) c1 c2";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkDeadlockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(y) w2(x) w1(y)";
		String outputSchedule = "xl1(x) w1(x) xl2(y) w2(y)";
		String dataActionProjection = "w1(x) w2(y)";
		String deadlcokCycle = "T1 T2 T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, deadlcokCycle));
	}
	
	@Test
	public void checkBlockAndLockAnticipationDeadlockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "r1(x) w2(y) w2(x) r1(y) w1(x) c1 c2";
		String outputSchedule = "sl1(x) r1(x) xl2(y) w2(y)";
		String dataActionProjection = "r1(x) w2(y)";
		String deadlcokCycle = "T1 T2 T1";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, deadlcokCycle));
	}
	
	@Test
	public void checkUnableToAnticipateAndBlockLockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(x) w2(x) w3(y) w1(y)";
		String outputSchedule = "xl1(x) w1(x) xl3(y) w3(y) u3(y) xl1(y) w1(y) u1(y) u1(x) xl2(x) w2(x) u2(x)";
		String dataActionProjection = "w1(x) w3(y) w1(y) w2(x)";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, emptyDeadlockCycle));
	}
	
	@Test
	public void checkUnableToAnticipateLockExamDeadlockAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(Z) r2(X) w3(X) r3(Y) w4(Y) w4(X) r2(Y) r1(Y) w2(Z)";
		String outputSchedule = "xl1(Z) w1(Z) sl2(X) r2(X) xl4(Y) w4(Y)";
		String dataActionProjection = "w1(Z) r2(X) w4(Y)";
		String deadlcokCycle = "T2 T4 T2";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, false, schedule, outputSchedule, dataActionProjection, emptySerialSchedule, deadlcokCycle));
	}
	
	@Test
	public void checkAbleToAnticipateLockExamAnticipationShared() throws InputBeanException, InternalErrorException {
		String schedule = "w1(Z) r2(X) w3(X) r3(Y) w4(X) r2(Y) r1(Y) w2(Z)";
		String outputSchedule = "xl1(Z) w1(Z) sl2(X) r2(X) sl2(Y) sl1(Y) u1(Z) xl2(Z) u2(X) xl3(X) w3(X) sl3(Y) r3(Y) u3(Y) u3(X) xl4(X) w4(X) u4(X) r2(Y) u2(Y) r1(Y) u1(Y) w2(Z) u2(Z)";
		String serialSchedule = "T1 T2 T3 T4";
		InputBean iB = new InputBean(schedule, lockAnticipation, sharedLockType);
		Scheduler2PL s2PL = new Scheduler2PL(iB);
		OutputBean oB = s2PL.check();
		assertTrue(this.getAssertion(oB, true, schedule, outputSchedule, schedule, serialSchedule, emptyDeadlockCycle));
	}

}