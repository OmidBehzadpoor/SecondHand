package com.example.secondhand.service;

import com.example.secondhand.model.User;
import com.example.secondhand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private final UserRepository userRepository;
    private final TelegramBotService telegramBotService;

    @Value("${app.phone.verification.enabled:false}")
    private boolean verificationEnabled;

    public void sendVerificationCode(User user, String telegramChatId) {
        if (!verificationEnabled) return;

        String code = generateCode();
        user.setPhoneVerificationCode(code);
        user.setPhoneVerificationExpiry(LocalDateTime.now().plusMinutes(10));
        user.setPhoneVerified(false);
        userRepository.save(user);

        telegramBotService.sendVerificationCode(telegramChatId, code);
    }

    public void verifyCode(User user, String code) {
        if (!verificationEnabled) {
            user.setPhoneVerified(true);
            userRepository.save(user);
            return;
        }

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

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}