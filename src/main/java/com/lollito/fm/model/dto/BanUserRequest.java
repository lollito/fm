package com.lollito.fm.model.dto;

import java.time.Duration;

import lombok.Data;

@Data
public class BanUserRequest {
    private String reason;
    private Duration banDuration;
}
