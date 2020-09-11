package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.rest.PlayerCondition;

@Repository
public interface PlayerRepository extends PagingAndSortingRepository<Player, Long> {

	public List<PlayerCondition> findAllBy(Pageable pageable);
	
	public List<Player> findByOnSale(Boolean onSale);
}