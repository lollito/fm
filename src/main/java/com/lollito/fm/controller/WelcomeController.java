package com.lollito.fm.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeController {

	@RequestMapping("/")
	public String welcome() {
		return "index";
	}

	@RequestMapping("/home")
	public String home() {
		return "home";
	}
	
	@RequestMapping("/team")
	public String team() {
		return "team";
	}
	
	@RequestMapping("/schedule")
	public String schedule() {
		return "schedule";
	}
	
	@RequestMapping("/ranking")
	public String ranking() {
		return "ranking";
	}
	
	@RequestMapping("/formation")
	public String formation() {
		return "formation";
	}
	
	@RequestMapping("/login")
	public String login() {
		return "login";
	}
	
	@RequestMapping("/admin")
	public String admin() {
		return "admin/dashboard";
	}
	
}