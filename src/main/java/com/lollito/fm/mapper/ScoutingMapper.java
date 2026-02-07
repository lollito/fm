package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.dto.PlayerScoutingStatusDTO;
import com.lollito.fm.dto.ScoutDTO;
import com.lollito.fm.dto.ScoutingAssignmentDTO;
import com.lollito.fm.dto.ScoutingReportDTO;
import com.lollito.fm.model.PlayerScoutingStatus;
import com.lollito.fm.model.Scout;
import com.lollito.fm.model.ScoutingAssignment;
import com.lollito.fm.model.ScoutingReport;

@Mapper(componentModel = "spring")
public interface ScoutingMapper {
    @Mapping(source = "club.id", target = "clubId")
    @Mapping(source = "scoutingRegion.id", target = "regionId")
    @Mapping(source = "scoutingRegion.name", target = "regionName")
    ScoutDTO toDto(Scout scout);

    @Mapping(source = "targetPlayer.id", target = "targetPlayerId")
    @Mapping(source = "targetPlayer.name", target = "targetPlayerName")
    @Mapping(source = "targetPlayer.surname", target = "targetPlayerSurname")
    ScoutingAssignmentDTO toDto(ScoutingAssignment assignment);

    @Mapping(source = "assignment.id", target = "assignmentId")
    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "player.name", target = "playerName")
    @Mapping(source = "player.surname", target = "playerSurname")
    ScoutingReportDTO toDto(ScoutingReport report);

    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "scoutingClub.id", target = "clubId")
    PlayerScoutingStatusDTO toDto(PlayerScoutingStatus status);
}
