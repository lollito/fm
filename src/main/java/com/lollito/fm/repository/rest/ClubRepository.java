package com.lollito.fm.repository.rest;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;

@Repository
public interface ClubRepository extends PagingAndSortingRepository<Club, Long> {
	public Optional<Club> findTopByLeagueCountryAndUserIsNull(Country country);
	public Optional<Club> findTopByLeagueCountry(Country country);
}