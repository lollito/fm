# Task: Implement Quest Widget (Frontend)

**Goal:** Create a React component to display and claim Daily/Weekly Quests.

**Requirements:**

1.  **API Integration**
    *   **Endpoint:** `GET /api/quests` (Filter by Status: ACTIVE/COMPLETED/CLAIMED).
    *   **Endpoint:** `POST /api/quests/{id}/claim`.

2.  **UI Component: `QuestList.js`**
    *   **Location:** `fm-admin/src/components/Quest/QuestList.js`.
    *   **Display:** List of quests (Title, Description, Progress Bar `current / target`, Reward XP/Money).
    *   **Grouping:** "Daily" vs "Weekly".

3.  **Claim Interaction**
    *   **Button:** "Claim Reward" if `status === COMPLETED`.
    *   **Effect:** Call API, show success toast ("+50 XP"), refresh list (move to `CLAIMED` section or disappear).
    *   **Visual Cue:** Use confetti or similar animation on claim.

4.  **Dashboard Integration**
    *   Embed `<QuestList limit={3} />` in the main `Dashboard.js` widget area.
    *   Ensure it handles empty state ("No active quests").
