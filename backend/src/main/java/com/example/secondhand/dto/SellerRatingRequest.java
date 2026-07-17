package com.example.secondhand.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerRatingRequest {

    @NotNull(message = "امتیاز نمی‌تواند خالی باشد")
    @Min(value = 1, message = "امتیاز باید حداقل ۱ باشد")
    @Max(value = 5, message = "امتیاز باید حداکثر ۵ باشد")
    private Integer rating;

    private String comment;
}