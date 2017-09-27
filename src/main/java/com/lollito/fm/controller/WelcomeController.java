package com.lollito.fm.controller;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeController {

	@RequestMapping("/")
	public String welcome(Map<String, Object> model) {
		return "index";
	}

	@RequestMapping("/home")
	public String home(Map<String, Object> model) {
		return "home";
	}
}