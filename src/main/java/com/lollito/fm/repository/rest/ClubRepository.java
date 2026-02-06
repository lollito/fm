package com.lollito.fm.repository.rest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.Team;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
	public Optional<Club> findTopByLeagueCountryAndUserIsNull(Country country);
	public Optional<Club> findTopByLeagueServerAndLeagueCountryAndUserIsNull(Server server, Country country);
	public Optional<Club> findTopByLeagueCountry(Country country);

	Optional<Club> findByTeam(Team team);
	Optional<Club> findByUnder18(Team team);
}