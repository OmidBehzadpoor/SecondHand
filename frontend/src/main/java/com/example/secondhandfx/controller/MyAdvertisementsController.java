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
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    @FXML
    private void initialize() {
        loadAdvertisements();
    }

    private void loadAdvertisements() {
        runAsync(advertisementService::getMyAdvertisements, this::renderAll, "خطا در دریافت آگهی‌های شما");
    }

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

    private List<AdvertisementResponse> filterByStatus(List<AdvertisementResponse> advertisements, String status) {
        return advertisements.stream()
                .filter(ad -> status.equals(ad.getStatus()))
                .collect(Collectors.toList());
    }

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
                controller.setOnDelete(() -> confirmAndDelete(ad.getId()));
                container.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openDetails(Long id) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(id);
        controller.setReturnPage("/com/example/secondhandfx/fxml/my-advertisements.fxml");
    }

    private void openEditForm(Long id) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-form.fxml", "ویرایش آگهی");
        AdvertisementFormController controller = loader.getController();
        controller.setAdvertisementId(id);
    }
    
    private void markAsSold(Long id) {
        runAsync(
                () -> advertisementService.markAsSold(id),
                response -> loadAdvertisements(),
                "خطا در تغییر وضعیت آگهی"
        );
    }

    private void confirmAndDelete(Long id) {
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

    @FXML
    private void onBackClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
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