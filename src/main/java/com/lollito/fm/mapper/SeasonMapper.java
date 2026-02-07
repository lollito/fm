package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.model.Season;
import com.lollito.fm.model.dto.SeasonDTO;

@Mapper(componentModel = "spring")
public interface SeasonMapper {
    @Mapping(source = "league.id", target = "leagueId")
    SeasonDTO toDto(Season season);
}
