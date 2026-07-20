package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdminAdvertisementResponse;
import com.example.secondhandfx.model.AdminUserResponse;
import com.example.secondhandfx.model.CategoryResponse;
import com.example.secondhandfx.model.CityResponse;
import com.example.secondhandfx.service.*;
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

    @FXML private TextField newCategoryField;
    @FXML private TableView<CategoryResponse> categoriesTable;
    @FXML private TableColumn<CategoryResponse, String> categoryNameColumn;
    @FXML private TableColumn<CategoryResponse, Void> categoryActionsColumn;

    @FXML private TextField newCityField;
    @FXML private TableView<CityResponse> citiesTable;
    @FXML private TableColumn<CityResponse, String> cityNameColumn;
    @FXML private TableColumn<CityResponse, Void> cityActionsColumn;

    private final AdminService adminService = new AdminServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final CityService cityService = new CityServiceImpl();

    private final ObservableList<AdminAdvertisementResponse> pendingAds = FXCollections.observableArrayList();
    private final ObservableList<AdminUserResponse> users = FXCollections.observableArrayList();
    private final ObservableList<CategoryResponse> categories = FXCollections.observableArrayList();
    private final ObservableList<CityResponse> cities = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupAdsTable();
        setupUsersTable();
        setupCategoriesTable();
        setupCitiesTable();

        loadPendingAds();
        loadUsers();
        loadCategories();
        loadCities();
    }

    // ---------- آگهی‌ها ----------

    private void setupAdsTable() {
        adTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        adSellerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSellerUsername()));
        adPriceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrice() + " تومان"));

        adActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button approveButton = new Button("تایید");
            private final Button rejectButton = new Button("رد");
            private final Button deleteButton = new Button("حذف");
            private final HBox box = new HBox(5, approveButton, rejectButton, deleteButton);

            {
                approveButton.setOnAction(e -> onApproveClick(getTableView().getItems().get(getIndex())));
                rejectButton.setOnAction(e -> onRejectClick(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> onDeleteAdClick(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        pendingAdsTable.setItems(pendingAds);
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

    private void onDeleteAdClick(AdminAdvertisementResponse ad) {
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

    // ---------- کاربران ----------

    private void setupUsersTable() {
        userNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        userRoleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        userStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserStatus()));

        userActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button toggleButton = new Button();

            {
                toggleButton.setOnAction(e -> onToggleBlockClick(getTableView().getItems().get(getIndex())));
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

    private void onToggleBlockClick(AdminUserResponse user) {
        boolean isBlocked = "BLOCKED".equals(user.getUserStatus());
        Task<AdminUserResponse> task = new Task<>() {
            @Override
            protected AdminUserResponse call() throws Exception {
                return isBlocked ? adminService.unblockUser(user.getId()) : adminService.blockUser(user.getId());
            }
        };
        task.setOnSucceeded(e -> users.set(users.indexOf(user), task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    // ---------- دسته‌بندی‌ها ----------

    private void setupCategoriesTable() {
        categoryNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        categoryActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("حذف");

            {
                deleteButton.setOnAction(e -> onDeleteCategoryClick(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        categoriesTable.setItems(categories);
    }

    private void loadCategories() {
        Task<List<CategoryResponse>> task = new Task<>() {
            @Override
            protected List<CategoryResponse> call() throws Exception {
                return categoryService.getAllCategories();
            }
        };
        task.setOnSucceeded(e -> categories.setAll(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    @FXML
    private void onAddCategoryClick() {
        String name = newCategoryField.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showError("نام دسته‌بندی نمی‌تواند خالی باشد.");
            return;
        }
        Task<CategoryResponse> task = new Task<>() {
            @Override
            protected CategoryResponse call() throws Exception {
                return categoryService.createCategory(name);
            }
        };
        task.setOnSucceeded(e -> {
            categories.add(task.getValue());
            newCategoryField.clear();
        });
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private void onDeleteCategoryClick(CategoryResponse category) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                categoryService.deleteCategory(category.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> categories.remove(category));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    // ---------- شهرها ----------

    private void setupCitiesTable() {
        cityNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        cityActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("حذف");

            {
                deleteButton.setOnAction(e -> onDeleteCityClick(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        citiesTable.setItems(cities);
    }

    private void loadCities() {
        Task<List<CityResponse>> task = new Task<>() {
            @Override
            protected List<CityResponse> call() throws Exception {
                return cityService.getAllCities();
            }
        };
        task.setOnSucceeded(e -> cities.setAll(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    @FXML
    private void onAddCityClick() {
        String name = newCityField.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showError("نام شهر نمی‌تواند خالی باشد.");
            return;
        }
        Task<CityResponse> task = new Task<>() {
            @Override
            protected CityResponse call() throws Exception {
                return cityService.createCity(name);
            }
        };
        task.setOnSucceeded(e -> {
            cities.add(task.getValue());
            newCityField.clear();
        });
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    private void onDeleteCityClick(CityResponse city) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                cityService.deleteCity(city.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> cities.remove(city));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    // ---------- مشترک ----------

    private void showError(Throwable ex) {
        ex.printStackTrace();
        String message = (ex instanceof ApiException) ? ex.getMessage() : "خطای ناشناخته‌ای رخ داد.";
        AlertUtil.showError(message);
    }
}