package com.example.secondhand.service;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.AdminUserResponse;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.exception.InvalidCredentialsException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.exception.UserNotFoundException;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.model.UserStatus;
import com.example.secondhand.repository.UserRepository;
import com.example.secondhand.exception.UserAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Long register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("این نام کاربری قبلاً ثبت شده است");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("این شماره تماس قبلاً ثبت شده است");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(hashedPassword)
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        return userRepository.save(user).getId();
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("نام کاربری یا رمز عبور اشتباه است"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("نام کاربری یا رمز عبور اشتباه است");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsersForAdmin() {
        return userRepository.findAll().stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserResponse blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("کاربر مورد نظر یافت نشد"));

        if (user.getRole() == Role.ADMIN) {
            throw new UnauthorizedActionException("امکان تغییر وضعیت دسترسی سایر مدیران وجود ندارد");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("کاربر از قبل مسدود شده است");
        }

        user.setStatus(UserStatus.BLOCKED);
        return mapToAdminResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("کاربر مورد نظر یافت نشد"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalStateException("کاربر از قبل فعال است");
        }

        user.setStatus(UserStatus.ACTIVE);
        return mapToAdminResponse(userRepository.save(user));
    }

    private AdminUserResponse mapToAdminResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .userStatus(user.getStatus())
                .build();
    }
}