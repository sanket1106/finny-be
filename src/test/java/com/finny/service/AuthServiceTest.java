package com.finny.service;

import com.finny.constant.AuthConstants;
import com.finny.domain.master.User;
import com.finny.domain.master.UserSession;
import com.finny.dto.LoginRequest;
import com.finny.dto.LoginResponse;
import com.finny.repository.master.UserRepository;
import com.finny.repository.master.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserSessionRepository userSessionRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginUser_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");

        UserSession oldSession = new UserSession();
        oldSession.setActive(true);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(userSessionRepository.findByUserAndActiveTrue(user)).thenReturn(Collections.singletonList(oldSession));
        
        UserSession newSession = new UserSession();
        newSession.setToken("new-session-id");
        when(userSessionRepository.save(any(UserSession.class))).thenReturn(newSession);

        LoginResponse response = authService.loginUser(request, "Mozilla/5.0");

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotEquals("new-session-id", response.getToken());

        assertFalse(oldSession.isActive());
        assertEquals(AuthConstants.INVALIDATED_BY_SYSTEM, oldSession.getInvalidatedBy());
        verify(userSessionRepository).saveAll(anyList());
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void testLoginUser_InvalidEmail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.loginUser(request, "Mozilla/5.0"));

        assertEquals(AuthConstants.ERROR_INVALID_CREDENTIALS, exception.getMessage());
    }
}
