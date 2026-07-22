package com.example.secondhand.service;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.AdminUserResponse;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.exception.InvalidCredentialsException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.exception.UserAlreadyExistsException;
import com.example.secondhand.exception.UserBlockedException;
import com.example.secondhand.exception.UserNotFoundException;
import com.example.secondhand.exception.UserStateConflictException;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Ali Ahmadi");
        request.setUsername("ali123");
        request.setPassword("123456");
        request.setPhone("09121234567");
        request.setEmail("ali@example.com");
        return request;
    }

    // ==================== register ====================

    @Test
    void register_shouldRegisterUser_whenDataIsValid() {
        User saved = User.builder().id(1L).username("ali123").build();

        when(userRepository.existsByUsername("ali123")).thenReturn(false);
        when(userRepository.existsByPhone("09121234567")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        Long userId = userService.register(registerRequest());

        assertEquals(1L, userId);
    }

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenUsernameIsTaken() {
        when(userRepository.existsByUsername("ali123")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(registerRequest()));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrowUserAlreadyExistsException_whenPhoneIsTaken() {
        when(userRepository.existsByUsername("ali123")).thenReturn(false);
        when(userRepository.existsByPhone("09121234567")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(registerRequest()));

        verify(userRepository, never()).save(any());
    }

    // ==================== login ====================

    @Test
    void login_shouldReturnLoginResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ali123");
        request.setPassword("123456");

        User user = User.builder().id(1L).username("ali123").password("hashed")
                .role(Role.USER).status(UserStatus.ACTIVE).build();

        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = userService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenUserDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("123456");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_shouldThrowInvalidCredentialsException_whenPasswordIsWrong() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ali123");
        request.setPassword("wrong");

        User user = User.builder().id(1L).username("ali123").password("hashed").status(UserStatus.ACTIVE).build();

        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.login(request));
    }

    @Test
    void login_shouldThrowUserBlockedException_whenUserIsBlocked() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ali123");
        request.setPassword("123456");

        User user = User.builder().id(1L).username("ali123").password("hashed").status(UserStatus.BLOCKED).build();

        when(userRepository.findByUsername("ali123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hashed")).thenReturn(true);

        assertThrows(UserBlockedException.class, () -> userService.login(request));

        verify(jwtService, never()).generateToken(any());
    }

    // ==================== getAllUsersForAdmin ====================

    @Test
    void getAllUsersForAdmin_shouldReturnAllUsers() {
        User u1 = User.builder().id(1L).username("ali").role(Role.USER).status(UserStatus.ACTIVE).build();
        User u2 = User.builder().id(2L).username("admin1").role(Role.ADMIN).status(UserStatus.ACTIVE).build();

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<AdminUserResponse> result = userService.getAllUsersForAdmin();

        assertEquals(2, result.size());
    }

    // ==================== blockUser ====================

    @Test
    void blockUser_shouldBlockUser_whenUserIsActiveAndNotAdmin() {
        User user = User.builder().id(1L).role(Role.USER).status(UserStatus.ACTIVE).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AdminUserResponse response = userService.blockUser(1L);

        assertEquals(UserStatus.BLOCKED, response.getUserStatus());
    }

    @Test
    void blockUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.blockUser(99L));

        verify(userRepository, never()).save(any());
    }

    @Test
    void blockUser_shouldThrowUnauthorizedActionException_whenTargetIsAdmin() {
        User admin = User.builder().id(2L).role(Role.ADMIN).status(UserStatus.ACTIVE).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        assertThrows(UnauthorizedActionException.class, () -> userService.blockUser(2L));

        verify(userRepository, never()).save(any());
    }

    @Test
    void blockUser_shouldThrowUserStateConflictException_whenAlreadyBlocked() {
        User user = User.builder().id(1L).role(Role.USER).status(UserStatus.BLOCKED).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(UserStateConflictException.class, () -> userService.blockUser(1L));

        verify(userRepository, never()).save(any());
    }

    // ==================== unblockUser ====================

    @Test
    void unblockUser_shouldUnblockUser_whenCurrentlyBlocked() {
        User user = User.builder().id(1L).role(Role.USER).status(UserStatus.BLOCKED).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AdminUserResponse response = userService.unblockUser(1L);

        assertEquals(UserStatus.ACTIVE, response.getUserStatus());
    }

    @Test
    void unblockUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.unblockUser(99L));

        verify(userRepository, never()).save(any());
    }

    @Test
    void unblockUser_shouldThrowUserStateConflictException_whenAlreadyActive() {
        User user = User.builder().id(1L).role(Role.USER).status(UserStatus.ACTIVE).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(UserStateConflictException.class, () -> userService.unblockUser(1L));

        verify(userRepository, never()).save(any());
    }
}
