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

/**
 * <h2>AdvertisementDetailsController</h2>
 * <p>
 * کنترلر صفحه‌ی <b>جزئیات آگهی</b>، شامل نمایش گالری تصاویر (با قابلیت
 * جابه‌جایی بین تصاویر)، اطلاعات کامل آگهی و فروشنده، و عملیات مخصوص خریدار
 * (شروع گفت‌وگو با فروشنده، افزودن به علاقه‌مندی‌ها) یا مالک آگهی (ویرایش،
 * حذف، فروخته‌شده علامت زدن)، بسته به اینکه کاربر جاری مالک آگهی باشد یا خیر.
 * </p>
 * <p>
 * این صفحه از چند مسیر مختلف (خانه، آگهی‌های من، پنل ادمین، علاقه‌مندی‌ها)
 * قابل دسترسی است؛ به همین دلیل {@link #returnPage} و {@link #returnTabIndex}
 * برای بازگرداندن کاربر به همان مبدأ هنگام کلیک روی دکمه‌ی بازگشت استفاده می‌شوند.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.AdvertisementService
 */
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

    /** مسیر (FXML) صفحه‌ای که کاربر پیش از ورود به این صفحه در آن بوده است؛ برای بازگشت استفاده می‌شود. */
    @Setter
    private String returnPage;
    /** شاخص تب مقصد در صفحه‌ی بازگشتی (در صورتی که مقصد پنل ادمین با چند تب باشد). */
    @Setter
    private int returnTabIndex = 1;

    private final AdvertisementService advertisementService = new AdvertisementServiceImpl();
    private final ChatService chatService = new ChatServiceImpl();
    private AdvertisementResponse advertisement;
    private final FavoriteService favoriteService = new FavoriteServiceImpl();
    private List<String> imageUrls;
    private int currentImageIndex = 0;

    /**
     * تنظیم شناسه‌ی آگهی مورد نظر و بارگذاری غیرهمزمان اطلاعات کامل آن از سرور.
     *
     * @param id شناسه‌ی آگهی‌ای که باید نمایش داده شود
     */
    public void setAdvertisementId(Long id) {
        runAsync(
                () -> advertisementService.getById(id),
                this::renderAdvertisement,
                "خطا در دریافت اطلاعات آگهی"
        );
    }

    /**
     * نمایش کامل اطلاعات آگهی دریافت‌شده در تمام بخش‌های صفحه، شامل گالری
     * تصاویر، اطلاعات فروشنده، امتیاز و ناحیه‌ی عملیات.
     *
     * @param ad داده‌ی کامل آگهی دریافت‌شده از سرور
     */
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
            ratingLabel.setText(String.format("⭐ %.1f از ۵ (%d رای)", ad.getSellerAverageRating(), ad.getSellerRatingCount()));
        } else {
            ratingLabel.setText("این فروشنده هنوز امتیازی ندارد");
        }

        renderCurrentImage();
        setupActionsArea();
    }

    /**
     * نمایش تصویر فعلی گالری (بر اساس {@link #currentImageIndex}) و
     * به‌روزرسانی برچسب شمارنده‌ی تصاویر و فعال/غیرفعال بودن دکمه‌های
     * جابه‌جایی. در صورت نداشتن تصویر، پیام «بدون تصویر» نمایش داده می‌شود.
     */
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

    /**
     * پردازش کلیک روی دکمه‌ی تصویر قبلی؛ جابه‌جایی چرخشی به تصویر قبل در گالری.
     */
    @FXML
    private void onPrevImageClick() {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        currentImageIndex = (currentImageIndex - 1 + imageUrls.size()) % imageUrls.size();
        renderCurrentImage();
    }

    /**
     * پردازش کلیک روی دکمه‌ی تصویر بعدی؛ جابه‌جایی چرخشی به تصویر بعد در گالری.
     */
    @FXML
    private void onNextImageClick() {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        currentImageIndex = (currentImageIndex + 1) % imageUrls.size();
        renderCurrentImage();
    }

    /**
     * تعیین اینکه کاربر جاری مالک آگهی است یا نه، و بر اساس آن نمایان کردن
     * ناحیه‌ی عملیات مناسب (خریدار یا مالک) به‌همراه فعال/غیرفعال و
     * نمایان/مخفی بودن دکمه‌های مربوطه بسته به وضعیت فعلی آگهی.
     */
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

    /**
     * پردازش کلیک روی دکمه‌ی «ویرایش»: هدایت کاربر به فرم آگهی در حالت ویرایش.
     */
    @FXML
    private void onEditClick() {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-form.fxml", "ویرایش آگهی");
        AdvertisementFormController controller = loader.getController();
        controller.setAdvertisementId(advertisement.getId());
    }

    /**
     * پردازش کلیک روی دکمه‌ی «حذف»: حذف نرم آگهی و بازگشت به مبدأ در صورت موفقیت.
     */
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

    /**
     * پردازش کلیک روی دکمه‌ی «فروخته‌شد»: علامت‌گذاری آگهی به‌عنوان فروخته‌شده و
     * به‌روزرسانی نمایش صفحه با اطلاعات جدید.
     */
    @FXML
    private void onMarkAsSoldClick() {
        runAsync(
                () -> advertisementService.markAsSold(advertisement.getId()),
                this::renderAdvertisement,
                "خطا در تغییر وضعیت آگهی"
        );
    }

    /**
     * پردازش کلیک روی دکمه‌ی «ارسال پیام به فروشنده»: در صورت وارد بودن
     * کاربر، شروع یا بازیابی گفت‌وگو با فروشنده و هدایت به صفحه‌ی چت.
     */
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

    /**
     * پردازش کلیک روی دکمه‌ی «افزودن به علاقه‌مندی‌ها»: در صورت وارد بودن
     * کاربر، افزودن آگهی جاری به لیست علاقه‌مندی‌های او.
     */
    @FXML
    private void onAddFavoriteClick() {
        requireLoginThen(() -> runAsyncVoid(
                () -> favoriteService.addFavorite(advertisement.getId()),
                () -> AlertUtil.showSuccess("آگهی به علاقه‌مندی‌ها اضافه شد."),
                "خطا در افزودن به علاقه‌مندی‌ها"
        ));
    }

    /**
     * پردازش کلیک روی دکمه‌ی بازگشت: هدایت کاربر به صفحه‌ی مبدأ
     * ({@link #returnPage})، و در صورتی که مبدأ پنل ادمین باشد، انتخاب مجدد
     * تب مناسب ({@link #returnTabIndex}) در آن پنل.
     */
    @FXML
    private void onBackClick() {
        String target = returnPage != null ? returnPage : "/com/example/secondhandfx/fxml/home.fxml";
        FXMLLoader loader = SceneNavigator.navigateTo(target, "آگهی‌ها");

        Object controller = loader.getController();
        if (controller instanceof AdminPanelController) {
            ((AdminPanelController) controller).setSelectedTabIndex(returnTabIndex);
        }
    }

    /**
     * اجرای یک عملیات فقط در صورتی که کاربر جاری وارد سیستم شده باشد؛ در غیر
     * این صورت پیام خطا نمایش داده شده و کاربر به صفحه‌ی ورود هدایت می‌شود.
     *
     * @param action عملیاتی که فقط برای کاربران واردشده باید اجرا شود
     */
    private void requireLoginThen(Runnable action) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            AlertUtil.showError("برای این کار ابتدا باید وارد حساب کاربری خود شوید.");
            SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
            return;
        }
        action.run();
    }

    /**
     * ترجمه‌ی مقدار خام وضعیت آگهی به متن نمایشی فارسی.
     *
     * @param status مقدار خام وضعیت؛ می‌تواند {@code null} باشد
     * @return متن فارسی متناظر با وضعیت، یا {@code "-"} در صورت {@code null} بودن ورودی
     */
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

    /**
     * اجرای غیرهمزمان یک عملیات بازگرداننده‌ی مقدار (روی یک {@code Virtual Thread})
     * و بازگرداندن نتیجه یا نمایش خطای مناسب در نخ رابط کاربری (JavaFX Application Thread).
     *
     * @param supplier     عملیات ناهمزمانی که مقداری از نوع {@code T} برمی‌گرداند و ممکن است {@link ApiException} پرتاب کند
     * @param onSuccess    عملیاتی که در صورت موفقیت با نتیجه‌ی دریافتی فراخوانی می‌شود
     * @param errorMessage پیام پیش‌فرض خطا برای زمانی که استثنای پرتاب‌شده از نوع {@link ApiException} نباشد
     * @param <T>          نوع نتیجه‌ی بازگردانده‌شده توسط {@code supplier}
     */
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

    /**
     * اجرای غیرهمزمان یک عملیات بدون مقدار بازگشتی (روی یک {@code Virtual Thread})
     * و فراخوانی callback موفقیت یا نمایش خطای مناسب در نخ رابط کاربری.
     *
     * @param action       عملیات ناهمزمانی که ممکن است {@link ApiException} پرتاب کند
     * @param onSuccess    عملیاتی که در صورت موفقیت اجرا می‌شود
     * @param errorMessage پیام پیش‌فرض خطا برای زمانی که استثنای پرتاب‌شده از نوع {@link ApiException} نباشد
     */
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

    /**
     * اینترفیس تابعی داخلی برای عملیات‌های ناهمزمانی که مقداری بازمی‌گردانند
     * و ممکن است {@link ApiException} پرتاب کنند.
     *
     * @param <T> نوع مقدار بازگشتی
     */
    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws ApiException;
    }

    /**
     * اینترفیس تابعی داخلی برای عملیات‌های ناهمزمانی بدون مقدار بازگشتی که
     * ممکن است {@link ApiException} پرتاب کنند.
     */
    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws ApiException;
    }

    /**
     * نمایش یا مخفی‌سازی برچسب دلیل رد آگهی، بر اساس وضعیت آگهی و موجود
     * بودن دلیل رد.
     *
     * @param status           مقدار خام وضعیت فعلی آگهی
     * @param rejectionReason  دلیل رد آگهی (در صورت وجود)
     */
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
