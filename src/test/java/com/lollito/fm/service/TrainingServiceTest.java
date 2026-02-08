package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
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
import com.lollito.fm.model.Club;
import com.lollito.fm.model.TrainingFacility;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.dto.StaffBonusesDTO;
import com.lollito.fm.repository.PlayerTrainingFocusRepository;
import com.lollito.fm.repository.PlayerTrainingResultRepository;
import com.lollito.fm.repository.TrainingPlanRepository;
import com.lollito.fm.repository.TrainingSessionRepository;
import com.lollito.fm.repository.rest.ClubRepository;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private PlayerTrainingFocusRepository playerTrainingFocusRepository;

    @Mock
    private PlayerTrainingResultRepository playerTrainingResultRepository;

    @Mock
    private TrainingPlanRepository trainingPlanRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private TeamService teamService;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private StaffService staffService;

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

        verify(playerService).saveAll(any());
        verify(playerService, times(0)).save(any());
        verify(playerTrainingResultRepository, times(0)).save(any());

        assertThat(session.getPlayerResults()).hasSize(1);
    }

    @Test
    void testCalculateEffectivenessWithFacility() {
        Team team = new Team();
        team.setId(1L);
        Club club = new Club();
        club.setId(10L);
        TrainingFacility facility = new TrainingFacility();
        facility.setOverallQuality(5);
        club.setTrainingFacility(facility);

        when(clubRepository.findByTeam(team)).thenReturn(Optional.of(club));
        when(staffService.calculateClubStaffBonuses(10L)).thenReturn(null);

        Double effectiveness = trainingService.calculateEffectiveness(team, TrainingFocus.ATTACKING);

        // Base 1.0 + (5 * 0.05) = 1.25
        assertThat(effectiveness).isEqualTo(1.25);
    }

    @Test
    void testCalculateEffectiveness() {
        Team team = new Team();
        Double effectiveness = trainingService.calculateEffectiveness(team, TrainingFocus.BALANCED);
        assertThat(effectiveness).isEqualTo(1.0); // Base value
    }

    @Test
    void testCalculateEffectivenessWithSpecificBonuses() {
        Team team = new Team();
        team.setId(1L);
        Club club = new Club();
        club.setId(10L);

        when(clubRepository.findByTeam(team)).thenReturn(Optional.of(club));

        StaffBonusesDTO bonuses = StaffBonusesDTO.builder()
            .goalkeepingBonus(0.2)
            .attackingBonus(0.3)
            .build();

        when(staffService.calculateClubStaffBonuses(10L)).thenReturn(bonuses);

        // Test Goalkeeping Focus
        Double effectivenessGK = trainingService.calculateEffectiveness(team, TrainingFocus.GOALKEEPING);
        // Base 1.0 + 0.2 = 1.2
        assertThat(effectivenessGK).isEqualTo(1.2);

        // Test Attacking Focus
        Double effectivenessAtt = trainingService.calculateEffectiveness(team, TrainingFocus.ATTACKING);
        // Base 1.0 + 0.3 = 1.3
        assertThat(effectivenessAtt).isEqualTo(1.3);

        // Test Unrelated Focus (e.g. Defending - should be 0 from bonus if null)
        Double effectivenessDef = trainingService.calculateEffectiveness(team, TrainingFocus.DEFENDING);
        // Base 1.0 + 0.0 = 1.0
        assertThat(effectivenessDef).isEqualTo(1.0);
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
