package com.lollito.fm.controller.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.News;
import com.lollito.fm.service.NewsService;

@RestController
@RequestMapping(value="/api/news")
public class NewsController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private NewsService newsService;
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return newsService.getCount();
    }
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<News> findAll(Model model) {
        return newsService.findAll();
    }
   
}
