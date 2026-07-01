package com.example.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdvertisementRequest {

    @NotBlank(message = "عنوان نمی‌تواند خالی باشد")
    private String title;

    @NotBlank(message = "توضیحات نمی‌تواند خالی باشد")
    private String description;

    @NotNull(message = "قیمت نمی‌تواند خالی باشد")
    @Positive(message = "قیمت باید عدد مثبت باشد")
    private Long price;

    @NotNull(message = "دسته‌بندی انتخاب نشده")
    private Long categoryId;

    @NotNull(message = "شهر انتخاب نشده")
    private Long cityId;

    private List<String> imageUrls;

}
