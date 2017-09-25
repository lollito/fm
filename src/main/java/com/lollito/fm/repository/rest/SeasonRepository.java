package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.Season;

@RepositoryRestResource(collectionResourceRel = "season", path = "season")
public interface SeasonRepository extends PagingAndSortingRepository<Season, Long> {

}