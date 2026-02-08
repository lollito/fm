package com.lollito.fm.model.dto;

import com.lollito.fm.model.ManagerPerk;
import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class ManagerProfileDTO {
    private Long id;
    private Integer level;
    private Long currentXp;
    private Integer talentPoints;
    private Set<ManagerPerk> unlockedPerks;
    private Long xpForNextLevel;
}
