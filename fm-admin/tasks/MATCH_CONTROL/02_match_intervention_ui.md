# Match Intervention UI

The admin needs a UI to trigger the intervention actions created in the backend.

**Requirements:**

1.  **Action Buttons:** In the `LiveMatchMonitoring.js` table, add a new column "Actions".
2.  **Force Finish:** Add a "Force Finish" button for each live match.
    *   Calls `POST /api/live-match/{id}/finish`.
    *   Requires confirmation.
3.  **Reset:** Add a "Reset" button for each live match.
    *   Calls `POST /api/live-match/{id}/reset`.
    *   Requires confirmation.

**Files to Modify:**
*   `fm-admin/src/pages/LiveMatchMonitoring.js`
*   `fm-admin/src/services/api.js` (Add `forceFinishMatch` and `resetMatch` functions).

**Prerequisites:**
*   The backend endpoints must be implemented first (Task `01_match_intervention_backend.md`).

**Verification:**
*   Ensure buttons appear for live matches.
*   Ensure clicking them triggers the correct API calls and refreshes the list.
