# Specialized Coaching Impact

## User Story
As a user, I want my coaching staff's specializations to matter in training, so that hiring a Goalkeeping coach improves my keepers.

## Tasks
1.  **Refactor `StaffService.calculateClubStaffBonuses`:**
    *   Currently, it just sums up a generic `trainingBonus`.
    *   Change return DTO to `StaffBonusesDTO` with specific fields: `goalkeepingBonus`, `defendingBonus`, `attackingBonus`, `fitnessBonus`, `tacticalBonus`.
    *   Populate these based on `StaffRole` (e.g., `GOALKEEPING_COACH` contributes to `goalkeepingBonus`).
2.  **Update `TrainingService.calculateEffectiveness`:**
    *   Currently, it uses the generic `trainingBonus`.
    *   Update logic to use the *specific* bonus relevant to the *current training focus*.
    *   Example: If focus is `GOALKEEPING`, use `goalkeepingBonus`. If `ATTACKING`, use `attackingBonus`.
3.  **Update `TrainingSession` Entity:**
    *   Store which bonus was applied for historical reference.
4.  **Update Tests:** Ensure new bonus calculations are correct.
