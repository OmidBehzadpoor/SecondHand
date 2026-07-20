package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.service.AdvertisementService;
import com.example.secondhandfx.service.AdvertisementServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.Config;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AdvertisementDetailsController {

    @FXML private ImageView mainImageView;
    @FXML private Label imageIndicatorLabel;
    @FXML private Button prevImageButton;
    @FXML private Button nextImageButton;

    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label statusLabel;
    @FXML private Label cityLabel;
    @FXML private Label categoryLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label sellerLabel;
    @FXML private Label ratingLabel;

    // ناحیه‌ی دکمه‌ها بسته به اینکه بیننده صاحب آگهیه یا نه، عوض می‌شه
    @FXML private HBox buyerActionsBox;
    @FXML private HBox ownerActionsBox;
    @FXML private Button markAsSoldButton;

    private final AdvertisementService advertisementService = new AdvertisementServiceImpl();

    private AdvertisementResponse advertisement;
    private List<String> imageUrls;
    private int currentImageIndex = 0;

    public void setAdvertisementId(Long id) {
        runAsync(
                () -> advertisementService.getById(id),
                this::renderAdvertisement,
                "خطا در دریافت اطلاعات آگهی"
        );
    }

    private void renderAdvertisement(AdvertisementResponse ad) {
        this.advertisement = ad;
        this.imageUrls = ad.getImageUrls();
        this.currentImageIndex = 0;

        titleLabel.setText(ad.getTitle());
        priceLabel.setText(formatPrice(ad.getPrice()) + " تومان");
        statusLabel.setText(mapStatus(ad.getStatus()));
        cityLabel.setText("شهر: " + ad.getCityName());
        categoryLabel.setText("دسته‌بندی: " + ad.getCategoryName());
        descriptionLabel.setText(ad.getDescription());
        sellerLabel.setText("فروشنده: " + ad.getOwnerUsername());

        if (ad.getSellerAverageRating() != null && ad.getSellerRatingCount() != null && ad.getSellerRatingCount() > 0) {
            ratingLabel.setText(String.format("⭐ %.1f از ۵ (%d امتیاز)", ad.getSellerAverageRating(), ad.getSellerRatingCount()));
        } else {
            ratingLabel.setText("این فروشنده هنوز امتیازی ندارد");
        }

        renderCurrentImage();
        setupActionsArea();
    }

    private void renderCurrentImage() {
        boolean hasImages = imageUrls != null && !imageUrls.isEmpty();

        prevImageButton.setDisable(!hasImages);
        nextImageButton.setDisable(!hasImages);

        if (!hasImages) {
            mainImageView.setImage(null);
            imageIndicatorLabel.setText("بدون تصویر");
            return;
        }

        String relativeUrl = imageUrls.get(currentImageIndex);
        String fullUrl = Config.getApiBaseUrl() + relativeUrl;
        // پارامتر آخر یعنی بارگذاری در پس‌زمینه انجام بشه، نه روی FX Thread
        mainImageView.setImage(new Image(fullUrl, true));
        imageIndicatorLabel.setText((currentImageIndex + 1) + " از " + imageUrls.size());
    }

    @FXML
    private void onPrevImageClick() {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        currentImageIndex = (currentImageIndex - 1 + imageUrls.size()) % imageUrls.size();
        renderCurrentImage();
    }

    @FXML
    private void onNextImageClick() {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        currentImageIndex = (currentImageIndex + 1) % imageUrls.size();
        renderCurrentImage();
    }

    // بر اساس اینکه بیننده صاحب آگهیه یا یک خریدار، دکمه‌های مناسب رو نشون می‌ده
    private void setupActionsArea() {
        boolean isOwner = SessionManager.getInstance().isLoggedIn()
                && SessionManager.getInstance().getUserId().equals(advertisement.getOwnerId());

        ownerActionsBox.setVisible(isOwner);
        ownerActionsBox.setManaged(isOwner);

        buyerActionsBox.setVisible(!isOwner);
        buyerActionsBox.setManaged(!isOwner);

        if (isOwner) {
            boolean alreadySold = "SOLD".equals(advertisement.getStatus());
            markAsSoldButton.setDisable(alreadySold);
        }
    }

    @FXML
    private void onEditClick() {
        AlertUtil.showSuccess("صفحه‌ی ویرایش آگهی به زودی اضافه می‌شود!");
    }

    @FXML
    private void onDeleteClick() {
        runAsyncVoid(
                () -> advertisementService.delete(advertisement.getId()),
                () -> {
                    AlertUtil.showSuccess("آگهی با موفقیت حذف شد.");
                    SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
                },
                "خطا در حذف آگهی"
        );
    }

    @FXML
    private void onMarkAsSoldClick() {
        runAsync(
                () -> advertisementService.markAsSold(advertisement.getId()),
                this::renderAdvertisement,
                "خطا در تغییر وضعیت آگهی"
        );
    }

    @FXML
    private void onMessageSellerClick() {
        requireLoginThen(() -> AlertUtil.showSuccess("صفحه‌ی گفت‌وگو با فروشنده به زودی اضافه می‌شود!"));
    }

    @FXML
    private void onAddFavoriteClick() {
        requireLoginThen(() -> AlertUtil.showSuccess("افزودن به علاقه‌مندی‌ها به زودی اضافه می‌شود!"));
    }

    @FXML
    private void onBackClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    private void requireLoginThen(Runnable action) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            AlertUtil.showError("برای این کار ابتدا باید وارد حساب کاربری خود شوید.");
            SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
            return;
        }
        action.run();
    }

    private String mapStatus(String status) {
        if (status == null) return "-";
        return switch (status) {
            case "PENDING" -> "در انتظار بررسی";
            case "APPROVED" -> "فعال";
            case "REJECTED" -> "رد شده";
            case "SOLD" -> "فروخته شده";
            case "DELETED" -> "حذف شده";
            default -> status;
        };
    }

    private String formatPrice(Long price) {
        if (price == null) return "-";
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }

    private <T> void runAsync(ThrowingSupplier<T> supplier, java.util.function.Consumer<T> onSuccess, String errorMessage) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, Executors.newVirtualThreadPerTaskExecutor()).whenComplete((result, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                    String message = (cause instanceof ApiException ae) ? ae.getMessage() : errorMessage;
                    AlertUtil.showError(message);
                } else {
                    onSuccess.accept(result);
                }
            });
        });
    }

    private void runAsyncVoid(ThrowingRunnable action, Runnable onSuccess, String errorMessage) {
        CompletableFuture.runAsync(() -> {
            try {
                action.run();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }, Executors.newVirtualThreadPerTaskExecutor()).whenComplete((result, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                    String message = (cause instanceof ApiException ae) ? ae.getMessage() : errorMessage;
                    AlertUtil.showError(message);
                } else {
                    onSuccess.run();
                }
            });
        });
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws ApiException;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws ApiException;
    }
}