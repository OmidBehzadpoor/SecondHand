package com.example.secondhand.service;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.exception.InvalidCredentialsException;
import com.example.secondhand.exception.UserAlreadyExistsException;
import com.example.secondhand.exception.UserBlockedException;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    // ==================== register ====================

    @Test
    void register_shouldRegisterUser_whenDataIsValid() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ali Ahmadi");
        request.setUsername("ali123");
        request.setPassword("123456");
        request.setPhone("09121234567");
        request.setEmail("ali@example.com");

        User savedUser = User.builder()
                .id(1L)
                .name("Ali Ahmadi")
                .username("ali123")
                .password("hashed-password")
                .phone("09121234567")
                .email("ali@example.com")
                .build();

        when(userRepository.existsByUsername("ali123")).thenReturn(false);
        when(userRepository.existsByPhone("09121234567")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Long userId = userService.register(request);

        assertEquals(1L, userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenUsernameIsTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ali123");
        request.setPhone("09121234567");

        when(userRepository.existsByUsername("ali123")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenPhoneIsTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ali123");
        request.setPhone("09121234567");

        when(userRepository.existsByUsername("ali123")).thenReturn(false);
        when(userRepository.existsByPhone("09121234567")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(request));

        verify(userRepository, never()).save(any());
    }

    // ==================== login ====================

    @Test
    void login_shouldReturnLoginResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ali123");
        request.setPassword("123456");

        User user = User.builder()
                .id(1L)
                .username("ali123")
                .password("hashed-password")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("ali123", response.getUsername());
        assertEquals(Role.USER, response.getRole());
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenUserDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("123456");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(request));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenPasswordIsWrong() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ali123");
        request.setPassword("wrong-password");

        User user = User.builder()
                .id(1L)
                .username("ali123")
                .password("hashed-password")
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.login(request));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowUserBlockedException_whenUserIsBlocked() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ali123");
        request.setPassword("123456");

        User user = User.builder()
                .id(1L)
                .username("ali123")
                .password("hashed-password")
                .status(UserStatus.BLOCKED)
                .build();

        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed-password")).thenReturn(true);

        assertThrows(UserBlockedException.class,
                () -> userService.login(request));

        verify(jwtService, never()).generateToken(any());
    }
}
