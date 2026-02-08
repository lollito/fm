package com.lollito.fm.repository.rest;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.User;
import com.lollito.fm.model.UserNotification;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    List<UserNotification> findByUserOrderByCreatedAtDesc(User user);
}
