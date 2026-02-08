package com.lollito.fm.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.User;
import com.lollito.fm.model.UserNotification;
import com.lollito.fm.service.NotificationService;
import com.lollito.fm.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/unread")
    public ResponseEntity<List<UserNotification>> getUnreadNotifications(Authentication authentication) {
        User user = userService.getUser(authentication.getName());
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        User user = userService.getUser(authentication.getName());
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = userService.getUser(authentication.getName());
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
}
