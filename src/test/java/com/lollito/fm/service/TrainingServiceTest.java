package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Player;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.TrainingFocus;
import com.lollito.fm.model.TrainingIntensity;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.repository.PlayerTrainingResultRepository;
import com.lollito.fm.repository.TrainingPlanRepository;
import com.lollito.fm.repository.TrainingSessionRepository;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private PlayerTrainingResultRepository playerTrainingResultRepository;

    @Mock
    private TrainingPlanRepository trainingPlanRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void testProcessTeamTraining() {
        Team team = new Team();
        team.setId(1L);
        Player player = new Player();
        player.setId(1L);
        player.setScoring(10.0);
        player.setWinger(10.0);
        player.setPassing(10.0);
        player.setCondition(100.0);
        player.setMoral(100.0);
        team.addPlayer(player);

        when(trainingSessionRepository.save(any(TrainingSession.class)))
            .thenAnswer(i -> i.getArguments()[0]);

        TrainingSession session = trainingService.processTeamTraining(
            team, TrainingFocus.ATTACKING, TrainingIntensity.MODERATE);

        assertThat(session).isNotNull();
        assertThat(session.getTeam()).isEqualTo(team);
        assertThat(session.getFocus()).isEqualTo(TrainingFocus.ATTACKING);
        assertThat(session.getIntensity()).isEqualTo(TrainingIntensity.MODERATE);

        verify(playerService).save(player);
        verify(playerTrainingResultRepository).save(any());
    }

    @Test
    void testCalculateEffectiveness() {
        Team team = new Team();
        Double effectiveness = trainingService.calculateEffectiveness(team);
        assertThat(effectiveness).isEqualTo(1.0); // Base value
    }

    @Test
    void testInjuredPlayersSkipTraining() {
        Team team = new Team();
        Player injuredPlayer = new Player();
        injuredPlayer.setId(1L);
        injuredPlayer.setCondition(100.0);
        // We need to simulate injury. Player.isInjured() checks injuries list.
        com.lollito.fm.model.Injury injury = new com.lollito.fm.model.Injury();
        injury.setStatus(com.lollito.fm.model.InjuryStatus.ACTIVE);
        injuredPlayer.getInjuries().add(injury);

        team.addPlayer(injuredPlayer);

        when(trainingSessionRepository.save(any(TrainingSession.class)))
            .thenAnswer(i -> i.getArguments()[0]);

        TrainingSession session = trainingService.processTeamTraining(
            team, TrainingFocus.ATTACKING, TrainingIntensity.MODERATE);

        assertThat(session.getPlayerResults()).isEmpty();
    }
}
