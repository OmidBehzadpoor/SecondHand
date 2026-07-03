package com.example.secondhand.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    @Value("${recaptcha.secret}")
    private String secret;

    @Value("${recaptcha.enabled:false}")
    private boolean enabled;

    private static final String VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    public void verify(String token) {
        if (!enabled) return;

        RestTemplate restTemplate = new RestTemplate();
        String url = VERIFY_URL + "?secret=" + secret + "&response=" + token;

        Map response = restTemplate.postForObject(url, null, Map.class);

        if (response == null || !Boolean.TRUE.equals(response.get("success"))) {
            throw new RuntimeException("تایید CAPTCHA ناموفق بود");
        }
    }
}