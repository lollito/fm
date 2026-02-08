package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.Quest;
import com.lollito.fm.model.QuestStatus;
import com.lollito.fm.model.QuestType;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.QuestRepository;
import com.lollito.fm.repository.rest.UserRepository;

@ExtendWith(MockitoExtension.class)
public class QuestServiceTest {

    @Mock
    private QuestRepository questRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ManagerProgressionService managerProgressionService;

    @Mock
    private FinancialService financialService;

    @InjectMocks
    private QuestService questService;

    @Test
    void testGenerateDailyQuests() {
        User user = new User();
        user.setId(1L);

        questService.generateDailyQuests(user);

        verify(questRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testClaimReward() {
        User user = new User();
        user.setId(1L);
        Quest quest = new Quest();
        quest.setId(10L);
        quest.setUser(user);
        quest.setStatus(QuestStatus.COMPLETED);
        quest.setRewardXp(100);

        when(questRepository.findById(10L)).thenReturn(Optional.of(quest));

        questService.claimReward(10L);

        verify(managerProgressionService, times(1)).addXp(user, 100);
        verify(questRepository, times(1)).save(quest);
        assertEquals(QuestStatus.CLAIMED, quest.getStatus());
    }

    @Test
    void testIncrementProgress() {
        User user = new User();
        user.setId(1L);
        Quest quest = new Quest();
        quest.setType(QuestType.PLAY_MATCH);
        quest.setCurrentValue(0);
        quest.setTargetValue(5);
        quest.setStatus(QuestStatus.ACTIVE);

        when(questRepository.findByUserIdAndStatusAndExpirationDateAfter(any(), any(), any())).thenReturn(Collections.singletonList(quest));

        questService.incrementProgress(user, QuestType.PLAY_MATCH, 1);

        verify(questRepository, times(1)).save(quest);
        assertEquals(1, quest.getCurrentValue());
    }

    @Test
    void testCheckCompletion() {
        Quest quest = new Quest();
        quest.setTargetValue(5);
        quest.setCurrentValue(5);
        quest.setStatus(QuestStatus.ACTIVE);

        questService.checkCompletion(quest);

        assertEquals(QuestStatus.COMPLETED, quest.getStatus());
        verify(questRepository, times(1)).save(quest);
    }
}
