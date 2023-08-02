package com.example.scheduler.view;

import java.util.List;

public class OutputBean {
	String scheduleWithLocks;		// Output schedule
	List<String> log;					// Description of the computation
	Boolean result;						// Define if the schedule follows the 2PL protocol
	
	public OutputBean(List<String> scheduleWithLocks, List<String> log, Boolean result) {
		this.formatScheduleWithLocks(scheduleWithLocks);
		this.log = log;
		this.result = result;
	}
	
	public String getSchedleWithLocks() {
		return this.scheduleWithLocks;
	}
	
	public List<String> getLog() {
		return this.log;
	}
	
	public Boolean getResult() {
		return this.result;
	}
	
	private void formatScheduleWithLocks(List<String> scheduleWithLocks) {
		this.scheduleWithLocks = scheduleWithLocks.get(0);
		for(String operation: scheduleWithLocks.subList(1, scheduleWithLocks.size())) {
			this.scheduleWithLocks = this.scheduleWithLocks.concat(" ".concat(operation));
		}
	}
}