package com.lollito.fm.repository.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.OfferStatus;
import com.lollito.fm.model.SponsorshipOffer;
import com.lollito.fm.model.SponsorshipType;

@Repository
public interface SponsorshipOfferRepository extends JpaRepository<SponsorshipOffer, Long> {

    List<SponsorshipOffer> findByClubAndStatusAndExpiryDateAfter(Club club, OfferStatus status, LocalDate date);

    List<SponsorshipOffer> findByClubAndTypeAndStatus(Club club, SponsorshipType type, OfferStatus status);

    List<SponsorshipOffer> findByClubAndTypeAndStatusAndIdNot(Club club, SponsorshipType type, OfferStatus status, Long id);
}
