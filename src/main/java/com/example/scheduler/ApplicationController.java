package com.example.scheduler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplicationController {
	
	Logger logger = Logger.getLogger(ApplicationController.class.getName());
	
	@GetMapping("/")
	public String index(
			@RequestParam(name="schedule", required=false, defaultValue="") String schedule,
			@RequestParam(name="lock_anticipation", required=false, defaultValue="") String lock_anticipation,
			@RequestParam(name="lock_type", required=false, defaultValue="") String lock_type,
			Model model
	){
		logger.log(Level.INFO, "schedule: " + schedule);
		logger.log(Level.INFO, "lock_anticipation: " + lock_anticipation);
		logger.log(Level.INFO, "lock_type: " + lock_type);
		
		model.addAttribute("schedule", schedule);
		model.addAttribute("lock_anticipation", lock_anticipation);
		model.addAttribute("lock_type", lock_type);
		
		return "index";
	}

}