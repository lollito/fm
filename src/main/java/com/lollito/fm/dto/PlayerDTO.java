package com.lollito.fm.dto;

import java.time.LocalDate;

import com.lollito.fm.model.Foot;
import com.lollito.fm.model.PlayerRole;

import lombok.Data;

@Data
public class PlayerDTO {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birth;
    private PlayerRole role;
    private Foot preferredFoot;

    private Double stamina;
    private Double playmaking;
    private Double scoring;
    private Double winger;
    private Double goalkeeping;
    private Double passing;
    private Double defending;
    private Double setPieces;
}
