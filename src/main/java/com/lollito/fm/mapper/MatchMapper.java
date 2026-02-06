package com.lollito.fm.mapper;

import com.lollito.fm.model.EventHistory;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Stats;
import com.lollito.fm.model.dto.EventHistoryDTO;
import com.lollito.fm.model.dto.MatchDTO;
import com.lollito.fm.model.dto.StatsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ClubMapper.class, FormationMapper.class, MatchPlayerStatsMapper.class})
public interface MatchMapper {

    @Mapping(source = "number", target = "roundNumber")
    MatchDTO toDto(Match match);

    Match toEntity(MatchDTO matchDTO);

    StatsDTO toDto(Stats stats);
    Stats toEntity(StatsDTO statsDTO);

    EventHistoryDTO toDto(EventHistory eventHistory);
    EventHistory toEntity(EventHistoryDTO eventHistoryDTO);
}
