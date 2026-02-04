package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.SponsorshipDeal;

@Repository
public interface SponsorshipDealRepository extends JpaRepository<SponsorshipDeal, Long> {

    List<SponsorshipDeal> findByClub(Club club);
}
