# Individual Training Focus

## User Story
As a user, I want to assign individual training focuses to players (e.g., "Improve Passing") so they can develop specific attributes.

## Tasks
1.  **Add `individualFocus` to `Player` or create `PlayerTrainingFocus` entity:**
    *   Fields: `playerId`, `focus` (enum: PASSING, DEFENDING, STAMINA, etc.), `intensity` (LOW, MEDIUM, HIGH), `startDate`, `endDate`.
    *   Currently, only team-wide focus exists.
2.  **Update `TrainingService.processDailyTraining`:**
    *   Iterate through players and check for individual focuses.
    *   If an individual focus exists, add extra improvement to that stat but reduce overall condition more (increased workload).
3.  **Add `TrainingController` endpoints:**
    *   `POST /api/training/focus/{playerId}`: Set individual focus.
    *   `DELETE /api/training/focus/{playerId}`: Remove focus.
    *   `GET /api/training/focus`: Get all focuses for the team.
4.  **Add logic to prevent conflicting focuses:** (e.g., can't train "Rest" and "Heavy Passing").
