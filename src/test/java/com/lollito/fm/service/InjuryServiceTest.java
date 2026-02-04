package com.lollito.fm.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.lollito.fm.model.Injury;
import com.lollito.fm.model.InjuryContext;
import com.lollito.fm.model.InjuryStatus;
import com.lollito.fm.model.Player;
import com.lollito.fm.repository.rest.InjuryRepository;
import com.lollito.fm.repository.rest.PlayerRepository;

@ExtendWith(MockitoExtension.class)
class InjuryServiceTest {

    @Mock
    private InjuryRepository injuryRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private InjuryService injuryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(injuryService, "baseProbability", 0.02);
        ReflectionTestUtils.setField(injuryService, "ageThreshold", 30);
        ReflectionTestUtils.setField(injuryService, "conditionThreshold", 50);
    }

    @Test
    void testCheckForInjury() {
        Player youngPlayer = Player.builder().birth(LocalDate.now().minusYears(22)).condition(100.0).injuries(List.of()).build();

        boolean result = injuryService.checkForInjury(youngPlayer, 1.0);
        // Just ensure it runs
    }

    @Test
    void testCreateInjury() {
        Player player = new Player();
        player.setId(1L);

        when(injuryRepository.save(any(Injury.class))).thenAnswer(i -> i.getArguments()[0]);

        Injury injury = injuryService.createInjury(player, InjuryContext.MATCH);

        assertThat(injury.getPlayer()).isEqualTo(player);
        assertThat(injury.getStatus()).isEqualTo(InjuryStatus.ACTIVE);
        assertThat(injury.getExpectedRecoveryDate()).isAfterOrEqualTo(LocalDate.now());
    }

    @Test
    void testProcessInjuryRecovery() {
        Injury injury = Injury.builder()
            .status(InjuryStatus.ACTIVE)
            .expectedRecoveryDate(LocalDate.now().minusDays(1))
            .player(new Player())
            .build();

        when(injuryRepository.findByStatus(InjuryStatus.ACTIVE)).thenReturn(List.of(injury));

        injuryService.processInjuryRecovery();

        verify(injuryRepository).findByStatus(InjuryStatus.ACTIVE);
    }
}
