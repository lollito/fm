# Fix ServerService Loading Logic

## Context
The `ServerService` class handles server (game world) management. Two methods are currently problematic:

1.  `load(Long serverId)`: It retrieves a server using `serverRepository.findById(serverId).get()`. If the server doesn't exist, `.get()` throws `NoSuchElementException`, causing a 500 error instead of a handled exception. The subsequent `if (server == null)` check is dead code.
2.  `load()` (no args): It currently returns a `new Server()` wrapped in a response, which provides no actual data. It contains a FIXME comment.

## Task
1.  Refactor `load(Long serverId)`:
    *   Use `.orElseThrow(() -> new ResourceNotFoundException("Server not found"))` (or similar existing exception).
    *   Remove the `if (server == null)` block.
2.  Refactor `load()`:
    *   Determine if this method is intended to load the "current" server from a user session.
    *   If so, inject `UserService` or session bean to get the context.
    *   If it cannot be implemented yet, throw `UnsupportedOperationException` or clearly document why it returns empty.
    *   Ideally, if `UserService.getLoggedUser()` has a server, return that.

## Verification
*   Unit test `load(Long)` with an invalid ID to ensure it throws the correct exception.
*   Unit test `load(Long)` with a valid ID.
