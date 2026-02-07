package com.lollito.fm.model.dto;

import java.util.Set;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String country;
    private String preferredLanguage;
    private String timezone;
    private Set<RoleDTO> roles;
}
