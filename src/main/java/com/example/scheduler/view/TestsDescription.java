package com.example.scheduler.view;

import java.util.ArrayList;
import java.util.List;

public class TestsDescription {
	
	public static List<EntryDescription> getTests() {
		List<EntryDescription> tests = new ArrayList<EntryDescription>();
		tests.add(new EntryDescription("r1(x) w1(x)", false, false, true, "checkReadWriteNoAnticipationExclusive"));
		tests.add(new EntryDescription("r1(x) r2(x)", false, false, true, "checkRead1Read2NoAnticipationExclusive"));
		tests.add(new EntryDescription("r1(x) r2(x) w1(x)", false, false, false, "checkBlock2NoAnticipationExclusive"));
		tests.add(new EntryDescription("w1(x) w2(y) w2(x) w1(y)", false, false, false, "checkDeadlockNoAnticipationExclusive"));
		
		tests.add(new EntryDescription("r1(x) w1(x)", false, true, true, "checkLockUpgradeNoAnticipationShared"));
		tests.add(new EntryDescription("r1(x) r2(x)", false, true, true, "checkRead1Read2NoAnticipationShared"));
		tests.add(new EntryDescription("r1(x) w2(x) w1(x)", false, true, false, "checkBlock2NoAnticipationShared"));
		tests.add(new EntryDescription("w1(x) w2(y) w2(x) w1(y)", false, true, false, "checkDeadlockNoAnticipationShared"));

		tests.add(new EntryDescription("w1(x) w2(x) w1(y) w1(z)", true, false, true, "checkMultipleAnticipateLockAnticipationExclusive"));
		tests.add(new EntryDescription("w1(x) w2(x) w3(y) w1(y)", true, false, false, "checkUnableToAnticipateLockAnticipationExclusive"));
		tests.add(new EntryDescription("w1(x) w2(y) w2(x) w1(y)", true, false, false, "checkDeadlockAnticipationExclusive"));

		tests.add(new EntryDescription("r2(x) r1(x) w2(x) w1(y)", true, true, true, "checkAnticipateLockWithUpgradeAnticipationShared"));
		tests.add(new EntryDescription("r1(x) w2(y) w2(x) r1(y) w1(x) c1 c2", true, true, false, "checkBlockAndLockAnticipationDeadlockAnticipationShared"));
		tests.add(new EntryDescription("w1(Z) r2(X) w3(X) r3(Y) w4(Y) w4(X) r2(Y) r1(Y) w2(Z)", true, true, false, "checkUnableToAnticipateLockExamAnticipationShared"));		

		return tests;
}
	
}