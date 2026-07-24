package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.util.Config;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * <h2>AdvertisementCardController</h2>
 * <p>
 * کنترلر کارت نمایش خلاصه‌ی یک <b>آگهی</b> در فهرست عمومی آگهی‌ها (مثلاً در
 * صفحه‌ی خانه). تصویر شاخص، عنوان، قیمت، شهر، دسته‌بندی و امتیاز فروشنده را
 * نمایش می‌دهد و کلیک روی کارت را به یک {@link Runnable} handler که توسط
 * کنترلر والد ثبت شده واگذار می‌کند.
 * </p>
 *
 * @author تیم فرانت‌اند
 */
public class AdvertisementCardController {

    @FXML private VBox root;
    @FXML private ImageView imageView;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label cityLabel;
    @FXML private Label categoryLabel;
    @FXML private Label ratingLabel;

    private AdvertisementResponse advertisement;
    private Runnable onClickHandler;

    /**
     * مقداردهی محتوای کارت بر اساس اطلاعات یک آگهی.
     * <p>
     * در صورت وجود حداقل یک تصویر برای آگهی، اولین تصویر به‌عنوان تصویر
     * شاخص بارگذاری می‌شود. در صورت وجود حداقل یک امتیاز ثبت‌شده برای
     * فروشنده، میانگین و تعداد امتیازها نمایش داده می‌شود؛ در غیر این صورت
     * پیام «بدون امتیاز» نشان داده می‌شود.
     * </p>
     *
     * @param ad داده‌ی آگهی که باید در کارت نمایش داده شود
     */
    public void setData(AdvertisementResponse ad) {
        this.advertisement = ad;
        titleLabel.setText(ad.getTitle());
        priceLabel.setText(formatPrice(ad.getPrice()) + " تومان");
        cityLabel.setText(ad.getCityName());
        categoryLabel.setText(ad.getCategoryName());

        if (ad.getImageUrls() != null && !ad.getImageUrls().isEmpty()) {
            String imageUrl = Config.getApiBaseUrl() + ad.getImageUrls().get(0);
            imageView.setImage(new Image(imageUrl, true));
        } else {
            imageView.setImage(null);
        }

        if (ad.getSellerAverageRating() != null && ad.getSellerRatingCount() != null && ad.getSellerRatingCount() > 0) {
            ratingLabel.setText(String.format("⭐ %.1f (%d)", ad.getSellerAverageRating(), ad.getSellerRatingCount()));
        } else {
            ratingLabel.setText("بدون امتیاز");
        }
    }

    /**
     * ثبت متد اجراشونده هنگام کلیک روی کارت.
     *
     * @param handler عملیاتی که باید هنگام کلیک روی کارت اجرا شود
     */
    public void setOnClickHandler(Runnable handler) {
        this.onClickHandler = handler;
    }

    /**
     * پردازش کلیک روی کارت؛ در صورت ثبت‌شدن یک handler، آن را اجرا می‌کند.
     */
    @FXML
    private void onCardClick() {
        if (onClickHandler != null) {
            onClickHandler.run();
        }
    }

    /**
     * دریافت داده‌ی آگهی فعلی این کارت.
     *
     * @return آگهی‌ای که آخرین بار با {@link #setData(AdvertisementResponse)} تنظیم شده است
     */
    public AdvertisementResponse getAdvertisement() {
        return advertisement;
    }

    /**
     * قالب‌بندی مقدار قیمت برای نمایش خوانا با جداکننده‌ی هزارگان.
     *
     * @param price مقدار قیمت؛ می‌تواند {@code null} باشد
     * @return رشته‌ی قیمت قالب‌بندی‌شده، یا {@code "-"} در صورت {@code null} بودن مقدار ورودی
     */
    private String formatPrice(Long price) {
        if (price == null) return "-";
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }
}
