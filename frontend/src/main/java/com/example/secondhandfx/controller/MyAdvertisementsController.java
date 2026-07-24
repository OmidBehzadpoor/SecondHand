package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.service.AdvertisementService;
import com.example.secondhandfx.service.AdvertisementServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * <h2>MyAdvertisementsController</h2>
 * <p>
 * کنترلر صفحه‌ی <b>آگهی‌های من</b>. تمام آگهی‌های متعلق به کاربر جاری را از
 * سرور دریافت کرده و بر اساس وضعیت (فعال، در انتظار بررسی، رد شده،
 * فروخته‌شده، حذف‌شده) در تب‌های جداگانه گروه‌بندی و نمایش می‌دهد. برای هر
 * آگهی، کارت {@link MyAdvertisementCardController} با دکمه‌های عملیاتی
 * (مشاهده، ویرایش، فروخته‌شده علامت زدن، حذف) ساخته می‌شود.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.AdvertisementService
 */
public class MyAdvertisementsController {

    @FXML
    private Tab approvedTab;
    @FXML
    private Tab pendingTab;
    @FXML
    private Tab rejectedTab;
    @FXML
    private Tab soldTab;
    @FXML
    private Tab deletedTab;

    @FXML
    private FlowPane approvedContainer;
    @FXML
    private FlowPane pendingContainer;
    @FXML
    private FlowPane rejectedContainer;
    @FXML
    private FlowPane soldContainer;
    @FXML
    private FlowPane deletedContainer;

    private final AdvertisementService advertisementService = new AdvertisementServiceImpl();

    /**
     * مقداردهی اولیه‌ی صفحه پس از بارگذاری FXML: بارگذاری آگهی‌های کاربر جاری.
     */
    @FXML
    private void initialize() {
        loadAdvertisements();
    }

    /**
     * بارگذاری غیرهمزمان تمام آگهی‌های کاربر جاری (بدون فیلتر وضعیت) از سرور.
     */
    private void loadAdvertisements() {
        runAsync(advertisementService::getMyAdvertisements, this::renderAll, "خطا در دریافت آگهی‌های شما");
    }

    /**
     * تفکیک لیست کامل آگهی‌ها بر اساس وضعیت و نمایش هر گروه در تب متناظر خودش.
     *
     * @param advertisements لیست کامل آگهی‌های کاربر جاری
     */
    private void renderAll(List<AdvertisementResponse> advertisements) {
        renderGroup(approvedContainer, approvedTab, "فعال",
                filterByStatus(advertisements, "APPROVED"));
        renderGroup(pendingContainer, pendingTab, "در انتظار بررسی",
                filterByStatus(advertisements, "PENDING"));
        renderGroup(rejectedContainer, rejectedTab, "رد شده",
                filterByStatus(advertisements, "REJECTED"));
        renderGroup(soldContainer, soldTab, "فروخته‌شده",
                filterByStatus(advertisements, "SOLD"));
        renderGroup(deletedContainer, deletedTab, "حذف‌شده",
                filterByStatus(advertisements, "DELETED"));
    }

