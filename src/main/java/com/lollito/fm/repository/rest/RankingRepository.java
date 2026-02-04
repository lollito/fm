package com.lollito.fm.repository.rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.Season;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

	public Ranking findByClubAndSeason(Club club, Season season);

	public Ranking findFirstByClubAndSeason(Club club, Season season);
}