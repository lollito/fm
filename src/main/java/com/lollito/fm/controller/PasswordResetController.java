package com.lollito.fm.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lollito.fm.model.dto.CompletePasswordResetRequest;
import com.lollito.fm.service.UserManagementService;

import lombok.Data;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    @Autowired
    private UserManagementService userManagementService;

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @RequestBody PasswordResetInitRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        userManagementService.requestPasswordReset(request.getEmail(), ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> completePasswordReset(
            @RequestBody CompletePasswordResetRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        userManagementService.completePasswordReset(
            request.getResetToken(), request.getNewPassword(), ipAddress, userAgent);

        return ResponseEntity.ok().build();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Data
    public static class PasswordResetInitRequest {
        private String email;
    }
}
