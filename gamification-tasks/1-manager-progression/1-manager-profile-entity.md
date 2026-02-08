# Task: Create ManagerProfile Entity and Perks

**Goal:** Implement the database entity to store manager progression and the Enum for Perks.

**Requirements:**

1.  **Create `ManagerPerk.java` Enum**
    *   **Location:** `src/main/java/com/lollito/fm/model/ManagerPerk.java`
    *   **Content:** Define the perks listed in `gamification.md`:
        *   **Tactical:** `VIDEO_ANALYST` (Level 1), `MOTIVATOR` (Level 5), `FORTRESS` (Level 10).
        *   **Financial:** `NEGOTIATOR` (Level 1), `MARKETING_GURU` (Level 5), `INVESTOR` (Level 10).
        *   **Scouting:** `HAWK_EYE` (Level 1), `GLOBAL_NETWORK` (Level 5), `PERSUADER` (Level 10).
    *   **Attributes:** Add fields for `name`, `description`, `requiredLevel`, `category` (Tactical, Financial, Scouting).

2.  **Create `ManagerProfile.java` Entity**
    *   **Location:** `src/main/java/com/lollito/fm/model/ManagerProfile.java`
    *   **Fields:**
        *   `id` (Long, GeneratedValue)
        *   `user` (OneToOne with `User`, JoinColumn `user_id`)
        *   `level` (Integer, default 1)
        *   `currentXp` (Long, default 0)
        *   `talentPoints` (Integer, default 0)
        *   `unlockedPerks` (Set<ManagerPerk>, ElementCollection or similar mapping if using Enum, or specialized OneToMany if using Entity - prefer Enum for simplicity unless complexity requires Entity). *Correction:* Since it's an Enum, use `@ElementCollection` and `@Enumerated(EnumType.STRING)`.
    *   **Annotations:** `@Entity`, `@Data` (Lombok), `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.

3.  **Create `ManagerProfileRepository.java`**
    *   **Location:** `src/main/java/com/lollito/fm/repository/ManagerProfileRepository.java`
    *   **Extends:** `JpaRepository<ManagerProfile, Long>`.
    *   **Method:** `Optional<ManagerProfile> findByUserId(Long userId);`

4.  **Update `User.java`**
    *   **Location:** `src/main/java/com/lollito/fm/model/User.java`
    *   **Changes:** Add `@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)` relationship to `ManagerProfile`.

5.  **Run Tests/Verification**
    *   Ensure the application context loads.
    *   Create a simple test to verify Entity persistence.
