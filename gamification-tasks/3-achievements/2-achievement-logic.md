# Task: Implement Achievement Logic

**Goal:** Logic to check conditions and award achievements based on specific events.

**Requirements:**

1.  **Create `AchievementService.java`**
    *   **Location:** `src/main/java/com/lollito/fm/service/AchievementService.java`
    *   **Injections:** `UserAchievementRepository`, `ManagerProgressionService`, `FinanceService`.

2.  **Core Methods:**
    *   `void checkAndUnlock(User user, AchievementType achievement)`:
        *   If `repository.existsByUserIdAndAchievement(user.getId(), achievement)`, return.
        *   Create `UserAchievement` (unlockedAt = now).
        *   Save entity.
        *   Call `managerProgressionService.addXp(user, achievement.getXpReward())`.
        *   Log event (or publish `AchievementUnlockedEvent`).

3.  **Event Handling Logic:**
    *   **On Match Win (5-0):** Check `match.getScoreHome() >= 5` -> `checkAndUnlock(user, GOLEADOR)`.
    *   **On Comeback (0-2 down to Win):** Check logic -> `checkAndUnlock(user, COMEBACK_KING)`.
    *   **On Promotion:** `checkAndUnlock(user, PROMOTION)`.
    *   **On League Win:** `checkAndUnlock(user, WIN_LEAGUE)`.
    *   **On Financial Update:** If `balance > 100_000_000` -> `checkAndUnlock(user, TYCOON)`.

4.  **Integration:**
    *   Hook this service into `SimulationMatchService`, `SeasonService` (end of season), and `FinanceService`.
    *   Use Spring Events if possible (e.g., listen for `MatchFinishedEvent` and check score).

5.  **Testing:**
    *   Unit test `checkAndUnlock` ensures duplicates are not created.
    *   Unit test condition logic (e.g., Goleador condition).
