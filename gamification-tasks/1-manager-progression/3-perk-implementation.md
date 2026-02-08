# Task: Apply Manager Perks in Game Services

**Goal:** Modify existing services (Scouting, Training, Match, Finance) to check for and apply unlocked Manager Perks.

**Requirements:**

1.  **Inject `ManagerProgressionService`** into the following services:
    *   `ScoutingService`
    *   `TrainingService`
    *   `SimulationMatchService`
    *   `FinanceService`
    *   `TransferService` (or `PlayerService` for negotiation logic)

2.  **Scouting Service Modifications**
    *   **File:** `src/main/java/com/lollito/fm/service/ScoutingService.java`
    *   **Perk:** `ManagerPerk.HAWK_EYE` (Scouting Speed)
    *   **Logic:** In `assignScout`, if user has `HAWK_EYE`, reduce `scoutingDuration` by 10%.
    *   **Perk:** `ManagerPerk.GLOBAL_NETWORK` (Wonderkids)
    *   **Logic:** In `generatePlayer` (or relevant generation logic), increase chance of high potential if user has perk.

3.  **Training Service Modifications**
    *   **File:** `src/main/java/com/lollito/fm/service/TrainingService.java`
    *   **Perk:** `ManagerPerk.VIDEO_ANALYST` (Tactical Learning)
    *   **Logic:** In `calculateTrainingPerformance`, boost `tactical` attribute gain by 5%.

4.  **Match Service Modifications**
    *   **File:** `src/main/java/com/lollito/fm/service/SimulationMatchService.java`
    *   **Perk:** `ManagerPerk.FORTRESS` (Home Advantage)
    *   **Logic:** In `playMatch`, if home team manager has `FORTRESS`, increase `homeAdvantage` factor (currently 0-10) by 2.
    *   **Perk:** `ManagerPerk.MOTIVATOR` (Morale Recovery)
    *   **Logic:** In post-match processing, if team lost but manager has `MOTIVATOR`, apply smaller morale penalty.

5.  **Finance Service Modifications**
    *   **File:** `src/main/java/com/lollito/fm/service/FinanceService.java`
    *   **Perk:** `ManagerPerk.INVESTOR` (Bank Interest)
    *   **Logic:** Add `@Scheduled` task to calculate weekly interest on balance if positive and manager has perk.
    *   **Perk:** `ManagerPerk.MARKETING_GURU` (Sponsor Income)
    *   **Logic:** In `processSponsorshipPayment`, increase amount by 5%.

6.  **Transfer/Negotiation Modifications**
    *   **File:** `src/main/java/com/lollito/fm/service/TransferService.java`
    *   **Perk:** `ManagerPerk.NEGOTIATOR` (Salary Demand)
    *   **Logic:** During contract negotiation logic, reduce player wage demands by 5%.

**Note:** Use dependency injection carefully to avoid circular dependencies. If needed, use `ApplicationEventPublisher` or lazy loading.
