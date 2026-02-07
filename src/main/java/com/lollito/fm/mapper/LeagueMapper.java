package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.League;
import com.lollito.fm.model.dto.LeagueDTO;

@Mapper(componentModel = "spring")
public interface LeagueMapper {
    LeagueDTO toDto(League league);
    League toEntity(LeagueDTO dto);
}
