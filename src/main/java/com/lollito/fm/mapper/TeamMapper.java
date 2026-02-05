package com.lollito.fm.mapper;

import com.lollito.fm.model.Team;
import com.lollito.fm.model.dto.TeamDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    TeamDTO toDto(Team team);
    Team toEntity(TeamDTO dto);
}
