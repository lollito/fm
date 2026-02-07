package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.rest.PlayerCondition;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

	@Query("SELECT p FROM Player p WHERE p.team.id IN :teamIds")
	public List<Player> findByTeamIdIn(@Param("teamIds") List<Long> teamIds);

	public List<PlayerCondition> findAllBy(Pageable pageable);
	
	public List<Player> findByOnSale(Boolean onSale);
}