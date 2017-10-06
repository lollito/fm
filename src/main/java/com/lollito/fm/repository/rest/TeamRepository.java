package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Team;

@Repository
public interface TeamRepository extends PagingAndSortingRepository<Team, Long> {

}