package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdminAdvertisementResponse;
import com.example.secondhandfx.model.AdminUserResponse;
import com.example.secondhandfx.service.AdminService;
import com.example.secondhandfx.service.AdminServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class AdminPanelController {

    @FXML private TableView<AdminAdvertisementResponse> pendingAdsTable;
    @FXML private TableColumn<AdminAdvertisementResponse, String> adTitleColumn;
    @FXML private TableColumn<AdminAdvertisementResponse, String> adSellerColumn;
    @FXML private TableColumn<AdminAdvertisementResponse, String> adPriceColumn;
    @FXML private TableColumn<AdminAdvertisementResponse, Void> adActionsColumn;

    @FXML private TableView<AdminUserResponse> usersTable;
    @FXML private TableColumn<AdminUserResponse, String> userNameColumn;
    @FXML private TableColumn<AdminUserResponse, String> userRoleColumn;
    @FXML private TableColumn<AdminUserResponse, String> userStatusColumn;
    @FXML private TableColumn<AdminUserResponse, Void> userActionsColumn;

    private final AdminService adminService = new AdminServiceImpl();

    private final ObservableList<AdminAdvertisementResponse> pendingAds = FXCollections.observableArrayList();
    private final ObservableList<AdminUserResponse> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupAdsTable();
        setupUsersTable();
        loadPendingAds();
        loadUsers();
    }

    private void setupAdsTable() {
        adTitleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTitle()));

        adSellerColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getSellerUsername()));

        adPriceColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPrice() + " تومان"));

        adActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button approveButton = new Button("تایید");
            private final Button rejectButton = new Button("رد");
            private final Button deleteButton = new Button("حذف");
            private final HBox box = new HBox(5, approveButton, rejectButton, deleteButton);

            {
                approveButton.setOnAction(event -> {
                    AdminAdvertisementResponse ad = getTableView().getItems().get(getIndex());
                    onApproveClick(ad);
                });
                rejectButton.setOnAction(event -> {
                    AdminAdvertisementResponse ad = getTableView().getItems().get(getIndex());
                    onRejectClick(ad);
                });
                deleteButton.setOnAction(event -> {
                    AdminAdvertisementResponse ad = getTableView().getItems().get(getIndex());
                    onDeleteClick(ad);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        pendingAdsTable.setItems(pendingAds);
    }

    private void setupUsersTable() {
        userNameColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername()));

        userRoleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRole().name()));

        userStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUserStatus()));

        userActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button toggleButton = new Button();

            {
                toggleButton.setOnAction(event -> {
                    AdminUserResponse user = getTableView().getItems().get(getIndex());
                    onToggleBlockClick(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                AdminUserResponse user = getTableView().getItems().get(getIndex());
                boolean isBlocked = "BLOCKED".equals(user.getUserStatus());
                toggleButton.setText(isBlocked ? "آنبلاک" : "بلاک");
                setGraphic(toggleButton);
            }
        });

        usersTable.setItems(users);
    }

    private void loadPendingAds() {
        Task<List<AdminAdvertisementResponse>> task = new Task<>() {
            @Override
            protected List<AdminAdvertisementResponse> call() throws Exception {
                return adminService.getPendingAdvertisements();
            }
        };
        task.setOnSucceeded(e -> pendingAds.setAll(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private void loadUsers() {
        Task<List<AdminUserResponse>> task = new Task<>() {
            @Override
            protected List<AdminUserResponse> call() throws Exception {
                return adminService.getAllUsers();
            }
        };
        task.setOnSucceeded(e -> users.setAll(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private void onApproveClick(AdminAdvertisementResponse ad) {
        Task<AdminAdvertisementResponse> task = new Task<>() {
            @Override
            protected AdminAdvertisementResponse call() throws Exception {
                return adminService.approveAdvertisement(ad.getId());
            }
        };
        task.setOnSucceeded(e -> {
            pendingAds.remove(ad);
            AlertUtil.showSuccess("آگهی تایید شد.");
        });
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private void onRejectClick(AdminAdvertisementResponse ad) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("دلیل رد آگهی را وارد کنید:");
        dialog.showAndWait().ifPresent(reason -> {
            if (reason.isBlank()) {
                AlertUtil.showError("دلیل رد نمی‌تواند خالی باشد.");
                return;
            }
            Task<AdminAdvertisementResponse> task = new Task<>() {
                @Override
                protected AdminAdvertisementResponse call() throws Exception {
                    return adminService.rejectAdvertisement(ad.getId(), reason);
                }
            };
            task.setOnSucceeded(e -> {
                pendingAds.remove(ad);
                AlertUtil.showSuccess("آگهی رد شد.");
            });
            task.setOnFailed(e -> showError(task.getException()));
            new Thread(task).start();
        });
    }

    private void onDeleteClick(AdminAdvertisementResponse ad) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                adminService.deleteAdvertisement(ad.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            pendingAds.remove(ad);
            AlertUtil.showSuccess("آگهی حذف شد.");
        });
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private void onToggleBlockClick(AdminUserResponse user) {
        boolean isBlocked = "BLOCKED".equals(user.getUserStatus());

        Task<AdminUserResponse> task = new Task<>() {
            @Override
            protected AdminUserResponse call() throws Exception {
                return isBlocked
                        ? adminService.unblockUser(user.getId())
                        : adminService.blockUser(user.getId());
            }
        };
        task.setOnSucceeded(e -> {
            int index = users.indexOf(user);
            users.set(index, task.getValue());
        });
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private void showError(Throwable ex) {
        ex.printStackTrace();
        String message = (ex instanceof ApiException)
                ? ex.getMessage()
                : "خطای ناشناخته‌ای رخ داد.";
        AlertUtil.showError(message);
    }
}