package com.example.scheduler;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplicationController {

	@GetMapping("/2pl")
	public String index(
			@RequestParam(name="schedule", required=false, defaultValue="") String schedule,
			@RequestParam(name="lock_anticipation", required=false, defaultValue="") String lock_anticipation,
			@RequestParam(name="lock_type", required=false, defaultValue="") String lock_type,
			Model model
	){
		model.addAttribute("schedule", schedule);
		model.addAttribute("lock_anticipation", lock_anticipation);
		model.addAttribute("lock_type", lock_type);
		System.out.println("schedule: " + schedule);
		System.out.println("lock_anticipation: " + lock_anticipation);
		System.out.println("lock_type: " + lock_type);
		
		return "index";
	}

}