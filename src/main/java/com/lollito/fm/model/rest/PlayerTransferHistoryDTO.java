package com.lollito.fm.model.rest;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;
import com.lollito.fm.model.TransferType;
import com.lollito.fm.model.Club;
import com.lollito.fm.model.Season;

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
