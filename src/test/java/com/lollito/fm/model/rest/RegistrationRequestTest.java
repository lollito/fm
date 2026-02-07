package com.lollito.fm.model.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RegistrationRequestTest {

    @Test
    public void toString_shouldNotIncludeSensitiveData() {
        RegistrationRequest request = new RegistrationRequest();
        request.setName("John");
        request.setSurname("Doe");
        request.setUsername("johndoe");
        request.setEmail("john@example.com");
        request.setEmailConfirm("john@example.com");
        request.setPassword("secretPassword");
        request.setPasswordConfirm("secretPassword");
        request.setCountryId(1L);
        request.setClubName("FC John");

        String toStringOutput = request.toString();

        assertFalse(toStringOutput.contains("password=secretPassword"), "toString should not contain password");
        assertFalse(toStringOutput.contains("passwordConfirm=secretPassword"), "toString should not contain passwordConfirm");
        assertTrue(toStringOutput.contains("username=johndoe"), "toString should contain non-sensitive data like username");
    }
}
