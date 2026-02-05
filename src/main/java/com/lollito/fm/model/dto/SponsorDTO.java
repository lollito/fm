package com.lollito.fm.model.dto;

import java.io.Serializable;
import com.lollito.fm.model.SponsorTier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorDTO implements Serializable {
    private Long id;
    private String name;
    private String logo;
    private String industry;
    private SponsorTier tier;
}
