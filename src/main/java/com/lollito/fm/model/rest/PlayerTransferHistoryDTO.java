package com.lollito.fm.model.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Season;
import com.lollito.fm.model.TransferType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTransferHistoryDTO {
    private Long id;
    private Club fromClub;
    private Club toClub;
    private LocalDate transferDate;
    private BigDecimal transferFee;
    private TransferType transferType;
    private Season season;
}
