package com.example.scheduler.view;

public class EntryDescription {
	String schedule;
	boolean isLockAnticipation;
	boolean isLockShared;
	boolean is2PL;
	String description;
	
	public EntryDescription(
			String schedule,
			boolean isLockAnticipation,
			boolean isLockShared,
			boolean is2PL,
			String description
			) {
		this.schedule = schedule;
		this.isLockAnticipation = isLockAnticipation;
		this.isLockShared = isLockShared;
		this.is2PL = is2PL;
		this.description = description;
	}
	
	public String getSchedule() {
		return this.schedule;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public boolean getIsLockAnticipation() {
		return this.isLockAnticipation;
	}
	
	public boolean getIsLockShared() {
		return this.isLockShared;
	}
	
	public boolean is2PL() {
		return this.is2PL;
	}
}