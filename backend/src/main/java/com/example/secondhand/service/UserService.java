package com.example.secondhand.service;

import com.example.secondhand.dto.LoginRequest;
import com.example.secondhand.dto.RegisterRequest;
import com.example.secondhand.dto.response.LoginResponse;
import com.example.secondhand.exception.InvalidCaptchaException;
import com.example.secondhand.exception.InvalidCredentialsException;
import com.example.secondhand.exception.UserAlreadyExistsException;
import com.example.secondhand.model.User;
import com.example.secondhand.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.email.verification.enabled:false}")
    private boolean verificationEnabled;
    private final CaptchaService captchaService;

    @Value("${app.security.captcha.enabled}")
    private boolean isCaptchaEnabled;

    public Long register(RegisterRequest request) {
        if (isCaptchaEnabled) {
            if (!captchaService.verifyToken(request.getCaptchaToken())) {
                throw new InvalidCaptchaException("تایید CAPTCHA ناموفق بود");
            }
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("این نام کاربری قبلاً ثبت شده است");
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("این شماره تماس قبلاً ثبت شده است");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String verificationToken =  UUID.randomUUID().toString() ;

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(hashedPassword)
                .phone(request.getPhone())
                .email(request.getEmail())
                .emailVerified(!verificationEnabled)
                .verificationToken(verificationToken)
                .build();

        userRepository.save(user);

        if (verificationEnabled) {
            emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        }

        return user.getId();
    }

    public LoginResponse login(LoginRequest request) {
        if (isCaptchaEnabled) {
            if (!captchaService.verifyToken(request.getCaptchaToken())) {
                throw new InvalidCaptchaException("تایید CAPTCHA ناموفق بود");
            }
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("نام کاربری یا رمز عبور اشتباه است"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("نام کاربری یا رمز عبور اشتباه است");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole(), user.isEmailVerified());
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("توکن نامعتبر است"));
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public void resendVerificationEmail(User currentUser) {
        if (currentUser.isEmailVerified()) {
            throw new RuntimeException("ایمیل قبلاً تایید شده است");
        }
        String newToken = UUID.randomUUID().toString();
        currentUser.setVerificationToken(newToken);
        userRepository.save(currentUser);
        emailService.sendVerificationEmail(currentUser.getEmail(), newToken);
    }
}