# FM Admin Enhancement Checklist

## Phase 1: Independent Tasks (Can run in parallel)

### Server Management UI
Prompts:
- Update `fm-admin/src/pages/ServerManagement.js` to include a "Force Next Day" button that calls `POST /api/server/next`.
- Add a "Delete Server" button to `fm-admin/src/pages/ServerManagement.js` that calls `DELETE /api/server/?serverId={id}`.
- Ensure `fm-admin/src/services/api.js` has the necessary functions (`forceNextDay`, `deleteServer`).

### Match Intervention Backend
Prompts:
- In `src/main/java/com/lollito/fm/service/LiveMatchService.java`, implement `forceFinish(Long matchId)` to set match status to FINISHED and save results.
- In `src/main/java/com/lollito/fm/service/LiveMatchService.java`, implement `reset(Long matchId)` to reset match time/score and set status to SCHEDULED.
- In `src/main/java/com/lollito/fm/controller/LiveMatchController.java`, expose `POST /api/live-match/{id}/finish` and `POST /api/live-match/{id}/reset` calling the service methods.

### Debug Tools - Player Editor
Prompts:
- Implement the `PlayerTools` component in `fm-admin/src/pages/DebugTools.js`.
- Add a search input to find players (by ID if search API is missing).
- Display player stats and allow editing.
- Add "Update Stats" button calling `POST /api/admin/debug/players/modify-stats` via `fm-admin/src/services/api.js`.

### Debug Tools - Financial Editor
Prompts:
- Implement the `FinancialTools` component in `fm-admin/src/pages/DebugTools.js`.
- Add a club selector (or input for ID).
- Add an input for amount to add/remove.
- Add "Process Transaction" button calling `POST /api/admin/debug/finances/adjust` via `fm-admin/src/services/api.js`.

## Phase 2: Dependent Tasks

### Match Intervention UI (Requires Backend)
Prompts:
- Update `fm-admin/src/pages/LiveMatchMonitoring.js` to add an "Actions" column to the match table.
- Add "Force Finish" button calling `POST /api/live-match/{id}/finish`.
- Add "Reset" button calling `POST /api/live-match/{id}/reset`.
- Ensure `fm-admin/src/services/api.js` includes `forceFinishMatch` and `resetMatch`.
