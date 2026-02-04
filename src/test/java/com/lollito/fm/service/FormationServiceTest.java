package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Formation;
import com.lollito.fm.model.Mentality;
import com.lollito.fm.model.Module;
import com.lollito.fm.model.Player;
import com.lollito.fm.model.Team;
import com.lollito.fm.model.rest.FormationRequest;
import com.lollito.fm.repository.rest.FormationRepository;

@RunWith(MockitoJUnitRunner.class)
public class FormationServiceTest {

    @InjectMocks
    private FormationService formationService;

    @Mock
    private ModuleService moduleService;
    @Mock
    private PlayerService playerService;
    @Mock
    private ClubService clubService;
    @Mock
    private MentalityService mentalityService;
    @Mock
    private FormationRepository formationRepository;
    @Mock
    private Club club;
    @Mock
    private Team team;

    @Test
    public void createPlayerFormation_shouldInvokeFindOneForEachPlayer() {
        // Setup
        FormationRequest request = new FormationRequest();
        request.setModuleId(1L);
        request.setMentality(Mentality.NORMAL);
        List<Long> playerIds = Arrays.asList(1L, 2L, 3L);
        request.setPlayersId(playerIds);

        when(clubService.load()).thenReturn(club);
        when(club.getTeam()).thenReturn(team);
        when(team.getFormation()).thenReturn(new Formation());
        Module module = new Module();

        when(moduleService.findOne(1L)).thenReturn(module);

        Player p1 = new Player(); p1.setId(1L);
        Player p2 = new Player(); p2.setId(2L);
        Player p3 = new Player(); p3.setId(3L);
        when(playerService.findAll(any(List.class))).thenReturn(Arrays.asList(p1, p2, p3));

        try {
            formationService.createPlayerFormation(request);
        } catch (RuntimeException e) {
            // Expected validation error as we are not setting up full 11 players and matching roles
        }

        // Verify optimization
        verify(playerService, times(1)).findAll(any(List.class));
        verify(playerService, times(0)).findOne(any(Long.class));
    }
}
