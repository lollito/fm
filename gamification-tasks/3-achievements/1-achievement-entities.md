# Task: Define Achievement Entities

**Goal:** implement the database structure for tracking user achievements.

**Requirements:**

1.  **Create `AchievementType.java` Enum**
    *   **Location:** `src/main/java/com/lollito/fm/model/AchievementType.java`
    *   **Values:**
        *   `WIN_LEAGUE`, `WIN_CUP`, `PROMOTION`
        *   `TYCOON` (100M Balance), `SCROOGE` (Zero Spending)
        *   `GOLEADOR` (Win 5-0), `COMEBACK_KING` (Win from 0-2)
        *   `ACADEMY_HERO` (Youth player scores), `TRADER` (Profit on sale)
    *   **Attributes:** `name`, `description`, `xpReward` (int).

2.  **Create `UserAchievement.java` Entity**
    *   **Location:** `src/main/java/com/lollito/fm/model/UserAchievement.java`
    *   **Fields:**
        *   `id` (Long, GeneratedValue)
        *   `user` (ManyToOne, JoinColumn `user_id`)
        *   `achievement` (Enumerated EnumType.STRING `AchievementType`)
        *   `unlockedAt` (Instant, default `Instant.now()`)
    *   **Annotations:** `@Entity`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
    *   **Constraints:** Unique constraint on `user_id` + `achievement` to prevent duplicate unlocks.

3.  **Create `UserAchievementRepository.java`**
    *   **Location:** `src/main/java/com/lollito/fm/repository/UserAchievementRepository.java`
    *   **Extends:** `JpaRepository<UserAchievement, Long>`.
    *   **Methods:**
        *   `boolean existsByUserIdAndAchievement(Long userId, AchievementType achievement);`
        *   `List<UserAchievement> findByUserId(Long userId);`

4.  **Update `User.java` (Optional)**
    *   Add `@OneToMany(mappedBy = "user")` list of achievements.
