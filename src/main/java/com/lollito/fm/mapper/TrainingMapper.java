package com.lollito.fm.mapper;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.TrainingPlan;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.model.PlayerTrainingResult;
import com.lollito.fm.model.dto.TrainingPlanDTO;
import com.lollito.fm.model.dto.TrainingSessionDTO;
import com.lollito.fm.model.dto.PlayerTrainingResultDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    @Mapping(source = "team.id", target = "teamId")
    TrainingPlanDTO toDto(TrainingPlan plan);

    @Mapping(source = "team.id", target = "teamId")
    TrainingSessionDTO toDto(TrainingSession session);

    @Mapping(source = "trainingSession.id", target = "trainingSessionId")
    PlayerTrainingResultDTO toDto(PlayerTrainingResult result);

    PlayerTrainingResultDTO.PlayerSummaryDTO toPlayerSummaryDto(Player player);
}
