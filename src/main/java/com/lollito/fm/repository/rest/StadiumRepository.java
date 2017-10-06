package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Stadium;

@Repository
public interface StadiumRepository extends PagingAndSortingRepository<Stadium, Long> {

}