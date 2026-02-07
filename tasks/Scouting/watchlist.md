# Implement Scouting Watchlist

## User Story
As a user, I want to be able to add players to a watchlist so I can track their progress without assigning a scout immediately.

## Tasks
1.  **Update `ScoutingService`:** Implement the `addToWatchlist` method (currently a stub).
    *   It should check if the player is already in the watchlist for the club.
    *   It should create a `WatchlistEntry` entity (or similar) linking `Club` and `Player`.
2.  **Create `WatchlistEntry` Entity:**
    *   Fields: `id`, `club`, `player`, `dateAdded`, `notes`.
3.  **Create `WatchlistController`:**
    *   `POST /api/scouting/watchlist/{playerId}`: Add player to watchlist.
    *   `DELETE /api/scouting/watchlist/{entryId}`: Remove from watchlist.
    *   `GET /api/scouting/watchlist`: Get all watchlist entries for the logged-in user's club.
4.  **Add Tests:** Unit tests for service and integration tests for controller.
