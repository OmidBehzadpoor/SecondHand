package com.example.secondhand.service;

import com.example.secondhand.dto.response.CaptchaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.security.captcha.secret-key}")
    private String secretKey;

    @Value("${app.security.captcha.url}")
    private String captchaUrl;

    public boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("secret", secretKey);
        requestParams.add("response", token);

        try {
            CaptchaResponse apiResponse = restTemplate.postForObject(
                    captchaUrl,
                    requestParams,
                    CaptchaResponse.class
            );
            return apiResponse != null && apiResponse.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }
}