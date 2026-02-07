package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.lollito.fm.model.Club;
import com.lollito.fm.model.Match;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.rest.MatchRepository;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private MatchService matchService;

    @Mock
    private Page<Match> matchPage;

    @Test
    public void loadHistory_shouldReturnPageOfMatches() {
        // Arrange
        User user = new User();
        Club club = new Club();
        club.setId(1L);
        user.setClub(club);

        when(userService.getLoggedUser()).thenReturn(user);

        Pageable pageable = PageRequest.of(0, 10);
        when(matchRepository.findByClubAndFinishOrderByDateDesc(club, pageable)).thenReturn(matchPage);

        // Act
        Page<Match> result = matchService.loadHistory(pageable);

        // Assert
        assertEquals(matchPage, result);
        verify(userService).getLoggedUser();
        verify(matchRepository).findByClubAndFinishOrderByDateDesc(club, pageable);
    }
}
