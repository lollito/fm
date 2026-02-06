package com.lollito.fm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.repository.rest.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testSave_DuplicateUsername() {
        // Arrange
        String username = "duplicateUser";
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername(username);

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.save(request);
        });

        assertEquals("Username alredy exist", exception.getMessage());
        verify(userRepository).existsByUsername(username);
    }
}
