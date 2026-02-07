package com.lollito.fm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerTrainingResult;
import com.lollito.fm.model.TrainingPlan;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.model.dto.PlayerTrainingResultDTO;
import com.lollito.fm.model.dto.TrainingPlanDTO;
import com.lollito.fm.model.dto.TrainingSessionDTO;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    @Mapping(source = "team.id", target = "teamId")
    TrainingPlanDTO toDto(TrainingPlan plan);

    @Mapping(source = "team.id", target = "teamId")
    @Mapping(target = "playerCount", expression = "java(session.getPlayerResults() != null ? session.getPlayerResults().size() : 0)")
    @Mapping(target = "playerResults", ignore = true)
    TrainingSessionDTO toDto(TrainingSession session);

    @Mapping(source = "trainingSession.id", target = "trainingSessionId")
    PlayerTrainingResultDTO toDto(PlayerTrainingResult result);

    PlayerTrainingResultDTO.PlayerSummaryDTO toPlayerSummaryDto(Player player);
}
