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

    public void setOnClickHandler(Runnable handler) {
        this.onClickHandler = handler;
    }

    @FXML
    private void onCardClick() {
        if (onClickHandler != null) {
            onClickHandler.run();
        }
    }

    public AdvertisementResponse getAdvertisement() {
        return advertisement;
    }

    private String formatPrice(Long price) {
        if (price == null) return "-";
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }
}