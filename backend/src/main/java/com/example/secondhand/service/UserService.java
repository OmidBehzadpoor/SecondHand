package com.example.secondhand.service;

import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.UserRepository;
import com.example.secondhand.exception.UserAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}