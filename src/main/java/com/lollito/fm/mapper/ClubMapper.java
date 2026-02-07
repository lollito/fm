package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.dto.ClubDTO;

@Mapper(componentModel = "spring")
public interface ClubMapper {
    @Mapping(source = "league.id", target = "leagueId")
    @Mapping(source = "team.id", target = "teamId")
    @Mapping(target = "isHuman", expression = "java(club.getUser() != null)")
    ClubDTO toDto(Club club);
    Club toEntity(ClubDTO dto);
}
