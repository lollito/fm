package com.lollito.fm.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.lollito.fm.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String country;
    private LocalDate dateOfBirth;
    private String preferredLanguage;
    private String timezone;
    private Boolean isActive;
    private Boolean isVerified;
    private Boolean isBanned;
    private String banReason;
    private LocalDateTime bannedUntil;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    private Set<Role> roles;
}
