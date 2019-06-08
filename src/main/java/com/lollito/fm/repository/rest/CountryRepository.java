package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
	
	public List<Country> findByCreateLeague(Boolean createLeague);

}