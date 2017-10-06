package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Season;

@Repository
public interface SeasonRepository extends PagingAndSortingRepository<Season, Long> {

}