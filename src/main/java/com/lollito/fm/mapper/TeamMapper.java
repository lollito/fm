package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.Team;
import com.lollito.fm.model.dto.TeamDTO;

import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    @Mapping(source = "formation.id", target = "formationId")
    TeamDTO toDto(Team team);
    Team toEntity(TeamDTO dto);
}
