package com.lollito.fm.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class NavigationController {

	@GetMapping(path = "/")
	public String welcome() {
		return "home";
	}
	
	@GetMapping(path = "/login")
	public String login() {
		return "login";
	}
	
	@GetMapping(path = "/home")
	public String index() {
		return "home";
	}
	
	@GetMapping(path = "/team")
	public String team() {
		return "team";
	}
	
	@GetMapping(path = "/schedule")
	public String schedule() {
		return "schedule";
	}
	
	@GetMapping(path = "/ranking")
	public String ranking() {
		return "ranking";
	}
	
	@GetMapping(path = "/formation")
	public String formation() {
		return "formation";
	}
	
	@GetMapping(path = "/admin")
	public String admin() {
		return "admin/dashboard";
	}
}