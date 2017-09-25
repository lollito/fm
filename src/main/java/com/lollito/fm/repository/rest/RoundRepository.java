package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.Round;

@RepositoryRestResource(collectionResourceRel = "round", path = "round")
public interface RoundRepository extends PagingAndSortingRepository<Round, Long> {

}