# Task: Implement Manager Dashboard (Frontend)

**Goal:** Create a React component to display Manager Profile, XP, Level, and the Perk Talent Tree.

**Requirements:**

1.  **API Integration**
    *   **Endpoint:** `GET /api/manager/profile` (Create if needed in Backend).
    *   **Endpoint:** `POST /api/manager/unlock-perk` (Body: `{ perkId: "VIDEO_ANALYST" }`).

2.  **UI Component: `ManagerProfile.js`**
    *   **Location:** `fm-admin/src/pages/ManagerProfile.js` (or `components/Manager/`).
    *   **Header:** Display Level (e.g., "Level 5"), XP Progress Bar (`current / needed`), Talent Points Available.
    *   **Tabs:** "Overview", "Talents", "Achievements".

3.  **Talent Tree UI**
    *   Display 3 columns: Tactical, Financial, Scouting.
    *   List perks in order (Level 1 -> 5 -> 10).
    *   **Visual States:**
        *   **Locked:** Greyed out.
        *   **Unlockable:** Highlighted if `points > 0` and `level >= required`.
        *   **Unlocked:** Green/Gold border.
    *   **Action:** Click on "Unlockable" perk calls API to unlock.

4.  **Integration**
    *   Add route `/manager` in `App.js`.
    *   Add link in Sidebar.
