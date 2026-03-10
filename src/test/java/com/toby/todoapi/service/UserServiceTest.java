package com.toby.todoapi.service;

import com.toby.todoapi.dto.*;
import com.toby.todoapi.model.User;
import com.toby.todoapi.repository.UserRepository;
import com.toby.todoapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@doe.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@doe.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        User savedUser = new User(1L, "John Doe", "john@doe.com", "hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@doe.com", response.getEmail());

        verify(userRepository).existsByEmail("john@doe.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@doe.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("john@doe.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        assertEquals("Email is already in use", exception.getMessage());

        verify(userRepository).existsByEmail("john@doe.com");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@doe.com");
        request.setPassword("password123");

        User user = new User(1L, "John Doe", "john@doe.com", "hashedPassword");

        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateAccessToken("john@doe.com")).thenReturn("mocked-access-token");
        when(jwtService.generateRefreshToken("john@doe.com")).thenReturn("mocked-refresh-token");

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("mocked-access-token", response.getAccessToken());
        assertEquals("mocked-refresh-token", response.getRefreshToken());

        verify(userRepository).findByEmail("john@doe.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtService).generateAccessToken("john@doe.com");
        verify(jwtService).generateRefreshToken("john@doe.com");
    }

    @Test
    void loginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@doe.com");
        request.setPassword("wrongPassword");

        User user = new User(1L, "John Doe", "john@doe.com", "hashedPassword");

        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(request)
        );

        assertEquals("Invalid email or password", exception.getMessage());

        verify(userRepository).findByEmail("john@doe.com");
        verify(passwordEncoder).matches("wrongPassword", "hashedPassword");
        verify(jwtService, never()).generateAccessToken(anyString());
        verify(jwtService, never()).generateRefreshToken(anyString());
    }

    @Test
    void refreshTokenSuccess() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        User user = new User(1L, "John Doe", "john@doe.com", "hashedPassword");

        when(jwtService.extractEmail("valid-refresh-token")).thenReturn("john@doe.com");
        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("valid-refresh-token", "john@doe.com")).thenReturn(true);
        when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtService.generateAccessToken("john@doe.com")).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("john@doe.com")).thenReturn("new-refresh-token");

        AuthResponse response = userService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());

        verify(jwtService).extractEmail("valid-refresh-token");
        verify(userRepository).findByEmail("john@doe.com");
        verify(jwtService).isTokenValid("valid-refresh-token", "john@doe.com");
        verify(jwtService).isRefreshToken("valid-refresh-token");
        verify(jwtService).generateAccessToken("john@doe.com");
        verify(jwtService).generateRefreshToken("john@doe.com");
    }

    @Test
    void refreshTokenInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-refresh-token");

        User user = new User(1L, "John Doe", "john@doe.com", "hashedPassword");

        when(jwtService.extractEmail("invalid-refresh-token")).thenReturn("john@doe.com");
        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("invalid-refresh-token", "john@doe.com")).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.refreshToken(request)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(jwtService).extractEmail("invalid-refresh-token");
        verify(userRepository).findByEmail("john@doe.com");
        verify(jwtService).isTokenValid("invalid-refresh-token", "john@doe.com");
        verify(jwtService, never()).generateAccessToken(anyString());
        verify(jwtService, never()).generateRefreshToken(anyString());
    }
}