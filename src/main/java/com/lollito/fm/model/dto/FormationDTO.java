package com.lollito.fm.model.dto;

import java.util.List;
import com.lollito.fm.dto.PlayerDTO;
import com.lollito.fm.model.Mentality;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormationDTO {
    private Long id;
    private ModuleDTO module;
    private Boolean haveBall;
    private List<PlayerDTO> players;
    private List<PlayerDTO> substitutes;
    private Mentality mentality;
}
