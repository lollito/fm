package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.SimulationMatch;

@RepositoryRestResource(collectionResourceRel = "simulationMatch", path = "simulationMatch")
public interface SimulationMatchRepository extends PagingAndSortingRepository<SimulationMatch, Long> {

}
