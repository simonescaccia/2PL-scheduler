package com.example.scheduler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.scheduler.controller.Scheduler2PL;
import com.example.scheduler.exception.InputBeanException;
import com.example.scheduler.view.InputBean;

@Controller
public class ApplicationController {
	
	Logger logger = Logger.getLogger(ApplicationController.class.getName());
	
	@GetMapping("/")
	public String index(
			@RequestParam(name="check", required=false, defaultValue="") String check_schedule,
			@RequestParam(name="schedule", required=false, defaultValue="") String schedule,
			@RequestParam(name="lock_anticipation", required=false, defaultValue="") String lockAnticipation,
			@RequestParam(name="lock_type", required=false, defaultValue="") String lockType,
			Model model
	){
		logger.log(Level.INFO, "----------------------");
		logger.log(Level.INFO, "schedule: " + schedule);
		logger.log(Level.INFO, "lock_anticipation: " + lockAnticipation);
		logger.log(Level.INFO, "lock_type: " + lockType);
		
		if (!check_schedule.equals("True") || schedule.equals("")) {
			model.addAttribute("result", "False");
		} else {
			// Show the result tab
			model.addAttribute("result", "True");
			model.addAttribute("error", "");
			
			try {
				// Check the input
				InputBean iB = new InputBean(schedule, lockAnticipation, lockType);
				model.addAttribute("transactions", iB.getTransactions());
				
				Scheduler2PL s2PL = new Scheduler2PL(iB);
				s2PL.check();			
				
			} catch (InputBeanException lTE) {
				model.addAttribute("error", lTE.getMessage());
			}
			
			model.addAttribute("schedule", schedule);
		}
		
		return "index";
	}

}