package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.Formation;

@RepositoryRestResource(collectionResourceRel = "formation", path = "formation")
public interface FormationRepository extends PagingAndSortingRepository<Formation, Long> {

}