    /**
     * فیلتر کردن لیست آگهی‌ها بر اساس یک وضعیت مشخص.
     *
     * @param advertisements لیست کامل آگهی‌ها
     * @param status         مقدار وضعیتی که باید فیلتر شود
     * @return لیستی شامل فقط آگهی‌های با وضعیت مشخص‌شده
     */
    private List<AdvertisementResponse> filterByStatus(List<AdvertisementResponse> advertisements, String status) {
        return advertisements.stream()
                .filter(ad -> status.equals(ad.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * ساخت و نمایش کارت‌های یک گروه از آگهی‌ها در ظرف مشخص‌شده، به‌همراه
     * به‌روزرسانی عنوان تب با تعداد آگهی‌های آن گروه.
     * <p>
     * در صورت خالی بودن گروه، پیام «آگهی‌ای در این وضعیت نیست» نمایش داده
     * می‌شود. در صورت بروز خطا هنگام ساخت کارت یک آگهی خاص، به‌جای متوقف
     * شدن کل نمایش، یک برچسب خطا برای همان آگهی نمایش داده می‌شود.
     * </p>
     *
     * @param container ظرف {@link FlowPane} مقصد برای افزودن کارت‌ها
     * @param tab       تب متناظر که عنوان آن باید با تعداد آگهی‌ها به‌روزرسانی شود
     * @param label     برچسب فارسی وضعیت (برای استفاده در عنوان تب)
     * @param ads       لیست آگهی‌های این گروه وضعیتی
     */
    private void renderGroup(FlowPane container, Tab tab, String label, List<AdvertisementResponse> ads) {
        container.getChildren().clear();
        tab.setText(label + " (" + ads.size() + ")");

        if (ads.isEmpty()) {
            javafx.scene.control.Label empty = new javafx.scene.control.Label("آگهی‌ای در این وضعیت نیست.");
            empty.getStyleClass().add("muted-label");
            empty.setStyle("-fx-padding: 20;");
            container.getChildren().add(empty);
            return;
        }

        for (AdvertisementResponse ad : ads) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/secondhandfx/fxml/my-advertisement-card.fxml"));
                Parent card = loader.load();
                MyAdvertisementCardController controller = loader.getController();
                controller.setData(ad);
                controller.setOnView(() -> openDetails(ad.getId()));
                controller.setOnEdit(() -> openEditForm(ad.getId()));
                controller.setOnMarkAsSold(() -> markAsSold(ad.getId()));
                controller.setOnDelete(() -> confirmAndDelete(ad.getId(), ad.getStatus()));
                container.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("❌ خطا در ساخت کارت برای آگهی ID: " + ad.getId());
                e.printStackTrace();
                // در صورت خطا، یک پیام خطا نمایش بده
                Label errorLabel = new Label("خطا در بارگذاری آگهی: " + ad.getTitle());
                errorLabel.setStyle("-fx-text-fill: red; -fx-padding: 5;");
                container.getChildren().add(errorLabel);
            }
        }
    }

    /**
     * هدایت کاربر به صفحه‌ی جزئیات یک آگهی، با مبدأ بازگشت به صفحه‌ی آگهی‌های من.
     *
     * @param id شناسه آگهی مورد نظر
     */
    private void openDetails(Long id) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(id);
        controller.setReturnPage("/com/example/secondhandfx/fxml/my-advertisements.fxml");
    }

    /**
     * هدایت کاربر به فرم آگهی در حالت ویرایش برای یک آگهی مشخص.
     *
     * @param id شناسه آگهی‌ای که باید ویرایش شود
     */
    private void openEditForm(Long id) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-form.fxml", "ویرایش آگهی");
        AdvertisementFormController controller = loader.getController();
        controller.setAdvertisementId(id);
    }
    
    /**
     * علامت‌گذاری غیرهمزمان یک آگهی به‌عنوان فروخته‌شده، و بارگذاری مجدد
     * فهرست آگهی‌ها در صورت موفقیت.
     *
     * @param id شناسه آگهی‌ای که باید فروخته‌شده علامت زده شود
     */
    private void markAsSold(Long id) {
        runAsync(
                () -> advertisementService.markAsSold(id),
                response -> loadAdvertisements(),
                "خطا در تغییر وضعیت آگهی"
        );
    }

    /**
     * نمایش دیالوگ تایید حذف یک آگهی و، در صورت تایید کاربر، حذف غیرهمزمان
     * آن از سرور.
     *
     * @param id            شناسه آگهی‌ای که باید حذف شود
     * @param currentStatus وضعیت فعلی آگهی؛ در صورتی که از قبل {@code DELETED} باشد، عملیات با پیام خطا متوقف می‌شود
     */
    private void confirmAndDelete(Long id, String currentStatus) {
        if ("DELETED".equals(currentStatus)) {
            AlertUtil.showError("این آگهی قبلاً حذف شده است.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "آیا از حذف این آگهی مطمئن هستید؟", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("تایید حذف آگهی");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            runAsyncVoid(
                    () -> advertisementService.delete(id),
                    this::loadAdvertisements,
                    "خطا در حذف آگهی"
            );
        }
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
