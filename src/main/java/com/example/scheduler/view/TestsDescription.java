package com.example.scheduler.view;

import java.util.ArrayList;
import java.util.List;

public class TestsDescription {
	
	public static List<EntryDescription> getTests() {
		List<EntryDescription> tests = new ArrayList<EntryDescription>();
		tests.add(new EntryDescription("r1(x) w1(x) c1", false, false, true, "Read, write, commit"));
		tests.add(new EntryDescription("r1(x) w1(x) c1", false, true, true, "Lock upgrade"));
		tests.add(new EntryDescription("r1(x) r2(x) w1(x)", false, true, true, "Lock upgrade with unlocks"));
		tests.add(new EntryDescription("r1(x) w2(x) w1(y)", false, true, false, "Block and resume T2"));
		tests.add(new EntryDescription("r1(x) w2(x) w1(y)", true, true, true, "Lock anticipation "));
		tests.add(new EntryDescription("r1(x) w2(x) w1(x)", true, true, false, "Unable to anticipate e.g.1"));
		tests.add(new EntryDescription("r1(x) w2(x) w3(y) w1(y)", true, true, false, "Unable to anticipate e.g.2"));
		tests.add(new EntryDescription("r1(x) r2(y) w2(x) w1(y)", true, true, false, "Deadlock"));
		tests.add(new EntryDescription("w1(Z) r2(X) w3(X) r3(Y) w4(Y) w4(X) r2(Y) r1(Y) w2(Z)", true, true, false, "Exam text"));		
		tests.add(new EntryDescription("w1(Z) r2(X) w3(X) r3(Y) w4(X) r2(Y) r1(Y) w2(Z)", true, true, true, "Exam text (removing w4(Y))"));		

		return tests;
}
	
}