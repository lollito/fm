## 2026-02-09 - Redundant Random Generation
**Learning:** Found a redundant `randomValue` call in `RandomUtils.randomPercentage` that was executed but ignored. This method is used extensively in simulation loops.
**Action:** Inspect helper methods used in tight loops (like simulation logic) for obvious inefficiencies or copy-paste errors.
