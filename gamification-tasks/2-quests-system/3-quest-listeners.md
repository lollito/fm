# Task: Implement Quest Event Listeners

**Goal:** Integrate the Quest System with existing game events using Spring Event Listeners.

**Requirements:**

1.  **Define Event Classes** (if not already existing)
    *   **Location:** `src/main/java/com/lollito/fm/event/`
    *   `MatchFinishedEvent`: `Match`, `Result`
    *   `TrainingCompletedEvent`: `TrainingSession`
    *   `ScoutingCompletedEvent`: `Scout`, `Target`
    *   `TransferCompletedEvent`: `Transfer`, `Amount`

2.  **Create `QuestEventListener.java`**
    *   **Location:** `src/main/java/com/lollito/fm/listener/QuestEventListener.java`
    *   **Injections:** `QuestService`
    *   **Annotation:** `@Component`, `@Slf4j`.

3.  **Implement Listeners:**
    *   `@EventListener` on `MatchFinishedEvent`:
        *   Call `questService.incrementProgress(user, QuestType.PLAY_MATCH, 1)`.
        *   If result == WIN, call `questService.incrementProgress(user, QuestType.WIN_MATCH, 1)`.
        *   If clean sheet, call `questService.incrementProgress(user, QuestType.CLEAN_SHEET, 1)`.
    *   `@EventListener` on `TrainingCompletedEvent`:
        *   Call `questService.incrementProgress(user, QuestType.TRAIN_SESSION, 1)`.
    *   `@EventListener` on `ScoutingCompletedEvent`:
        *   Call `questService.incrementProgress(user, QuestType.SCOUT_PLAYER, 1)`.
    *   `@EventListener` on `TransferCompletedEvent`:
        *   Call `questService.incrementProgress(user, QuestType.SIGN_PLAYER, 1)`.

4.  **Note on Async:**
    *   Consider making listeners `@Async` if processing is heavy, but simple increments are fast enough to run synchronously within the event thread.

5.  **Testing:**
    *   Unit test that firing a `MatchFinishedEvent` correctly triggers the `QuestService` mock.
