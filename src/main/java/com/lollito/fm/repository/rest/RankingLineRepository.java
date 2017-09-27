package com.lollito.fm.repository.rest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Ranking;
import com.lollito.fm.model.RankingLine;

@RepositoryRestResource(collectionResourceRel = "rankingline", path = "rankingline")
public interface RankingLineRepository extends JpaRepository<RankingLine, Long> {

	public RankingLine findByClubAndRanking(Club club, Ranking ranking);
}