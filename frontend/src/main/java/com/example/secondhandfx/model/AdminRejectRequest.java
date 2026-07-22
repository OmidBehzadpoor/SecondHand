package com.example.secondhandfx.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRejectRequest implements ApiRequest {
    private String reason;
}