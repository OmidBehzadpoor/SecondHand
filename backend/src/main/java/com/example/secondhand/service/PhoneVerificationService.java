package com.example.secondhand.service;

import com.example.secondhand.model.User;
import com.example.secondhand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final UserRepository userRepository;

    public void verifyCode(User user, String code) {
        if (user.getPhoneVerificationCode() == null) {
            throw new RuntimeException("کد تایید ارسال نشده است");
        }

        if (LocalDateTime.now().isAfter(user.getPhoneVerificationExpiry())) {
            throw new RuntimeException("کد تایید منقضی شده است");
        }

        if (!user.getPhoneVerificationCode().equals(code)) {
            throw new RuntimeException("کد تایید اشتباه است");
        }

        user.setPhoneVerified(true);
        user.setPhoneVerificationCode(null);
        user.setPhoneVerificationExpiry(null);
        userRepository.save(user);
    }
}