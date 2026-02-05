package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lollito.fm.model.LoanAgreement;
import com.lollito.fm.model.LoanPerformanceReview;
import com.lollito.fm.model.LoanProposal;
import com.lollito.fm.model.dto.*;
import com.lollito.fm.service.LoanService;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/proposal")
    public ResponseEntity<LoanProposalDTO> createLoanProposal(@RequestBody CreateLoanProposalRequest request) {
        LoanProposal proposal = loanService.createLoanProposal(request);
        return ResponseEntity.ok(convertToDTO(proposal));
    }

    @PostMapping("/proposal/{proposalId}/accept")
    public ResponseEntity<LoanAgreementDTO> acceptLoanProposal(@PathVariable Long proposalId) {
        LoanAgreement agreement = loanService.acceptLoanProposal(proposalId);
        return ResponseEntity.ok(convertToDTO(agreement));
    }

    @PostMapping("/proposal/{proposalId}/reject")
    public ResponseEntity<Void> rejectLoanProposal(
            @PathVariable Long proposalId,
            @RequestBody RejectProposalRequest request) {
        loanService.rejectLoanProposal(proposalId, request.getReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/club/{clubId}/active")
    public ResponseEntity<List<LoanAgreementDTO>> getActiveLoans(@PathVariable Long clubId) {
        List<LoanAgreement> loans = loanService.getClubActiveLoans(clubId);
        return ResponseEntity.ok(loans.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/agreement/{loanId}/recall")
    public ResponseEntity<Void> recallPlayer(
            @PathVariable Long loanId,
            @RequestBody RecallPlayerRequest request) {
        loanService.recallPlayerFromLoan(loanId, request.getReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/agreement/{loanId}/purchase")
    public ResponseEntity<TransferOfferDTO> activatePurchaseOption(@PathVariable Long loanId) {
        TransferOfferDTO offer = loanService.activatePurchaseOption(loanId);
        return ResponseEntity.ok(offer);
    }

    @GetMapping("/agreement/{loanId}/reviews")
    public ResponseEntity<List<LoanPerformanceReviewDTO>> getPerformanceReviews(
            @PathVariable Long loanId) {
        List<LoanPerformanceReview> reviews = loanService.getPerformanceReviews(loanId);
        return ResponseEntity.ok(reviews.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    // DTO Converters

    private LoanProposalDTO convertToDTO(LoanProposal proposal) {
        return LoanProposalDTO.builder()
            .id(proposal.getId())
            .playerId(proposal.getPlayer().getId())
            .playerName(proposal.getPlayer().getName() + " " + proposal.getPlayer().getSurname())
            .proposingClubId(proposal.getProposingClub().getId())
            .proposingClubName(proposal.getProposingClub().getName())
            .targetClubId(proposal.getTargetClub().getId())
            .targetClubName(proposal.getTargetClub().getName())
            .status(proposal.getStatus())
            .proposedStartDate(proposal.getProposedStartDate())
            .proposedEndDate(proposal.getProposedEndDate())
            .proposedLoanFee(proposal.getProposedLoanFee())
            .proposedSalaryShare(proposal.getProposedSalaryShare())
            .proposedRecallClause(proposal.getProposedRecallClause())
            .proposedOptionToBuy(proposal.getProposedOptionToBuy())
            .proposedOptionPrice(proposal.getProposedOptionPrice())
            .proposalMessage(proposal.getProposalMessage())
            .rejectionReason(proposal.getRejectionReason())
            .proposalDate(proposal.getProposalDate())
            .responseDate(proposal.getResponseDate())
            .expiryDate(proposal.getExpiryDate())
            .build();
    }

    private LoanAgreementDTO convertToDTO(LoanAgreement agreement) {
        return LoanAgreementDTO.builder()
            .id(agreement.getId())
            .playerId(agreement.getPlayer().getId())
            .playerName(agreement.getPlayer().getName())
            .playerSurname(agreement.getPlayer().getSurname())
            .parentClubId(agreement.getParentClub().getId())
            .parentClubName(agreement.getParentClub().getName())
            .loanClubId(agreement.getLoanClub().getId())
            .loanClubName(agreement.getLoanClub().getName())
            .startDate(agreement.getStartDate())
            .endDate(agreement.getEndDate())
            .status(agreement.getStatus())
            .loanFee(agreement.getLoanFee())
            .parentClubSalaryShare(agreement.getParentClubSalaryShare())
            .loanClubSalaryShare(agreement.getLoanClubSalaryShare())
            .hasRecallClause(agreement.getHasRecallClause())
            .earliestRecallDate(agreement.getEarliestRecallDate())
            .hasOptionToBuy(agreement.getHasOptionToBuy())
            .optionToBuyPrice(agreement.getOptionToBuyPrice())
            .hasObligationToBuy(agreement.getHasObligationToBuy())
            .obligationConditions(agreement.getObligationConditions())
            .minimumAppearances(agreement.getMinimumAppearances())
            .actualAppearances(agreement.getActualAppearances())
            .developmentTargetsMet(agreement.getDevelopmentTargetsMet())
            .developmentTargets(agreement.getDevelopmentTargets())
            .loanReason(agreement.getLoanReason())
            .specialConditions(agreement.getSpecialConditions())
            .agreementDate(agreement.getAgreementDate())
            .build();
    }

    private LoanPerformanceReviewDTO convertToDTO(LoanPerformanceReview review) {
        return LoanPerformanceReviewDTO.builder()
            .id(review.getId())
            .loanAgreementId(review.getLoanAgreement().getId())
            .reviewDate(review.getReviewDate())
            .period(review.getPeriod())
            .matchesPlayed(review.getMatchesPlayed())
            .goals(review.getGoals())
            .assists(review.getAssists())
            .averageRating(review.getAverageRating())
            .yellowCards(review.getYellowCards())
            .redCards(review.getRedCards())
            .skillImprovement(review.getSkillImprovement())
            .developmentNotes(review.getDevelopmentNotes())
            .targetsMet(review.getTargetsMet())
            .recommendation(review.getRecommendation())
            .reviewNotes(review.getReviewNotes())
            .parentClubFeedback(review.getParentClubFeedback())
            .loanClubFeedback(review.getLoanClubFeedback())
            .build();
    }
}
