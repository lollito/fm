# Smart Substitutions in Match Engine

## User Story
As a user, I want the opposing team to make intelligent substitutions based on player condition and match situation, rather than random changes.

## Tasks
1.  **Refactor `SimulationMatchService.performSubstitution`:**
    *   Currently, it selects a random player to substitute.
    *   Implement logic to find the *worst performing* player (lowest rating).
    *   Implement logic to find the *most fatigued* player (lowest condition).
    *   Prioritize players on a yellow card if the team is winning comfortably (risk management).
    *   Consider score: If losing, sub on an attacker for a defender/midfielder. If winning, sub on a defender for an attacker.
2.  **Add `SubstitutionStrategy` Enum:**
    *   `AGGRESSIVE`, `DEFENSIVE`, `BALANCED`, `AUTO`.
    *   Allow teams (manager AI) to have a preferred strategy.
    *   Use this strategy in the substitution logic.
3.  **Refactor `performSubstitution` to use `SubstitutionStrategy`:**
    *   Pass the strategy into the method.
    *   Use it to filter candidates for substitution.
