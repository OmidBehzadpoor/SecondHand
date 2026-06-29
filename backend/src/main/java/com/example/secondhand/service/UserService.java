package com.example.secondhand.service;

import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("این نام کاربری قبلاً ثبت شده است");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("این شماره تماس قبلاً ثبت شده است");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(hashedPassword)
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        return userRepository.save(user);
    }
}