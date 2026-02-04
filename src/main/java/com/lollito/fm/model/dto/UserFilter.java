package com.lollito.fm.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserFilter {
    private String search;
    private Boolean isActive;
    private Boolean isVerified;
    private Boolean isBanned;
    private String role;
}
