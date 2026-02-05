package com.lollito.fm.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferOfferDTO {
    private Long id;
    private Long playerId;
    private Long buyingClubId;
    private Long sellingClubId;
    private BigDecimal offerAmount;
    private Boolean isReleaseClause;
    private String status;
    private LocalDateTime offerDate;
}
