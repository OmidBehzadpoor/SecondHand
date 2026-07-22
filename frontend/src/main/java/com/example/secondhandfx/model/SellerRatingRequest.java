package com.example.secondhandfx.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerRatingRequest implements ApiRequest {
    private Integer rating;
    private String comment;
}