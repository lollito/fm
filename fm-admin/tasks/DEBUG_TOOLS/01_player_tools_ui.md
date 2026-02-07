# Player Tools Implementation

Implement the `PlayerTools` component in `DebugTools.js` to allow admins to modify player stats directly.

**Requirements:**

1.  **Player Search:** Add a search input to find a player by name or ID. (You might need to use `PlayerService` or an existing API to search players. If no search API exists, ask to implement one or search by ID only).
2.  **Edit Stats:** Once a player is selected, display a form to modify their attributes (e.g., stamina, morale, specific skills).
3.  **Submit Changes:** Add a "Update Stats" button that calls `POST /api/admin/debug/players/modify-stats`.
    *   Payload structure: `ModifyPlayerStatsRequest` (check backend DTO for fields).

**Files to Modify:**
*   `fm-admin/src/pages/DebugTools.js` (Implement `PlayerTools` component).
*   `fm-admin/src/services/api.js` (Ensure `modifyPlayerStats` is correctly implemented).

**Verification:**
*   Verify that you can select a player and successfully update their stats.
