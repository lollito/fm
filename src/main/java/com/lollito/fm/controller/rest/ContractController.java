package com.lollito.fm.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.Contract;
import com.lollito.fm.model.ContractNegotiation;
import com.lollito.fm.model.NegotiationOffer;
import com.lollito.fm.model.NegotiationStatus;
import com.lollito.fm.model.dto.*;
import com.lollito.fm.service.ContractService;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @GetMapping("/player/{playerId}")
    public ResponseEntity<ContractDTO> getPlayerContract(@PathVariable Long playerId) {
        Contract contract = contractService.getPlayerCurrentContract(playerId);
        return ResponseEntity.ok(convertToDTO(contract));
    }

    @PostMapping("/negotiate")
    public ResponseEntity<ContractNegotiationDTO> startNegotiation(
            @RequestBody StartNegotiationRequest request) {
        ContractNegotiation negotiation = contractService.startNegotiation(
            request.getPlayerId(),
            request.getClubId(),
            request.getType(),
            request.getInitialOffer()
        );
        return ResponseEntity.ok(convertToDTO(negotiation));
    }

    @PostMapping("/negotiate/{negotiationId}/offer")
    public ResponseEntity<NegotiationOfferDTO> makeCounterOffer(
            @PathVariable Long negotiationId,
            @RequestBody CounterOfferRequest request) {
        NegotiationOffer offer = contractService.makeCounterOffer(
            negotiationId,
            request.getOfferSide(),
            request.getOffer()
        );
        return ResponseEntity.ok(convertToDTO(offer));
    }

    @PostMapping("/negotiate/{negotiationId}/accept")
    public ResponseEntity<ContractDTO> acceptOffer(@PathVariable Long negotiationId) {
        Contract contract = contractService.acceptOffer(negotiationId);
        return ResponseEntity.ok(convertToDTO(contract));
    }

    @PostMapping("/negotiate/{negotiationId}/reject")
    public ResponseEntity<Void> rejectOffer(
            @PathVariable Long negotiationId,
            @RequestBody RejectOfferRequest request) {
        contractService.rejectOffer(negotiationId, request.getReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/club/{clubId}/negotiations")
    public ResponseEntity<List<ContractNegotiationDTO>> getClubNegotiations(
            @PathVariable Long clubId,
            @RequestParam(required = false) NegotiationStatus status) {
        List<ContractNegotiation> negotiations = contractService.getClubNegotiations(clubId, status);
        return ResponseEntity.ok(negotiations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    @PostMapping("/{contractId}/bonus")
    public ResponseEntity<Void> addPerformanceBonus(
            @PathVariable Long contractId,
            @RequestBody AddPerformanceBonusRequest request) {
        contractService.addPerformanceBonus(
            contractId,
            request.getType(),
            request.getTargetValue(),
            request.getBonusAmount(),
            request.getDescription()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release-clause/{playerId}")
    public ResponseEntity<TransferOfferDTO> triggerReleaseClause(
            @PathVariable Long playerId,
            @RequestBody TriggerReleaseClauseRequest request) {
        TransferOfferDTO offer = contractService.triggerReleaseClause(playerId, request.getBuyingClubId());
        return ResponseEntity.ok(offer);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<ContractDTO>> getExpiringContracts(
            @RequestParam(defaultValue = "6") int monthsAhead) {
        List<Contract> contracts = contractService.getExpiringContracts(monthsAhead);
        return ResponseEntity.ok(contracts.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }

    private ContractDTO convertToDTO(Contract contract) {
        if (contract == null) return null;
        return ContractDTO.builder()
            .id(contract.getId())
            .playerId(contract.getPlayer().getId())
            .clubId(contract.getClub().getId())
            .weeklySalary(contract.getWeeklySalary())
            .signingBonus(contract.getSigningBonus())
            .loyaltyBonus(contract.getLoyaltyBonus())
            .startDate(contract.getStartDate())
            .endDate(contract.getEndDate())
            .releaseClause(contract.getReleaseClause())
            .hasReleaseClause(contract.getHasReleaseClause())
            .status(contract.getStatus())
            .negotiationAttempts(contract.getNegotiationAttempts())
            .build();
    }

    private ContractNegotiationDTO convertToDTO(ContractNegotiation negotiation) {
        if (negotiation == null) return null;
        return ContractNegotiationDTO.builder()
            .id(negotiation.getId())
            .playerId(negotiation.getPlayer().getId())
            .clubId(negotiation.getClub().getId())
            .type(negotiation.getType())
            .status(negotiation.getStatus())
            .offeredWeeklySalary(negotiation.getOfferedWeeklySalary())
            .offeredSigningBonus(negotiation.getOfferedSigningBonus())
            .offeredLoyaltyBonus(negotiation.getOfferedLoyaltyBonus())
            .offeredContractYears(negotiation.getOfferedContractYears())
            .offeredReleaseClause(negotiation.getOfferedReleaseClause())
            .demandedWeeklySalary(negotiation.getDemandedWeeklySalary())
            .demandedSigningBonus(negotiation.getDemandedSigningBonus())
            .demandedLoyaltyBonus(negotiation.getDemandedLoyaltyBonus())
            .demandedContractYears(negotiation.getDemandedContractYears())
            .demandedReleaseClause(negotiation.getDemandedReleaseClause())
            .startDate(negotiation.getStartDate())
            .expiryDate(negotiation.getExpiryDate())
            .lastOfferDate(negotiation.getLastOfferDate())
            .roundsOfNegotiation(negotiation.getRoundsOfNegotiation())
            .rejectionReason(negotiation.getRejectionReason())
            .build();
    }

    private NegotiationOfferDTO convertToDTO(NegotiationOffer offer) {
        if (offer == null) return null;
        return NegotiationOfferDTO.builder()
            .id(offer.getId())
            .negotiationId(offer.getNegotiation().getId())
            .offerSide(offer.getOfferSide())
            .weeklySalary(offer.getWeeklySalary())
            .signingBonus(offer.getSigningBonus())
            .loyaltyBonus(offer.getLoyaltyBonus())
            .contractYears(offer.getContractYears())
            .releaseClause(offer.getReleaseClause())
            .offerDate(offer.getOfferDate())
            .status(offer.getStatus())
            .notes(offer.getNotes())
            .build();
    }
}
