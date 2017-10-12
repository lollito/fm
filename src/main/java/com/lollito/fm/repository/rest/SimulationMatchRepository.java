package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Formation;
import com.lollito.fm.model.SimulationMatch;

@Repository
public interface SimulationMatchRepository extends PagingAndSortingRepository<SimulationMatch, Long> {

//	public List<SimulationMatch> findByHomeFormationOrAwayFormation(Formation homeFormation, Formation awayFormation);
}
