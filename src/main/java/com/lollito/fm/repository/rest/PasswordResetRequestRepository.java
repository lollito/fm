package com.lollito.fm.repository.rest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lollito.fm.model.PasswordResetRequest;
import com.lollito.fm.model.ResetRequestStatus;

@Repository
public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, Long> {
    Optional<PasswordResetRequest> findByResetTokenAndStatus(String resetToken, ResetRequestStatus status);
}
