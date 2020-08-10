package com.lollito.fm.repository.rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
	

}