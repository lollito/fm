# Implement Training Facility Bonuses

## Context
In `TrainingService.calculateEffectiveness(Team team)`, there is a TODO:
`// TODO: Add facility bonuses when infrastructure system is implemented`

The `Club` entity has a relationship with `TrainingFacility`, which contains fields like `overallQuality`, `physicalTrainingBonus`, `technicalTrainingBonus`, etc.

## Task
1.  Modify `calculateEffectiveness` to retrieve the `TrainingFacility` from the team's club:
    ```java
    Club club = team.getClub(); // or clubRepository.findByTeam(team)
    if (club != null && club.getTrainingFacility() != null) {
        // Apply bonuses
    }
    ```
2.  Implement logic to add bonuses to `baseEffectiveness`.
    *   Example: `baseEffectiveness += club.getTrainingFacility().getOverallQuality() * 0.05;` (Define appropriate math based on game balance).
    *   Consider using specific bonuses (Physical/Technical) if `TrainingFocus` is available in the context (it is passed to `processTeamTraining` but `calculateEffectiveness` only takes `Team` currently. You might need to refactor `calculateEffectiveness` to accept `TrainingFocus` or average the bonuses).
    *   *Simple approach:* Use `overallQuality` or an average of the specific bonuses for the general effectiveness multiplier.

## Verification
*   Create a test case where a team with a high-level training facility gets a higher effectiveness score than a team with no facility.
