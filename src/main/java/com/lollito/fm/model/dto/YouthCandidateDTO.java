package com.lollito.fm.model.dto;

import java.io.Serializable;

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
public class YouthCandidateDTO implements Serializable {
    private Long id;
    private String name;
    private String surname;
    private Integer age;
    private PlayerRole role;
    private Foot preferredFoot;
    private String nationality;

    // Attributes
    private Double stamina;
    private Double playmaking;
    private Double scoring;
    private Double winger;
    private Double goalkeeping;
    private Double passing;
    private Double defending;
    private Double setPieces;

    private Integer average;
}
