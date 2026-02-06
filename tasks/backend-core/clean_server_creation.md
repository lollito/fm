# Clean Server Creation Logic

## Context
In `ServerService.create(String serverName)`, there is commented-out code:
*   `// LocalDate gameStartDate = LocalDate.of(2020, Month.AUGUST, 21);`
*   `// sessionBean.setGameId(game.getId());`

The method currently uses `LocalDateTime.now()` for the game start date.

## Task
1.  Remove the commented-out `gameStartDate` line if `LocalDateTime.now()` is the intended behavior.
2.  Remove or properly implement the `sessionBean` logic if session management is required upon creation.
3.  Review the `// Pass server instead of game` comments and clean them up if the refactoring is complete.

## Verification
*   Ensure the application compiles.
*   Verify `ServerService.create` still creates a server with the correct date.
