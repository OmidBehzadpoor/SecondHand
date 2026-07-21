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

public class FavoritesController {

    @FXML private FlowPane favoritesContainer;

    private final FavoriteService favoriteService = new FavoriteServiceImpl();

    @FXML
    private void initialize() {
        loadFavorites();
    }

    private void loadFavorites() {
        runAsync(favoriteService::getMyFavorites, this::render, "خطا در دریافت لیست علاقه‌مندی‌ها");
    }

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

    private void openDetails(Long advertisementId) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(advertisementId);
    }

    private void removeFavorite(Long advertisementId) {
        runAsyncVoid(
                () -> favoriteService.removeFavorite(advertisementId),
                this::loadFavorites,
                "خطا در حذف از علاقه‌مندی‌ها"
        );
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