package com.example.secondhand.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CaptchaResponse {
    private boolean success;

    @JsonProperty("error-codes")
    private String[] errorCodes;
}