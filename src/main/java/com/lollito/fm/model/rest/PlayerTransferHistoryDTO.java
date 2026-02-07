package com.lollito.fm.model.rest;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.TransferType;
import com.lollito.fm.model.dto.ClubDTO;
import com.lollito.fm.model.dto.SeasonDTO;

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
    private ClubDTO fromClub;
    private ClubDTO toClub;
    private LocalDate transferDate;
    private BigDecimal transferFee;
    private TransferType transferType;
    private SeasonDTO season;
}
