# Match Intervention Backend Implementation

The admin needs to be able to intervene in live matches that are stuck or require manual correction.

**Requirements:**

1.  **Force Finish Match:**
    *   Implement a method `forceFinish(Long matchId)` in `LiveMatchService`.
    *   This method should immediately set the match status to `FINISHED` and perform necessary post-match processing (saving results, updating rankings).
    *   Expose this via a new endpoint `POST /api/live-match/{id}/finish` in `LiveMatchController`.

2.  **Reset Match:**
    *   Implement a method `reset(Long matchId)` in `LiveMatchService`.
    *   This method should reset the match time to 00:00, score to 0-0, and status to `SCHEDULED` or `IN_PROGRESS` (depending on desired behavior, likely `SCHEDULED` so it can be picked up again).
    *   Expose this via a new endpoint `POST /api/live-match/{id}/reset` in `LiveMatchController`.

**Files to Modify:**
*   `src/main/java/com/lollito/fm/service/LiveMatchService.java`
*   `src/main/java/com/lollito/fm/controller/LiveMatchController.java`

**Verification:**
*   Verify the endpoints exist and work correctly (e.g., using `curl` or Postman).
