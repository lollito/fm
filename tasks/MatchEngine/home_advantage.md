# Implement Home Advantage

## User Story
As a user, I want home advantage to be a factor in matches, making it harder to win away at large stadiums.

## Tasks
1.  **Modify `SimulationMatchService.playMatch`:**
    *   Currently, `luckHome` and `luckAway` are initialized equally (20).
    *   Introduce a `homeAdvantageMultiplier` based on `match.getSpectators()` / `stadiumCapacity`.
    *   Example: If stadium is full (100% capacity), `luckHome` starts at 30 (instead of 20).
    *   Alternatively, modify the "advancement chance" probability directly: `RandomUtils.randomPercentage(65 + averageDiff + luck + homeAdvantage)`.
    *   Ensure the bonus is capped (e.g., max +10% chance) so it's not overpowered.
2.  **Add Configuration:**
    *   Allow league settings to influence home advantage (some leagues are tougher).
    *   This might be overkill for now, just stick to stadium size impact.
3.  **Refactor `luck` calculation:**
    *   Make the `luck` variable slightly more impactful or consistent, rather than completely random 0-5 swings every action.
