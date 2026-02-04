package com.lollito.fm.model.dto;

import lombok.Data;

@Data
public class CompletePasswordResetRequest {
    private String resetToken;
    private String newPassword;
}
