package com.lollito.fm.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.SponsorshipDeal;
import com.lollito.fm.model.SponsorshipOffer;
import com.lollito.fm.model.dto.NegotiationRequest;
import com.lollito.fm.model.dto.SponsorshipDashboardDTO;
import com.lollito.fm.model.dto.SponsorshipDealDTO;
import com.lollito.fm.model.dto.SponsorshipOfferDTO;
import com.lollito.fm.service.SponsorshipService;

@RestController
@RequestMapping("/api/sponsorship")
public class SponsorshipController {

    @Autowired
    private SponsorshipService sponsorshipService;

    @GetMapping("/club/{clubId}/dashboard")
    public ResponseEntity<SponsorshipDashboardDTO> getSponsorshipDashboard(@PathVariable Long clubId) {
        SponsorshipDashboardDTO dashboard = sponsorshipService.getSponsorshipDashboard(clubId);
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/club/{clubId}/generate-offers")
    public ResponseEntity<List<SponsorshipOfferDTO>> generateOffers(@PathVariable Long clubId) {
        List<SponsorshipOffer> offers = sponsorshipService.generateSponsorshipOffers(clubId);
        return ResponseEntity.ok(offers.stream()
            .map(sponsorshipService::convertToOfferDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/offer/{offerId}/accept")
    public ResponseEntity<SponsorshipDealDTO> acceptOffer(@PathVariable Long offerId) {
        SponsorshipDeal deal = sponsorshipService.acceptSponsorshipOffer(offerId);
        return ResponseEntity.ok(sponsorshipService.convertToDealDTO(deal));
    }

    @PostMapping("/offer/{offerId}/reject")
    public ResponseEntity<Void> rejectOffer(@PathVariable Long offerId) {
        sponsorshipService.rejectSponsorshipOffer(offerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/offer/{offerId}/negotiate")
    public ResponseEntity<SponsorshipOfferDTO> negotiateOffer(
            @PathVariable Long offerId,
            @RequestBody NegotiationRequest request) {
        SponsorshipOffer offer = sponsorshipService.negotiateOffer(offerId, request);
        return ResponseEntity.ok(sponsorshipService.convertToOfferDTO(offer));
    }

    @GetMapping("/club/{clubId}/deals")
    public ResponseEntity<List<SponsorshipDealDTO>> getActiveDeals(@PathVariable Long clubId) {
        List<SponsorshipDeal> deals = sponsorshipService.getActiveDeals(clubId);
        return ResponseEntity.ok(deals.stream()
            .map(sponsorshipService::convertToDealDTO)
            .collect(Collectors.toList()));
    }

    @GetMapping("/club/{clubId}/offers")
    public ResponseEntity<List<SponsorshipOfferDTO>> getPendingOffers(@PathVariable Long clubId) {
        List<SponsorshipOffer> offers = sponsorshipService.getPendingOffers(clubId);
        return ResponseEntity.ok(offers.stream()
            .map(sponsorshipService::convertToOfferDTO)
            .collect(Collectors.toList()));
    }
}
