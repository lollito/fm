package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.model.Event;
import com.lollito.fm.model.EventHistory;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.Stats;
import com.lollito.fm.model.dto.EventHistoryDTO;
import com.lollito.fm.model.dto.MatchDTO;
import com.lollito.fm.model.dto.StatsDTO;

@Mapper(componentModel = "spring", uses = {ClubMapper.class, FormationMapper.class, MatchPlayerStatsMapper.class})
public interface MatchMapper {

    @Mapping(source = "number", target = "roundNumber")
    MatchDTO toDto(Match match);

    Match toEntity(MatchDTO matchDTO);

    StatsDTO toDto(Stats stats);
    Stats toEntity(StatsDTO statsDTO);

    @Mapping(target = "eventType", expression = "java(mapEventType(eventHistory.getType()))")
    @Mapping(source = "event", target = "description")
    @Mapping(target = "isKeyEvent", expression = "java(isKeyEvent(eventHistory.getType()))")
    EventHistoryDTO toDto(EventHistory eventHistory);

    EventHistory toEntity(EventHistoryDTO eventHistoryDTO);

    default String mapEventType(Event event) {
        if (event == null) return "NORMAL";
        switch (event) {
            case HAVE_SCORED: return "GOAL";
            case HAVE_SCORED_FREE_KICK: return "GOAL";
            case HAVE_CORNER: return "CORNER";
            case COMMITS_FAUL: return "FOUL";
            case YELLOW_CARD: return "YELLOW_CARD";
            case RED_CARD: return "RED_CARD";
            case SHOT_AND_MISSED: return "SHOT_OFF_TARGET";
            case SUBSTITUTION: return "SUBSTITUTION";
            case INJURY: return "INJURY";
            default: return "NORMAL";
        }
    }

    default Boolean isKeyEvent(Event event) {
        if (event == null) return false;
        switch (event) {
            case HAVE_SCORED:
            case HAVE_SCORED_FREE_KICK:
            case RED_CARD:
                return true;
            default:
                return false;
        }
    }
}
