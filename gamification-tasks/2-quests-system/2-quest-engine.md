# Task: Implement Quest Engine and Scheduling

**Goal:** Logic for generating daily/weekly quests and scheduled tasks.

**Requirements:**

1.  **Create `QuestService.java`**
    *   **Location:** `src/main/java/com/lollito/fm/service/QuestService.java`
    *   **Injections:** `QuestRepository`, `UserRepository`, `ManagerProgressionService`, `FinanceService`.

2.  **Core Logic:**
    *   `void generateDailyQuests(User user)`:
        *   Clear existing expired or completed daily quests (optional, or rely on expiration cleanup).
        *   Generate 3 random quests (e.g., `QuestType.PLAY_MATCH` target 3, `QuestType.TRAIN_SESSION` target 1).
        *   Set `expirationDate` to end of day.
        *   Save quests.
    *   `void generateWeeklyQuests(User user)`:
        *   Generate 3 random quests (harder goals).
        *   Set `expirationDate` to end of week.
    *   `void claimReward(Long questId)`:
        *   Find quest by ID.
        *   Verify status is `COMPLETED` (or ready to claim).
        *   Update status to `CLAIMED`.
        *   Call `managerProgressionService.addXp(user, quest.getRewardXp())`.
        *   Call `financeService.addIncome(user.getClub(), quest.getRewardMoney(), TransactionType.QUEST_REWARD)`.
    *   `void checkCompletion(Quest quest)`:
        *   If `currentValue >= targetValue`, set status to `COMPLETED`.
        *   Save quest.
    *   `void incrementProgress(User user, QuestType type, int amount)`:
        *   Find active quests for user with matching type.
        *   Increment `currentValue` by amount.
        *   Call `checkCompletion(quest)`.

3.  **Scheduled Tasks:**
    *   **Method:** `runDailyQuestGeneration()`
    *   **Annotation:** `@Scheduled(cron = "0 0 0 * * ?")` (Midnight daily).
    *   **Logic:**
        *   Iterate active users (optimally in batches).
        *   Call `generateDailyQuests` for each.
        *   (Optional) If current day is Monday, call `generateWeeklyQuests`.

4.  **Integration:**
    *   Ensure `FinanceService` and `ManagerProgressionService` are called transactionally.
