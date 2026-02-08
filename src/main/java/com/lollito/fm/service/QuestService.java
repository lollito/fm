package com.lollito.fm.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.Quest;
import com.lollito.fm.model.QuestFrequency;
import com.lollito.fm.model.QuestStatus;
import com.lollito.fm.model.QuestType;
import com.lollito.fm.model.TransactionCategory;
import com.lollito.fm.model.User;
import com.lollito.fm.repository.QuestRepository;
import com.lollito.fm.repository.rest.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QuestService {

    @Autowired
    private QuestRepository questRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ManagerProgressionService managerProgressionService;

    @Autowired
    private FinancialService financialService;

    @Autowired
    @Lazy
    private QuestService self;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public void generateDailyQuests(User user) {
        List<Quest> dailyQuests = new ArrayList<>();
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        for (int i = 0; i < 3; i++) {
            QuestType type = getRandomQuestType();
            int target = generateTarget(type, false);
            Quest quest = Quest.builder()
                    .user(user)
                    .type(type)
                    .description(generateDescription(type, target))
                    .targetValue(target)
                    .currentValue(0)
                    .status(QuestStatus.ACTIVE)
                    .frequency(QuestFrequency.DAILY)
                    .expirationDate(endOfDay)
                    .rewardXp(generateRewardXp(false))
                    .rewardMoney(generateRewardMoney(false))
                    .build();
            dailyQuests.add(quest);
        }
        questRepository.saveAll(dailyQuests);
    }

    @Transactional
    public void generateWeeklyQuests(User user) {
        List<Quest> weeklyQuests = new ArrayList<>();
        LocalDateTime endOfWeek = LocalDateTime.now().plusDays(6).with(LocalTime.MAX);

        for (int i = 0; i < 3; i++) {
            QuestType type = getRandomQuestType();
            int target = generateTarget(type, true);
            Quest quest = Quest.builder()
                    .user(user)
                    .type(type)
                    .description(generateDescription(type, target))
                    .targetValue(target)
                    .currentValue(0)
                    .status(QuestStatus.ACTIVE)
                    .frequency(QuestFrequency.WEEKLY)
                    .expirationDate(endOfWeek)
                    .rewardXp(generateRewardXp(true))
                    .rewardMoney(generateRewardMoney(true))
                    .build();
            weeklyQuests.add(quest);
        }
        questRepository.saveAll(weeklyQuests);
    }

    @Transactional
    public void claimReward(Long questId) {
        Quest quest = questRepository.findById(questId).orElse(null);
        if (quest == null) return;

        if (quest.getStatus() == QuestStatus.COMPLETED) {
            quest.setStatus(QuestStatus.CLAIMED);
            questRepository.save(quest);

            managerProgressionService.addXp(quest.getUser(), quest.getRewardXp());
            if (quest.getUser().getClub() != null) {
                financialService.addIncome(quest.getUser().getClub(), quest.getRewardMoney(), TransactionCategory.QUEST_REWARD);
            }
            notificationService.createNotification(quest.getUser(), com.lollito.fm.model.NotificationType.QUEST_COMPLETED, "Quest Reward Claimed", "You claimed reward for: " + quest.getDescription(), com.lollito.fm.model.NotificationPriority.LOW);
        }
    }

    @Transactional
    public void checkCompletion(Quest quest) {
        if (quest.getStatus() == QuestStatus.ACTIVE && quest.getCurrentValue() >= quest.getTargetValue()) {
            quest.setStatus(QuestStatus.COMPLETED);
            questRepository.save(quest);
            notificationService.createNotification(quest.getUser(), com.lollito.fm.model.NotificationType.QUEST_COMPLETED, "Quest Completed", "You completed: " + quest.getDescription(), com.lollito.fm.model.NotificationPriority.MEDIUM);
        }
    }

    @Transactional
    public void incrementProgress(User user, QuestType type, int amount) {
        List<Quest> quests = questRepository.findByUserIdAndStatusAndExpirationDateAfter(user.getId(), QuestStatus.ACTIVE, LocalDateTime.now());
        for (Quest quest : quests) {
            if (quest.getType() == type) {
                quest.setCurrentValue(quest.getCurrentValue() + amount);
                checkCompletion(quest);
                if (quest.getStatus() == QuestStatus.ACTIVE) {
                    questRepository.save(quest);
                }
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void runDailyQuestGeneration() {
        log.info("Running daily quest generation");

        // Cleanup expired - this needs to be transactional too.
        // Calling self reference method or moving to repository call directly if it's transactional (repositories are usually transactional by default).
        // deleteByExpirationDateBefore is a derived delete query, usually requires @Transactional.
        // I'll wrap it in a helper method or call it via self if public.
        // It is not exposed in service. I'll expose a cleanup method.
        self.cleanupExpiredQuests();

        Pageable pageable = PageRequest.of(0, 50, Sort.by("id"));
        Page<User> page;

        do {
            page = userRepository.findAll(pageable);
            for (User user : page.getContent()) {
                if (Boolean.TRUE.equals(user.getIsActive())) {
                     self.generateDailyQuests(user);
                     if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.MONDAY) {
                         self.generateWeeklyQuests(user);
                     }
                }
            }
            pageable = page.nextPageable();
        } while (page.hasNext());
    }

    @Transactional
    public void cleanupExpiredQuests() {
        questRepository.deleteByExpirationDateBefore(LocalDateTime.now());
    }

    // Helpers
    private QuestType getRandomQuestType() {
        QuestType[] types = QuestType.values();
        return types[ThreadLocalRandom.current().nextInt(types.length)];
    }

    private int generateTarget(QuestType type, boolean isWeekly) {
        int multiplier = isWeekly ? 5 : 1;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        switch (type) {
            case PLAY_MATCH: return (1 + random.nextInt(3)) * multiplier;
            case WIN_MATCH: return (1 + random.nextInt(2)) * multiplier;
            case TRAIN_SESSION: return (1 + random.nextInt(2)) * multiplier;
            case SCOUT_PLAYER: return (1 + random.nextInt(3)) * multiplier;
            case SIGN_PLAYER: return (1) * multiplier;
            case CLEAN_SHEET: return (1) * multiplier;
            case SCORE_GOALS: return (2 + random.nextInt(4)) * multiplier;
            default: return 1 * multiplier;
        }
    }

    private String generateDescription(QuestType type, int target) {
        String typeName = type.name().replace("_", " ").toLowerCase();
        typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
        return typeName + ": " + target;
    }

    private Integer generateRewardXp(boolean isWeekly) {
        int base = isWeekly ? 500 : 100;
        return base + ThreadLocalRandom.current().nextInt(base);
    }

    private BigDecimal generateRewardMoney(boolean isWeekly) {
        int base = isWeekly ? 50000 : 10000;
        return BigDecimal.valueOf(base + ThreadLocalRandom.current().nextInt(base));
    }
}
