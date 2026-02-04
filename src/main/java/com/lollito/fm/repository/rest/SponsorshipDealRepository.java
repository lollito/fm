package com.lollito.fm.repository.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.SponsorshipDeal;
import com.lollito.fm.model.SponsorshipStatus;
import com.lollito.fm.model.SponsorshipType;

@Repository
public interface SponsorshipDealRepository extends JpaRepository<SponsorshipDeal, Long> {

    List<SponsorshipDeal> findByClub(Club club);

    List<SponsorshipDeal> findByClubAndStatus(Club club, SponsorshipStatus status);

    List<SponsorshipDeal> findByStatusAndEndDateAfter(SponsorshipStatus status, LocalDate date);

    boolean existsByClubAndTypeAndStatus(Club club, SponsorshipType type, SponsorshipStatus status);
}
