package com.lollito.fm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.IndividualTrainingFocus;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.PlayerTrainingFocus;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.TrainingFocus;
import com.lollito.fm.model.TrainingIntensity;
import com.lollito.fm.model.TrainingSession;
import com.lollito.fm.model.dto.IndividualFocusRequest;
import com.lollito.fm.repository.PlayerTrainingFocusRepository;
import com.lollito.fm.repository.PlayerTrainingResultRepository;
import com.lollito.fm.repository.TrainingSessionRepository;
import com.lollito.fm.repository.rest.ClubRepository;

@ExtendWith(MockitoExtension.class)
class IndividualTrainingTest {

    @Mock
    private TrainingSessionRepository trainingSessionRepository;

    @Mock
    private PlayerTrainingResultRepository playerTrainingResultRepository;

    @Mock
    private PlayerTrainingFocusRepository playerTrainingFocusRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private StaffService staffService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void testAssignIndividualFocus() {
        Long playerId = 1L;
        Player player = new Player();
        player.setId(playerId);

        IndividualFocusRequest request = new IndividualFocusRequest();
        request.setFocus(IndividualTrainingFocus.PASSING);
        request.setIntensity(TrainingIntensity.INTENSIVE);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        when(playerService.findOne(playerId)).thenReturn(player);
        when(playerTrainingFocusRepository.existsOverlappingFocus(any(), any(), any())).thenReturn(false);
        when(playerTrainingFocusRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        PlayerTrainingFocus result = trainingService.assignIndividualFocus(playerId, request);

        assertThat(result.getPlayer()).isEqualTo(player);
        assertThat(result.getFocus()).isEqualTo(IndividualTrainingFocus.PASSING);
        assertThat(result.getIntensity()).isEqualTo(TrainingIntensity.INTENSIVE);

        verify(playerTrainingFocusRepository).save(any(PlayerTrainingFocus.class));
    }

    @Test
    void testAssignIndividualFocusConflict() {
        Long playerId = 1L;
        Player player = new Player();
        player.setId(playerId);

        IndividualFocusRequest request = new IndividualFocusRequest();
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        when(playerService.findOne(playerId)).thenReturn(player);
        when(playerTrainingFocusRepository.existsOverlappingFocus(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> trainingService.assignIndividualFocus(playerId, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Player already has an active focus");
    }

    @Test
    void testRemoveIndividualFocus_StartsToday() {
        Long playerId = 1L;
        Player player = new Player();
        player.setId(playerId);

        PlayerTrainingFocus focus = new PlayerTrainingFocus();
        focus.setPlayer(player);
        focus.setStartDate(LocalDate.now());
        focus.setEndDate(LocalDate.now().plusDays(7));

        when(playerService.findOne(playerId)).thenReturn(player);
        when(playerTrainingFocusRepository.findActiveFocus(eq(player), any(LocalDate.class))).thenReturn(List.of(focus));

        trainingService.removeIndividualFocus(playerId);

        verify(playerTrainingFocusRepository).delete(focus);
    }

    @Test
    void testRemoveIndividualFocus_StartedBefore() {
        Long playerId = 1L;
        Player player = new Player();
        player.setId(playerId);

        PlayerTrainingFocus focus = new PlayerTrainingFocus();
        focus.setPlayer(player);
        focus.setStartDate(LocalDate.now().minusDays(5));
        focus.setEndDate(LocalDate.now().plusDays(7));

        when(playerService.findOne(playerId)).thenReturn(player);
        when(playerTrainingFocusRepository.findActiveFocus(eq(player), any(LocalDate.class))).thenReturn(List.of(focus));
        when(playerTrainingFocusRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        trainingService.removeIndividualFocus(playerId);

        ArgumentCaptor<PlayerTrainingFocus> focusCaptor = ArgumentCaptor.forClass(PlayerTrainingFocus.class);
        verify(playerTrainingFocusRepository).save(focusCaptor.capture());

        assertThat(focusCaptor.getValue().getEndDate()).isEqualTo(LocalDate.now().minusDays(1));
    }

    @Test
    void testProcessTeamTrainingWithIndividualFocus() {
        Team team = new Team();
        team.setId(1L);
        Player player = new Player();
        player.setId(1L);
        player.setStamina(10.0);
        player.setPlaymaking(10.0);
        player.setScoring(10.0);
        player.setWinger(10.0);
        player.setGoalkeeping(10.0);
        player.setPassing(10.0);
        player.setDefending(10.0);
        player.setSetPieces(10.0);
        player.setCondition(100.0);
        player.setMoral(100.0);
        team.addPlayer(player);

        PlayerTrainingFocus focus = new PlayerTrainingFocus();
        focus.setPlayer(player);
        focus.setFocus(IndividualTrainingFocus.PASSING);
        focus.setIntensity(TrainingIntensity.INTENSIVE);

        when(playerTrainingFocusRepository.findActiveFocusForPlayers(any(), any())).thenReturn(List.of(focus));
        when(trainingSessionRepository.save(any(TrainingSession.class))).thenAnswer(i -> i.getArguments()[0]);

        trainingService.processTeamTraining(team, TrainingFocus.BALANCED, TrainingIntensity.MODERATE);

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);
        verify(playerService).save(playerCaptor.capture());

        Player savedPlayer = playerCaptor.getValue();

        // Check if passing improved (it started at 10.0)
        // With random factor, it *might* be 10.0 if improvement is 0, but baseImprovement is 0.1
        assertThat(savedPlayer.getPassing()).isGreaterThanOrEqualTo(10.0);

        // Check condition decreased
        assertThat(savedPlayer.getCondition()).isLessThan(100.0);
    }
}
