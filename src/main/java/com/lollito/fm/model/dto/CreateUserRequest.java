package com.lollito.fm.model.dto;

import java.time.LocalDate;
import java.util.Set;

import com.lollito.fm.model.Role;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String country;
    private String preferredLanguage;
    private String timezone;
    private String password;
    private Boolean isActive;
    private Boolean isVerified;
    private Set<Role> roles;
}
