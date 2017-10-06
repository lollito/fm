package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Round;

@Repository
public interface RoundRepository extends PagingAndSortingRepository<Round, Long> {

}