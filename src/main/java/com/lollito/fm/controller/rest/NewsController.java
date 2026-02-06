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
import com.lollito.fm.model.dto.NewsDTO;
import com.lollito.fm.service.NewsService;
import com.lollito.fm.mapper.NewsMapper;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/api/news")
public class NewsController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired private NewsService newsService;
	@Autowired private NewsMapper newsMapper;
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long count() {
        return newsService.getCount();
    }
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
    public List<NewsDTO> findAll(Model model) {
        return newsService.findAll().stream()
			.map(newsMapper::toDto)
			.collect(Collectors.toList());
    }
   
}
