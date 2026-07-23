package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.service.*;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.Config;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AdvertisementDetailsController {

    @FXML
    private ImageView mainImageView;
    @FXML
    private Label imageIndicatorLabel;
    @FXML
    private Button prevImageButton;
    @FXML
    private Button nextImageButton;

    @FXML
    private Label titleLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label sellerLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label rejectionReasonLabel;

    @FXML
    private HBox buyerActionsBox;
    @FXML
    private HBox ownerActionsBox;
    @FXML
    private Button markAsSoldButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    @Setter
    private String returnPage;
    @Setter
    private int returnTabIndex = 1;

    private final AdvertisementService advertisementService = new AdvertisementServiceImpl();
    private final ChatService chatService = new ChatServiceImpl();
    private AdvertisementResponse advertisement;
    private final FavoriteService favoriteService = new FavoriteServiceImpl();
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
        applyRejectionReason(ad.getStatus(), ad.getRejectionReason());
        cityLabel.setText("شهر: " + ad.getCityName());
        categoryLabel.setText("دسته‌بندی: " + ad.getCategoryName());
        descriptionLabel.setText(ad.getDescription());
        sellerLabel.setText(ad.getOwnerName() + ": فروشنده" );

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

    private void setupActionsArea() {
        boolean isOwner = SessionManager.getInstance().isLoggedIn()
                && SessionManager.getInstance().getUserId().equals(advertisement.getOwnerId());

        if (!isOwner) {
            ownerActionsBox.setVisible(false);
            ownerActionsBox.setManaged(false);
            buyerActionsBox.setVisible(true);
            buyerActionsBox.setManaged(true);
            return;
        }

        ownerActionsBox.setVisible(true);
        ownerActionsBox.setManaged(true);
        buyerActionsBox.setVisible(false);
        buyerActionsBox.setManaged(false);

        boolean isDeleted = "DELETED".equals(advertisement.getStatus());
        boolean isSold = "SOLD".equals(advertisement.getStatus());

        markAsSoldButton.setDisable(isDeleted || isSold || !"APPROVED".equals(advertisement.getStatus()));

        editButton.setVisible(!isDeleted);
        editButton.setManaged(!isDeleted);
        deleteButton.setVisible(!isDeleted);
        deleteButton.setManaged(!isDeleted);
    }

    @FXML
    private void onEditClick() {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-form.fxml", "ویرایش آگهی");
        AdvertisementFormController controller = loader.getController();
        controller.setAdvertisementId(advertisement.getId());
    }

    @FXML
    private void onDeleteClick() {
        if ("DELETED".equals(advertisement.getStatus())) {
            AlertUtil.showError("آگهی از قبل حذف شده است.");
            return;
        }

        runAsyncVoid(
                () -> advertisementService.delete(advertisement.getId()),
                () -> {
                    AlertUtil.showSuccess("آگهی با موفقیت حذف شد.");
                    String target = returnPage != null ? returnPage : "/com/example/secondhandfx/fxml/home.fxml";
                    SceneNavigator.navigateTo(target, "آگهی‌ها");
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
        requireLoginThen(() -> runAsync(
                () -> chatService.startOrGetConversation(advertisement.getId()),
                conversation -> {
                    FXMLLoader loader = SceneNavigator.navigateTo(
                            "/com/example/secondhandfx/fxml/chat-view.fxml", "چت");
                    ChatViewController controller = loader.getController();
                    controller.setConversation(conversation);
                },
                "خطا در شروع گفتگو با فروشنده"
        ));
    }

    @FXML
    private void onAddFavoriteClick() {
        requireLoginThen(() -> runAsyncVoid(
                () -> favoriteService.addFavorite(advertisement.getId()),
                () -> AlertUtil.showSuccess("آگهی به علاقه‌مندی‌ها اضافه شد."),
                "خطا در افزودن به علاقه‌مندی‌ها"
        ));
    }

    @FXML
    private void onBackClick() {
        String target = returnPage != null ? returnPage : "/com/example/secondhandfx/fxml/home.fxml";
        FXMLLoader loader = SceneNavigator.navigateTo(target, "آگهی‌ها");

        Object controller = loader.getController();
        if (controller instanceof AdminPanelController) {
            ((AdminPanelController) controller).setSelectedTabIndex(returnTabIndex);
        }
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

    private void applyRejectionReason(String status, String rejectionReason) {
        boolean shouldShow = "REJECTED".equals(status)
                && rejectionReason != null && !rejectionReason.isBlank();

        rejectionReasonLabel.setVisible(shouldShow);
        rejectionReasonLabel.setManaged(shouldShow);

        if (shouldShow) {
            rejectionReasonLabel.setText("دلیل رد آگهی: " + rejectionReason);
        }
    }
}