package com.lollito.fm.repository.rest;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Player;

@Repository
public interface PlayerRepository extends PagingAndSortingRepository<Player, Long> {

}