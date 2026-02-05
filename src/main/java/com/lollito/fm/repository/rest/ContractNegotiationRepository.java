package com.lollito.fm.repository.rest;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.ContractNegotiation;
import com.lollito.fm.model.NegotiationStatus;
import com.lollito.fm.model.Player;

@Repository
public interface ContractNegotiationRepository extends JpaRepository<ContractNegotiation, Long> {
    Optional<ContractNegotiation> findByPlayerAndClubAndStatus(Player player, Club club, NegotiationStatus status);
    List<ContractNegotiation> findByClubAndStatus(Club club, NegotiationStatus status);
    List<ContractNegotiation> findByClub(Club club);
}
