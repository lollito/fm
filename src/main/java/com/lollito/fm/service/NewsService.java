package com.lollito.fm.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.lollito.fm.model.News;
import com.lollito.fm.repository.rest.NewsRepository;

@Service
public class NewsService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired NewsRepository newsRepository;
	
	public void save(News news){
		newsRepository.save(news);
	}
	
	public Long getCount() {
		return newsRepository.count();
	}
	
	public List<News> findAll() {
		return newsRepository.findAll();
	}
	
	public List<News> findLatest(int limit) {
		return newsRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"))).getContent();
	}

}
