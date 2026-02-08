# Task: Implement Manager Progression Service

**Goal:** Implement the business logic for calculating Manager XP, leveling up, and managing perks.

**Requirements:**

1.  **Create `ManagerProgressionService.java`**
    *   **Location:** `src/main/java/com/lollito/fm/service/ManagerProgressionService.java`
    *   **Injections:** `ManagerProfileRepository`, `UserRepository`.

2.  **Core Methods:**
    *   `ManagerProfile getProfile(User user)`: Retrieve or create a profile for the user.
    *   `void addXp(User user, long amount)`:
        *   Add XP to `currentXp`.
        *   Check for level up using a formula (e.g., `XP >= Level * 1000`).
        *   On level up: increment `level`, reset XP or carry over, add `talentPoints`.
        *   Log the event (or publish `LevelUpEvent` if using event system).
    *   `void unlockPerk(User user, ManagerPerk perk)`:
        *   Check if user has enough `talentPoints`.
        *   Check if user meets `perk.requiredLevel`.
        *   Deduct points, add perk to `unlockedPerks`.
        *   Save profile.
    *   `boolean hasPerk(User user, ManagerPerk perk)`: Helper to check if a user has a specific perk active.

3.  **XP Logic:**
    *   Define constants for XP rewards (Match Win: 100, Draw: 50, Loss: 10, etc.) as `public static final`.
    *   Implement level-up logic: `while (currentXp >= xpForNextLevel(level)) { level++; talentPoints++; currentXp -= xpForNextLevel; }`.

4.  **Integration:**
    *   Ensure thread safety if XP can be added concurrently (e.g., synchronized or atomic updates, though simple transaction management `@Transactional` usually suffices).

5.  **Testing:**
    *   Unit test `addXp` ensuring level ups occur correctly.
    *   Unit test `unlockPerk` ensuring points are deducted and requirements checked.
