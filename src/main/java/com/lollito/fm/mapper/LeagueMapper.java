package com.lollito.fm.mapper;

import com.lollito.fm.model.League;
import com.lollito.fm.model.dto.LeagueDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LeagueMapper {
    LeagueDTO toDto(League league);
    League toEntity(LeagueDTO dto);
}
