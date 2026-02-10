## 2026-02-10 - Incremental Updates for Player Career Stats
**Learning:** Avoiding full recalculations of aggregated data (like career stats) by using incremental updates saved significant database I/O in the hot path of match simulation.
**Action:** Look for other places where aggregated data is recalculated from scratch instead of incrementally updated.
