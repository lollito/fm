# Improve Scouting Reports

## User Story
As a user, I want detailed and accurate scouting reports that reflect a player's true ability based on the scout's skill, rather than generic text.

## Tasks
1.  **Refactor `ScoutingService.generatePlayerAssessment`:**
    *   Currently uses hardcoded strings.
    *   Implement logic to analyze player stats (e.g., if Passing < 10, "Needs to improve distribution").
    *   Consider scout's attributes (Judging Ability/Potential) to determine accuracy of assessment.
2.  **Refactor `calculateOverallRating`:**
    *   Currently averages all stats equally.
    *   Implement weighted average based on `PlayerRole` (e.g., Goalkeeping matters more for GK).
3.  **Refactor `calculatePotentialRating`:**
    *   Currently uses age-based formula.
    *   Use a hidden `potential` attribute (add to `Player` entity if not present, or infer from current stats + age curve + randomness based on scout ability).
4.  **Add `ScoutReportDTO`:** To expose more structured data to the frontend (e.g., specific pros/cons lists).
