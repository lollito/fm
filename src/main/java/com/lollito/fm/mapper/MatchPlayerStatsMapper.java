package com.lollito.fm.mapper;

import org.mapstruct.Mapper;

import com.lollito.fm.model.MatchPlayerStats;
import com.lollito.fm.model.dto.MatchPlayerStatsDTO;

@Mapper(componentModel = "spring", uses = {PlayerMapper.class})
public interface MatchPlayerStatsMapper {
    MatchPlayerStatsDTO toDto(MatchPlayerStats matchPlayerStats);
    MatchPlayerStats toEntity(MatchPlayerStatsDTO matchPlayerStatsDTO);
}
