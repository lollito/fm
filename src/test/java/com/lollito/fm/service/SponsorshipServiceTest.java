package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Finance;
import com.lollito.fm.model.OfferStatus;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.Sponsor;
import com.lollito.fm.model.SponsorTier;
import com.lollito.fm.model.SponsorshipDeal;
import com.lollito.fm.model.SponsorshipOffer;
import com.lollito.fm.model.SponsorshipStatus;
import com.lollito.fm.model.SponsorshipType;
import com.lollito.fm.model.Stadium;
import com.lollito.fm.repository.rest.RankingRepository;
import com.lollito.fm.repository.rest.SponsorRepository;
import com.lollito.fm.repository.rest.SponsorshipDealRepository;
import com.lollito.fm.repository.rest.SponsorshipOfferRepository;
import com.lollito.fm.repository.rest.SponsorshipPaymentRepository;

@ExtendWith(MockitoExtension.class)
public class SponsorshipServiceTest {

    @InjectMocks
    private SponsorshipService sponsorshipService;

    @Mock
    private SponsorRepository sponsorRepository;

    @Mock
    private SponsorshipDealRepository sponsorshipDealRepository;

    @Mock
    private SponsorshipOfferRepository sponsorshipOfferRepository;

    @Mock
    private SponsorshipPaymentRepository sponsorshipPaymentRepository;

    @Mock
    private ClubService clubService;

    @Mock
    private FinancialService financialService;

    @Mock
    private RankingRepository rankingRepository;

    @Mock
    private SeasonService seasonService;

    private Club club;
    private Sponsor sponsor;

    @BeforeEach
    public void setup() {
        club = new Club();
        club.setId(1L);
        club.setName("Test Club");

        Finance finance = new Finance();
        finance.setBalance(BigDecimal.valueOf(1000000));
        club.setFinance(finance);

        Stadium stadium = new Stadium();
        stadium.setGrandstandWest(50000);
        club.setStadium(stadium);

        sponsor = new Sponsor();
        sponsor.setId(1L);
        sponsor.setName("Test Sponsor");
        sponsor.setTier(SponsorTier.PREMIUM);
        Set<SponsorshipType> types = new HashSet<>();
        types.add(SponsorshipType.SHIRT);
        sponsor.setAvailableTypes(types);
    }

    @Test
    public void testGenerateSponsorshipOffers() {
        when(clubService.findById(1L)).thenReturn(club);
        List<Sponsor> sponsors = new ArrayList<>();
        sponsors.add(sponsor);
        when(sponsorRepository.findAll()).thenReturn(sponsors);
        when(sponsorshipOfferRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);

        List<SponsorshipOffer> offers = sponsorshipService.generateSponsorshipOffers(1L);

        assertNotNull(offers);
        assertEquals(1, offers.size());
        assertEquals(SponsorshipType.SHIRT, offers.get(0).getType());
        assertEquals(OfferStatus.PENDING, offers.get(0).getStatus());
    }

    @Test
    public void testAcceptSponsorshipOffer() {
        SponsorshipOffer offer = new SponsorshipOffer();
        offer.setId(1L);
        offer.setClub(club);
        offer.setSponsor(sponsor);
        offer.setType(SponsorshipType.SHIRT);
        offer.setOfferedAnnualValue(BigDecimal.valueOf(1000000));
        offer.setContractYears(2);
        offer.setStatus(OfferStatus.PENDING);
        offer.setExpiryDate(LocalDate.now().plusDays(10));

        when(sponsorshipOfferRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(sponsorshipDealRepository.save(any(SponsorshipDeal.class))).thenAnswer(i -> i.getArguments()[0]);
        when(seasonService.getCurrentSeason()).thenReturn(new Season());

        SponsorshipDeal deal = sponsorshipService.acceptSponsorshipOffer(1L);

        assertNotNull(deal);
        assertEquals(SponsorshipStatus.ACTIVE, deal.getStatus());
        assertEquals(BigDecimal.valueOf(1000000), deal.getCurrentAnnualValue());

        verify(sponsorshipOfferRepository).save(offer);
        assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
    }
}
