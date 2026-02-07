package com.lollito.fm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.Foot;
import com.lollito.fm.model.PlayerRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birth;
    private Integer age;
    private PlayerRole role;
    private Foot preferredFoot;
    private BigDecimal salary;

    private Double stamina;
    private Double playmaking;
    private Double scoring;
    private Double winger;
    private Double goalkeeping;
    private Double passing;
    private Double defending;
    private Double setPieces;
    private Long clubId;
    private BigDecimal price;
    private Double condition;
    private Double moral;
}
