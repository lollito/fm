package com.lollito.fm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lollito.fm.model.NotificationPriority;
import com.lollito.fm.model.NotificationType;
import com.lollito.fm.model.User;
import com.lollito.fm.model.UserNotification;
import com.lollito.fm.repository.rest.UserNotificationRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private UserNotificationRepository notificationRepository;

    @Transactional
    public UserNotification createNotification(User user, NotificationType type, String title, String message, String actionUrl, NotificationPriority priority) {
        UserNotification notification = UserNotification.builder()
                .user(user)
                .notificationType(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .priority(priority)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Transactional
    public UserNotification createNotification(User user, NotificationType type, String title, String message, NotificationPriority priority) {
        return createNotification(user, type, title, message, null, priority);
    }

    public List<UserNotification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    public List<UserNotification> getAllNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUser().getId().equals(user.getId())) {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
            } else {
                log.warn("User {} attempted to read notification {} belonging to User {}",
                        user.getId(), notificationId, notification.getUser().getId());
            }
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<UserNotification> unread = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }
}
