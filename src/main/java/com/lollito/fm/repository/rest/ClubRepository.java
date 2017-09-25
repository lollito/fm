package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.Club;

@RepositoryRestResource(collectionResourceRel = "club", path = "club")
public interface ClubRepository extends PagingAndSortingRepository<Club, Long> {

}