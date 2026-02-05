package com.lollito.fm.repository.rest;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.PaymentStatus;
import com.lollito.fm.model.SponsorshipPayment;

@Repository
public interface SponsorshipPaymentRepository extends JpaRepository<SponsorshipPayment, Long> {

    List<SponsorshipPayment> findByStatusAndDueDateLessThanEqual(PaymentStatus status, LocalDate date);

    List<SponsorshipPayment> findByStatusAndDueDateLessThan(PaymentStatus status, LocalDate date);

    List<SponsorshipPayment> findTop10BySponsorshipDeal_ClubOrderByPaidDateDesc(Club club);
}
