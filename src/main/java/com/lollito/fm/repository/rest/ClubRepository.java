package com.lollito.fm.repository.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Country;
import com.lollito.fm.model.League;
import com.lollito.fm.model.Server;
import com.lollito.fm.model.Team;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {

	@Query("SELECT DISTINCT c FROM Club c LEFT JOIN FETCH c.team WHERE c.league IN :leagues")
	public List<Club> findAllByLeagueInWithTeam(@Param("leagues") List<League> leagues);

	public Optional<Club> findTopByLeagueCountryAndUserIsNull(Country country);
	public Optional<Club> findTopByLeagueServerAndLeagueCountryAndUserIsNull(Server server, Country country);
	public Optional<Club> findTopByLeagueCountry(Country country);

	Optional<Club> findByTeam(Team team);
	Optional<Club> findByUnder18(Team team);
}