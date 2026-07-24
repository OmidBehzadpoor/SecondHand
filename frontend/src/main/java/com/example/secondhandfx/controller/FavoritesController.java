package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.FavoriteResponse;
import com.example.secondhandfx.service.FavoriteService;
import com.example.secondhandfx.service.FavoriteServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * <h2>FavoritesController</h2>
 * <p>
 * کنترلر صفحه‌ی <b>علاقه‌مندی‌های</b> کاربر جاری. لیست آگهی‌های موردعلاقه را
 * از سرور دریافت کرده و برای هر کدام یک کارت {@link FavoriteCardController}
 * در یک {@link FlowPane} ایجاد می‌کند، و اقدامات مشاهده و حذف هر کارت را
 * مدیریت می‌کند.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.FavoriteService
 */
public class FavoritesController {

    @FXML private FlowPane favoritesContainer;

    private final FavoriteService favoriteService = new FavoriteServiceImpl();

    /**
     * مقداردهی اولیه‌ی صفحه پس از بارگذاری FXML: بارگذاری لیست علاقه‌مندی‌ها.
     */
    @FXML
    private void initialize() {
        loadFavorites();
    }

    /**
     * بارگذاری غیرهمزمان لیست آگهی‌های موردعلاقه‌ی کاربر جاری از سرور.
     */
    private void loadFavorites() {
        runAsync(favoriteService::getMyFavorites, this::render, "خطا در دریافت لیست علاقه‌مندی‌ها");
    }

    /**
     * ساخت و نمایش کارت‌های علاقه‌مندی در ظرف {@link #favoritesContainer}.
     * <p>
     * در صورت خالی بودن لیست، یک پیام راهنما نمایش داده می‌شود. برای هر
     * آگهی موردعلاقه، فایل FXML کارت بارگذاری شده، داده‌ی آن مقداردهی
     * می‌شود، و handler های مشاهده و حذف به {@link #openDetails(Long)} و
     * {@link #removeFavorite(Long)} متصل می‌شوند.
     * </p>
     *
     * @param favorites لیست آگهی‌های موردعلاقه‌ی دریافت‌شده از سرور
     */
    private void render(List<FavoriteResponse> favorites) {
        favoritesContainer.getChildren().clear();

        if (favorites.isEmpty()) {
            Label empty = new Label("هنوز آگهی‌ای به علاقه‌مندی‌ها اضافه نکرده‌اید.");
            empty.getStyleClass().add("muted-label");
            empty.setStyle("-fx-padding: 20;");
            favoritesContainer.getChildren().add(empty);
            return;
        }

        for (FavoriteResponse favorite : favorites) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/secondhandfx/fxml/favorite-card.fxml"));
                Parent card = loader.load();
                FavoriteCardController controller = loader.getController();
                controller.setData(favorite);
                controller.setOnView(() -> openDetails(favorite.getAdvertisementId()));
                controller.setOnRemove(() -> removeFavorite(favorite.getAdvertisementId()));
                favoritesContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * هدایت کاربر به صفحه‌ی جزئیات یک آگهی موردعلاقه.
     *
     * @param advertisementId شناسه آگهی مورد نظر
     */
    private void openDetails(Long advertisementId) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(advertisementId);
        controller.setReturnPage("/com/example/secondhandfx/fxml/favorites.fxml");
    }

    /**
     * حذف غیرهمزمان یک آگهی از علاقه‌مندی‌های کاربر جاری، و بارگذاری مجدد
     * فهرست در صورت موفقیت.
     *
     * @param advertisementId شناسه آگهی‌ای که باید از علاقه‌مندی‌ها حذف شود
     */
    private void removeFavorite(Long advertisementId) {
        runAsyncVoid(
                () -> favoriteService.removeFavorite(advertisementId),
                this::loadFavorites,
                "خطا در حذف از علاقه‌مندی‌ها"
        );
    }

    /**
     * پردازش کلیک روی دکمه‌ی بازگشت، و هدایت کاربر به صفحه‌ی آگهی‌ها.
     */
    @FXML
    private void onBackClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    /**
     * اجرای غیرهمزمان یک عملیات بازگرداننده‌ی مقدار (روی یک {@code Virtual Thread})
     * و بازگرداندن نتیجه یا نمایش خطای مناسب در نخ رابط کاربری.
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
}
