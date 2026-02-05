package com.lollito.fm.model.dto;

import java.math.BigDecimal;

import com.lollito.fm.model.UpgradeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpgradeOptionDTO {
    private UpgradeType upgradeType;
    private String name;
    private String description;
    private BigDecimal cost;
    private Integer durationDays;
    private String effects;
    private String requirements;
}
