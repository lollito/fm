package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.Formation;
import com.lollito.fm.model.dto.FormationDTO;

@Mapper(componentModel = "spring", uses = {ModuleMapper.class, PlayerMapper.class})
public interface FormationMapper {
    FormationDTO toDto(Formation formation);
    Formation toEntity(FormationDTO dto);
}
