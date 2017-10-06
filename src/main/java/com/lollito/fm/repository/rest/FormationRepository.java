package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Formation;

@Repository
public interface FormationRepository extends PagingAndSortingRepository<Formation, Long> {

}