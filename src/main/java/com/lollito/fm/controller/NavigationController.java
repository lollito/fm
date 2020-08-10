package com.lollito.fm.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
	
	@GetMapping(path = "/register")
	public String register() {
		return "register";
	}
	
	@GetMapping(path = "/home")
	public String index() {
		return "home";
	}
	
	@GetMapping(path = "/team")
	public String team() {
		return "team";
	}
	
	@GetMapping(value = {"/match/", "/match/{id}"})
	public String match(@PathVariable (required = false, value = "id") String id) {
		return "match";
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
	
	@GetMapping(path = "/leagues")
	public String leagues() {
		return "leagues";
	}
	
	@GetMapping(path = "/admin")
	public String admin() {
		return "admin/dashboard";
	}
}