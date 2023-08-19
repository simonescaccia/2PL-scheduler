package com.example.scheduler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.scheduler.controller.Scheduler2PL;
import com.example.scheduler.exception.InputBeanException;
import com.example.scheduler.exception.InternalErrorException;
import com.example.scheduler.view.InputBean;
import com.example.scheduler.view.OutputBean;

@Controller
public class ApplicationController {
	
	Logger logger = Logger.getLogger(ApplicationController.class.getName());
	
	@GetMapping("/")
	public String index(
			@RequestParam(name="check", required=false, defaultValue="") String check_schedule,
			@RequestParam(name="schedule", required=false, defaultValue="") String schedule,
			@RequestParam(name="lockAnticipation", required=false, defaultValue="") String lockAnticipation,
			@RequestParam(name="lockType", required=false, defaultValue="") String lockType,
			Model model
	){
		logger.log(Level.INFO, "----------------------");
		logger.log(Level.INFO, "schedule: " + schedule);
		logger.log(Level.INFO, "lockAnticipation: " + lockAnticipation);
		logger.log(Level.INFO, "lockType: " + lockType);
		if(lockType.equals("")) {
			// first request, default setting
			lockAnticipation = "True";
		}
		model.addAttribute("lockAnticipation", lockAnticipation);
		model.addAttribute("lockType", lockType);
		
		if (!check_schedule.equals("True") || schedule.equals("")) {
			model.addAttribute("result", "False");
		} else {
			// Show the result tab
			model.addAttribute("result", "True");
			model.addAttribute("error", "");
			
			try {
				// Check the input
				InputBean iB = new InputBean(schedule, lockAnticipation, lockType);
				
				Scheduler2PL s2PL = new Scheduler2PL(iB);
				OutputBean oB = s2PL.check();
				
				model.addAttribute("transactions", oB.getTransactions());
				model.addAttribute("scheduleWithLocks", oB.getSchedleWithLocks());
				model.addAttribute("log", oB.getLog());
				model.addAttribute("result2PL", oB.getResult());
				model.addAttribute("transactionsWithLocks", oB.getTransactionsWithLocks());
				model.addAttribute("dataActionProjection", oB.getDataActionProjection());
				
			} catch (InputBeanException | InternalErrorException e) {
				model.addAttribute("error", e.getMessage());
			}
			
			model.addAttribute("schedule", schedule);
		}
		
		return "index";
	}

}