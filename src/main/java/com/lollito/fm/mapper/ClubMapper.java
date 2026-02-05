package com.lollito.fm.mapper;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.dto.ClubDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClubMapper {
    @Mapping(source = "league.id", target = "leagueId")
    @Mapping(source = "team.id", target = "teamId")
    ClubDTO toDto(Club club);
    Club toEntity(ClubDTO dto);
}
