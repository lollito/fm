# Task: Define Quest System Entities

**Goal:** Create the database entities and enums required for the Quest System (Daily & Weekly Missions).

**Requirements:**

1.  **Create Enums**
    *   **Location:** `src/main/java/com/lollito/fm/model/`
    *   **`QuestType.java`:** `PLAY_MATCH`, `WIN_MATCH`, `TRAIN_SESSION`, `SCOUT_PLAYER`, `SIGN_PLAYER`, `CLEAN_SHEET`, `SCORE_GOALS`.
    *   **`QuestStatus.java`:** `ACTIVE`, `COMPLETED`, `CLAIMED`, `EXPIRED`.
    *   **`QuestFrequency.java`:** `DAILY`, `WEEKLY`, `SEASONAL`.

2.  **Create `Quest.java` Entity**
    *   **Location:** `src/main/java/com/lollito/fm/model/Quest.java`
    *   **Fields:**
        *   `id` (Long, GeneratedValue)
        *   `user` (ManyToOne, JoinColumn `user_id`)
        *   `type` (Enum `QuestType`)
        *   `description` (String)
        *   `targetValue` (Integer)
        *   `currentValue` (Integer, default 0)
        *   `status` (Enum `QuestStatus`, default `ACTIVE`)
        *   `frequency` (Enum `QuestFrequency`)
        *   `expirationDate` (Instant/LocalDateTime)
        *   `rewardXp` (Integer)
        *   `rewardMoney` (BigDecimal - optional, or integer for simplicity)
    *   **Annotations:** `@Entity`, `@Data`, `@Builder`.

3.  **Create `QuestRepository.java`**
    *   **Location:** `src/main/java/com/lollito/fm/repository/QuestRepository.java`
    *   **Extends:** `JpaRepository<Quest, Long>`.
    *   **Methods:**
        *   `List<Quest> findByUserIdAndStatusAndExpirationDateAfter(Long userId, QuestStatus status, Instant now);`
        *   `List<Quest> findByUserIdAndFrequency(Long userId, QuestFrequency frequency);`
        *   `void deleteByExpirationDateBefore(Instant now);` (for cleanup)

4.  **Update `User.java` (Optional but recommended)**
    *   Add `@OneToMany(mappedBy = "user")` list of quests if needed for cascading deletes, though direct repository access is often cleaner for large lists.
