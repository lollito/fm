# Secure WebSocket Notifications

## Context
In `MatchProcessor.notifyUser`, notifications are sent to a public topic:
```java
// TODO: Secure this by implementing WebSocket authentication and switching back to convertAndSendToUser.
messagingTemplate.convertAndSend("/topic/user/" + user.getId() + "/notifications", ...);
```
This exposes user notifications to anyone listening on that topic.

## Task
1.  Update `MatchProcessor.notifyUser` to use `messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/notifications", ...)` (or similar user-specific destination).
2.  Ensure `WebSocketConfig` is configured to handle user-specific queues (usually `/user/queue/errors` etc).
3.  Verify if `WebSecurityConfig` allows authenticated WebSocket connections. If `JwtTokenFilter` is used, ensure it passes the Principal to the WebSocket session.
    *   *Note:* If full WebSocket security is too large a scope, implement the code change in `MatchProcessor` and verify what happens. If it breaks because of missing Auth context, documented it or implement the minimal Auth handshake.

## Verification
*   Verify that `convertAndSendToUser` is used.
*   (Manual) Verify that a user only receives their own notifications.
