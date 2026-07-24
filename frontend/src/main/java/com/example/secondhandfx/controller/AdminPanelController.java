package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdminAdvertisementResponse;
import com.example.secondhandfx.model.AdminDashboardResponse;
import com.example.secondhandfx.model.AdminUserResponse;
import com.example.secondhandfx.model.CategoryResponse;
import com.example.secondhandfx.model.CityResponse;
import com.example.secondhandfx.service.*;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.ThemeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2>AdminPanelController</h2>
 * <p>
 * کنترلر <b>پنل مدیریت (ادمین)</b>، شامل چند تب مجزا برای:
 * </p>
 * <ul>
 *   <li><b>نمای کلی</b>: کارت‌های آماری خلاصه‌ی سامانه</li>
 *   <li><b>آگهی‌های در انتظار بررسی</b>: مشاهده، تایید، رد و حذف</li>
 *   <li><b>همه‌ی آگهی‌ها</b>: مشاهده و حذف تمام آگهی‌های سامانه</li>
 *   <li><b>کاربران</b>: مشاهده لیست کاربران و مسدود/رفع مسدودیت آن‌ها</li>
 *   <li><b>دسته‌بندی‌ها و شهرها</b>: ایجاد، ویرایش، فعال/غیرفعال‌سازی و حذف</li>
 * </ul>
 * <p>
 * هر بخش داده‌های خود را به‌صورت غیرهمزمان (با {@link Task}) از سرویس مربوطه
 * دریافت کرده و در جدول‌ها ({@link TableView}) نمایش می‌دهد.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.AdminService
 */
public class AdminPanelController {

    @FXML
    private FlowPane statsContainer;

    @FXML
    private TableView<AdminAdvertisementResponse> pendingAdsTable;
    @FXML
    private TableColumn<AdminAdvertisementResponse, String> adTitleColumn;
    @FXML
    private TableColumn<AdminAdvertisementResponse, String> adSellerColumn;
    @FXML
    private TableColumn<AdminAdvertisementResponse, String> adPriceColumn;
    @FXML
    private TableColumn<AdminAdvertisementResponse, Void> adActionsColumn;
    @FXML
    private TableView<AdminAdvertisementResponse> allAdsTable;
    @FXML
    private TableColumn<AdminAdvertisementResponse, String> allAdTitleColumn;
    @FXML
    private TableColumn<AdminAdvertisementResponse, String> allAdSellerColumn;
    @FXML
    private TableColumn<AdminAdvertisementResponse, String> allAdPriceColumn;
    @FXML
    private TableColumn<AdminAdvertisementResponse, String> allAdStatusColumn;
    @FXML
    private TableColumn<AdminAdvertisementResponse, Void> allAdActionsColumn;
    @FXML
    private TableView<AdminUserResponse> usersTable;
    @FXML
    private TableColumn<AdminUserResponse, String> userNameColumn;
    @FXML
    private TableColumn<AdminUserResponse, String> userRoleColumn;
    @FXML
    private TableColumn<AdminUserResponse, String> userStatusColumn;
    @FXML
    private TableColumn<AdminUserResponse, Void> userActionsColumn;
    @FXML
    private TabPane adminTabPane;

    @FXML
    private TextField newCategoryField;
    @FXML
    private ComboBox<CategoryResponse> parentCategoryComboBox;
    @FXML
    private TableView<CategoryResponse> categoriesTable;
    @FXML
    private TableColumn<CategoryResponse, String> categoryNameColumn;
    @FXML
    private TableColumn<CategoryResponse, Void> categoryActionsColumn;

    @FXML
    private TextField newCityField;
    @FXML
    private TableView<CityResponse> citiesTable;
    @FXML
    private TableColumn<CityResponse, String> cityNameColumn;
    @FXML
    private TableColumn<CityResponse, Void> cityActionsColumn;

    private final AdminService adminService = new AdminServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final CityService cityService = new CityServiceImpl();

    private final ObservableList<AdminAdvertisementResponse> pendingAds = FXCollections.observableArrayList();
    private final ObservableList<AdminUserResponse> users = FXCollections.observableArrayList();
    private final ObservableList<CategoryResponse> categories = FXCollections.observableArrayList();
    private final ObservableList<CityResponse> cities = FXCollections.observableArrayList();

    /**
     * مقداردهی اولیه‌ی پنل پس از بارگذاری FXML: تنظیم تمام جدول‌ها و
     * بارگذاری غیرهمزمان داده‌های اولیه‌ی هر تب (داشبورد، آگهی‌های در
     * انتظار، همه‌ی آگهی‌ها، کاربران، دسته‌بندی‌ها و شهرها).
     */
    @FXML
    public void initialize() {
        setupAdsTable();
        setupAllAdsTable();
        setupUsersTable();
        setupCategoriesTable();
        setupCitiesTable();

        loadDashboard();
        loadPendingAds();
        loadAllAds();
        loadUsers();
        loadCategories();
        loadCities();
    }

    /**
     * بارگذاری غیرهمزمان آمار کلی داشبورد از سرور و رندر آن پس از دریافت.
     */
    private void loadDashboard() {
        Task<AdminDashboardResponse> task = new Task<>() {
            @Override
            protected AdminDashboardResponse call() throws Exception {
                return adminService.getDashboard();
            }
        };
        task.setOnSucceeded(e -> renderDashboard(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    /**
     * ساخت و نمایش کارت‌های آماری تب «نمای کلی» بر اساس آمار دریافتی از سرور.
     *
     * @param stats آمار کلی سامانه
     */
    private void renderDashboard(AdminDashboardResponse stats) {
        statsContainer.getChildren().clear();
        statsContainer.getChildren().addAll(
                buildStatCard("کاربران", stats.getTotalUsers() + " (" + stats.getBlockedUsers() + " بلاک)"),
                buildStatCard("آگهی‌ها", String.valueOf(stats.getTotalAdvertisements())),
                buildStatCard("در انتظار بررسی", String.valueOf(stats.getPendingAdvertisements())),
                buildStatCard("تایید‌شده", String.valueOf(stats.getApprovedAdvertisements())),
                buildStatCard("رد‌شده", String.valueOf(stats.getRejectedAdvertisements())),
                buildStatCard("فروخته‌شده", String.valueOf(stats.getSoldAdvertisements())),
                buildStatCard("دسته‌بندی‌ها", String.valueOf(stats.getTotalCategories())),
                buildStatCard("شهرها", String.valueOf(stats.getTotalCities())),
                buildStatCard("گفتگوها", String.valueOf(stats.getTotalConversations())),
                buildStatCard("پیام‌ها", String.valueOf(stats.getTotalMessages())),
                buildStatCard("علاقه‌مندی‌ها", String.valueOf(stats.getTotalFavorites())),
                buildStatCard("امتیازها", String.valueOf(stats.getTotalRatings()))
        );
    }

    /**
     * ساخت یک کارت آماری واحد شامل مقدار و برچسب آن.
     *
     * @param label برچسب توصیفی کارت
     * @param value مقدار نمایشی کارت
     * @return {@link VBox} حاوی کارت آماری آماده برای افزودن به {@link #statsContainer}
     */
    private VBox buildStatCard(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(Double.MAX_VALUE);
        if (value != null && value.length() > 6) {
            valueLabel.getStyleClass().add("stat-value-compact");
        }
        Label titleLabel = new Label(label);
        titleLabel.getStyleClass().add("muted-label");
        titleLabel.setWrapText(true);

        VBox card = new VBox(6, valueLabel, titleLabel);
        card.setPrefWidth(170);
        card.setMinWidth(170);
        card.getStyleClass().add("stat-card");
        return card;
    }

    /**
     * تنظیم ستون‌های داده و ستون عملیات (مشاهده، تایید، رد، حذف) جدول
     * آگهی‌های در انتظار بررسی، و اتصال آن به {@link #pendingAds}.
     */
    private void setupAdsTable() {
        adTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        adSellerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSellerUsername()));
        adPriceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrice() + " تومان"));

        adActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("مشاهده");
            private final Button approveButton = new Button("تایید");
            private final Button rejectButton = new Button("رد");
            private final Button deleteButton = new Button("حذف");
            private final HBox box = new HBox(6, viewButton, approveButton, rejectButton, deleteButton);

            {
                viewButton.getStyleClass().addAll("btn", "btn-sm", "btn-outline");
                approveButton.getStyleClass().addAll("btn", "btn-sm", "btn-success");
                rejectButton.getStyleClass().addAll("btn", "btn-sm", "btn-warning");
                deleteButton.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
                box.getStyleClass().add("table-actions");

                viewButton.setOnAction(e -> onViewAdClick(getTableView().getItems().get(getIndex())));
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

    /**
     * پردازش کلیک روی دکمه‌ی «مشاهده» یک آگهی در تب آگهی‌های در انتظار:
     * هدایت به صفحه‌ی جزئیات با بازگشت به تب مربوطه‌ی پنل ادمین.
     *
     * @param ad آگهی‌ای که باید جزئیات آن مشاهده شود
     */
    private void onViewAdClick(AdminAdvertisementResponse ad) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(ad.getId());
        controller.setReturnPage("/com/example/secondhandfx/fxml/admin-panel.fxml");
        controller.setReturnTabIndex(1);
    }

    /**
     * بارگذاری غیرهمزمان لیست آگهی‌های در انتظار بررسی از سرور.
     */
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

    /**
     * پردازش کلیک روی دکمه‌ی بازگشت، و هدایت کاربر به صفحه‌ی آگهی‌ها.
     */
    @FXML
    private void onBackClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    /**
     * تایید غیرهمزمان یک آگهی در انتظار بررسی، و حذف آن از فهرست در انتظار
     * بررسی در صورت موفقیت.
     *
     * @param ad آگهی‌ای که باید تایید شود
     */
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

    /**
     * نمایش دیالوگ دریافت دلیل رد آگهی و، در صورت وارد کردن دلیل معتبر،
     * ارسال غیرهمزمان درخواست رد آگهی به سرور.
     *
     * @param ad آگهی‌ای که باید رد شود
     */
    private void onRejectClick(AdminAdvertisementResponse ad) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("رد آگهی");
        dialog.setHeaderText("دلیل رد آگهی را وارد کنید:");
        ThemeManager.applyTheme(dialog.getDialogPane());
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

    /**
     * حذف غیرهمزمان یک آگهی توسط ادمین، و حذف آن از فهرست‌های در انتظار
     * بررسی و همه‌ی آگهی‌ها در صورت موفقیت.
     *
     * @param ad آگهی‌ای که باید حذف شود
     */
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
            allAdsTable.getItems().remove(ad);
            AlertUtil.showSuccess("آگهی حذف شد.");
        });
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    /**
     * تنظیم ستون‌های داده و ستون عملیات (بلاک/آنبلاک) جدول کاربران، و اتصال
     * آن به {@link #users}.
     */
    private void setupUsersTable() {
        userNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        userRoleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        userStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserStatus()));
        userStatusColumn.setCellFactory(column -> createStatusPillCell());

        userActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button toggleButton = new Button();

            {
                toggleButton.getStyleClass().addAll("btn", "btn-sm");
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
                toggleButton.getStyleClass().removeAll("btn-success", "btn-danger");
                toggleButton.getStyleClass().add(isBlocked ? "btn-success" : "btn-danger");
                setGraphic(toggleButton);
            }
        });

        usersTable.setItems(users);
    }

    /**
     * سلول عمومی برای نمایش وضعیت به‌صورت بج رنگی (Status Pill) به‌جای متن ساده.
     */
    private <T> TableCell<T, String> createStatusPillCell() {
        return new TableCell<>() {
            private final Label pill = new Label();

            {
                pill.getStyleClass().add("status-pill");
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                pill.setText(translateStatus(status));
                pill.getStyleClass().removeIf(c -> c.startsWith("status-") && !c.equals("status-pill"));
                pill.getStyleClass().add("status-" + status.toLowerCase());
                setGraphic(pill);
            }
        };
    }

    /**
     * ترجمه‌ی مقدار خام یک وضعیت (آگهی یا کاربر) به متن نمایشی فارسی.
     *
     * @param status مقدار خام وضعیت
     * @return متن فارسی متناظر، یا خود مقدار ورودی در صورت عدم تطابق با موارد شناخته‌شده
     */
    private String translateStatus(String status) {
        return switch (status) {
            case "APPROVED" -> "تایید‌شده";
            case "PENDING" -> "در انتظار بررسی";
            case "REJECTED" -> "رد‌شده";
            case "DELETED" -> "حذف‌شده";
            case "SOLD" -> "فروخته‌شده";
            case "ACTIVE" -> "فعال";
            case "BLOCKED" -> "بلاک‌شده";
            default -> status;
        };
    }

    /**
     * بارگذاری غیرهمزمان لیست تمام کاربران سامانه از سرور.
     */
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

    /**
     * تغییر غیرهمزمان وضعیت مسدودیت یک کاربر (بلاک/آنبلاک، بسته به وضعیت
     * فعلی)، و به‌روزرسانی ردیف متناظر در جدول در صورت موفقیت.
     *
     * @param user کاربری که وضعیت مسدودیت او باید تغییر کند
     */
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

    /**
     * تنظیم ستون‌های داده و ستون عملیات (ویرایش، فعال/غیرفعال، حذف) جدول
     * دسته‌بندی‌ها، اتصال آن به {@link #categories}، و پیکربندی مبدل نمایشی
     * کمبوباکس انتخاب دسته‌بندی والد.
     */
    private void setupCategoriesTable() {
        categoryNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        categoryActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("ویرایش");
            private final Button toggleButton = new Button();
            private final Button deleteButton = new Button("حذف");
            private final HBox box = new HBox(6, editButton, toggleButton, deleteButton);

            {
                editButton.getStyleClass().addAll("btn", "btn-sm", "btn-outline");
                deleteButton.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
                box.getStyleClass().add("table-actions");

                editButton.setOnAction(e -> onEditCategoryClick(getTableView().getItems().get(getIndex())));
                toggleButton.setOnAction(e -> onToggleCategoryActiveClick(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> onDeleteCategoryClick(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                CategoryResponse category = getTableView().getItems().get(getIndex());
                boolean isActive = category.isActive();
                toggleButton.setText(isActive ? "غیرفعال" : "فعال");
                toggleButton.getStyleClass().removeAll("btn", "btn-sm", "btn-success", "btn-warning");
                toggleButton.getStyleClass().addAll("btn", "btn-sm", isActive ? "btn-warning" : "btn-success");
                setGraphic(box);
            }
        });

        categoriesTable.setItems(categories);

        parentCategoryComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(CategoryResponse category) {
                return category == null ? "بدون والد (ریشه)" : category.getName();
            }

            @Override
            public CategoryResponse fromString(String string) {
                return null;
            }
        });
    }

    /**
     * نمایش دیالوگ ویرایش نام و والد یک دسته‌بندی، و در صورت تایید کاربر،
     * ارسال غیرهمزمان درخواست به‌روزرسانی به سرور و بارگذاری مجدد فهرست
     * دسته‌بندی‌ها.
     *
     * @param category دسته‌بندی‌ای که باید ویرایش شود
     */
    private void onEditCategoryClick(CategoryResponse category) {
        TextField nameField = new TextField(category.getName());
        nameField.getStyleClass().add("input");

        ObservableList<CategoryResponse> parentOptions = FXCollections.observableArrayList();
        parentOptions.add(null); // «بدون والد (ریشه)» — همیشه قابل انتخاب، حتی بعد از انتخاب یک والد
        categories.stream()
                .filter(c -> !c.getId().equals(category.getId()))
                .forEach(parentOptions::add);

        ComboBox<CategoryResponse> parentBox = new ComboBox<>(parentOptions);
        parentBox.setMaxWidth(Double.MAX_VALUE);
        parentBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(CategoryResponse c) {
                return c == null ? "بدون والد (ریشه)" : c.getName();
            }

            @Override
            public CategoryResponse fromString(String string) {
                return null;
            }
        });
        CategoryResponse currentParent = categories.stream()
                .filter(c -> c.getId().equals(category.getParentId()))
                .findFirst()
                .orElse(null);
        parentBox.setValue(currentParent);

        Label nameLabel = new Label("نام دسته‌بندی");
        nameLabel.getStyleClass().add("field-label");
        Label parentLabel = new Label("دسته‌ی والد");
        parentLabel.getStyleClass().add("field-label");

        VBox content = new VBox(6, nameLabel, nameField, parentLabel, parentBox);
        content.getStyleClass().add("dialog-form");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("ویرایش دسته‌بندی");
        dialog.setHeaderText("ویرایش دسته‌بندی «" + category.getName() + "»");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ThemeManager.applyTheme(dialog.getDialogPane());

        dialog.showAndWait().filter(result -> result == ButtonType.OK).ifPresent(result -> {
            String newName = nameField.getText().trim();
            if (newName.isEmpty()) {
                AlertUtil.showError("نام دسته‌بندی نمی‌تواند خالی باشد.");
                return;
            }
            CategoryResponse selectedParent = parentBox.getValue();
            Long newParentId = selectedParent != null ? selectedParent.getId() : null;

            Task<CategoryResponse> task = new Task<>() {
                @Override
                protected CategoryResponse call() throws Exception {
                    return categoryService.updateCategory(category.getId(), newName, newParentId);
                }
            };
            task.setOnSucceeded(e -> loadCategories());
            task.setOnFailed(e -> showError(task.getException()));
            new Thread(task).start();
        });
    }

    /**
     * بارگذاری غیرهمزمان درخت دسته‌بندی‌ها از سرور، مسطح‌سازی آن برای نمایش
     * در جدول، و به‌روزرسانی گزینه‌های کمبوباکس انتخاب دسته‌بندی والد.
     */
    private void loadCategories() {
        Task<List<CategoryResponse>> task = new Task<>() {
            @Override
            protected List<CategoryResponse> call() throws Exception {
                return categoryService.getAllCategoriesForAdmin();
            }
        };
        task.setOnSucceeded(e -> {
            List<CategoryResponse> flattened = new ArrayList<>();
            flattenCategories(task.getValue(), flattened);
            categories.setAll(flattened);

            ObservableList<CategoryResponse> options = FXCollections.observableArrayList();
            options.add(null); // «بدون والد (ریشه)» — همیشه به‌عنوان اولین گزینه قابل انتخاب است
            options.addAll(flattened);
            parentCategoryComboBox.setItems(options);
        });
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    /**
     * مسطح‌سازی بازگشتی درخت دسته‌بندی‌ها در یک لیست خروجی (پیمایش عمقی).
     *
     * @param roots لیست دسته‌بندی‌های سطح فعلی (در ابتدا، ریشه‌ها)
     * @param out   لیست خروجی که دسته‌بندی‌های مسطح‌شده به آن افزوده می‌شوند
     */
    private void flattenCategories(List<CategoryResponse> roots, List<CategoryResponse> out) {
        for (CategoryResponse category : roots) {
            out.add(category);
            if (category.getSubCategories() != null) {
                flattenCategories(category.getSubCategories(), out);
            }
        }
    }

    /**
     * پردازش کلیک روی دکمه‌ی «افزودن» دسته‌بندی جدید: اعتبارسنجی نام، ارسال
     * غیرهمزمان درخواست ایجاد به سرور، و پاک‌سازی فرم و بارگذاری مجدد فهرست
     * در صورت موفقیت.
     */
    @FXML
    private void onAddCategoryClick() {
        String name = newCategoryField.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showError("نام دسته‌بندی نمی‌تواند خالی باشد.");
            return;
        }

        CategoryResponse selectedParent = parentCategoryComboBox.getValue();
        Long parentId = selectedParent != null ? selectedParent.getId() : null;

        Task<CategoryResponse> task = new Task<>() {
            @Override
            protected CategoryResponse call() throws Exception {
                return categoryService.createCategory(name, parentId);
            }
        };
        task.setOnSucceeded(e -> {
            newCategoryField.clear();
            parentCategoryComboBox.setValue(null);
            loadCategories();
        });
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    /**
     * تغییر غیرهمزمان وضعیت فعال/غیرفعال یک دسته‌بندی (بسته به وضعیت
     * فعلی)، و بارگذاری مجدد فهرست دسته‌بندی‌ها در صورت موفقیت.
     *
     * @param category دسته‌بندی‌ای که وضعیت فعال بودن آن باید تغییر کند
     */
    private void onToggleCategoryActiveClick(CategoryResponse category) {
        boolean isActive = category.isActive();
        Task<CategoryResponse> task = new Task<>() {
            @Override
            protected CategoryResponse call() throws Exception {
                return isActive
                        ? categoryService.deactivateCategory(category.getId())
                        : categoryService.activateCategory(category.getId());
            }
        };
        task.setOnSucceeded(e -> loadCategories());
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    /**
     * حذف غیرهمزمان یک دسته‌بندی، و بارگذاری مجدد فهرست دسته‌بندی‌ها در
     * صورت موفقیت.
     *
     * @param category دسته‌بندی‌ای که باید حذف شود
     */
    private void onDeleteCategoryClick(CategoryResponse category) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                categoryService.deleteCategory(category.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadCategories());
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    /**
     * تنظیم ستون‌های داده و ستون عملیات (حذف) جدول شهرها، و اتصال آن به
     * {@link #cities}.
     */
    private void setupCitiesTable() {
        cityNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        cityActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("حذف");

            {
                deleteButton.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
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

    /**
     * بارگذاری غیرهمزمان لیست شهرها از سرور.
     */
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

    /**
     * پردازش کلیک روی دکمه‌ی «افزودن» شهر جدید: اعتبارسنجی نام، ارسال
     * غیرهمزمان درخواست ایجاد به سرور، و افزودن مستقیم شهر تازه‌ایجادشده به
     * جدول به‌همراه پاک‌سازی فیلد ورودی، در صورت موفقیت.
     */
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

    /**
     * حذف غیرهمزمان یک شهر، و حذف آن از جدول در صورت موفقیت.
     *
     * @param city شهری که باید حذف شود
     */
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

    /**
     * تنظیم ستون‌های داده و ستون عملیات (مشاهده، حذف) جدول همه‌ی آگهی‌ها.
     * دکمه‌ی حذف برای آگهی‌های از قبل حذف‌شده غیرفعال می‌شود.
     */
    private void setupAllAdsTable() {
        allAdTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        allAdSellerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSellerUsername()));
        allAdPriceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPrice() + " تومان"));
        allAdStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        allAdActionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("مشاهده");
            private final Button deleteButton = new Button("حذف");
            private final HBox box = new HBox(5, viewButton, deleteButton);

            {
                viewButton.getStyleClass().addAll("btn", "btn-sm", "btn-outline");
                deleteButton.getStyleClass().addAll("btn", "btn-sm", "btn-danger");
                box.getStyleClass().add("table-actions");

                viewButton.setOnAction(e -> {
                    AdminAdvertisementResponse ad = getTableView().getItems().get(getIndex());
                    onViewAllAdClick(ad);
                });
                deleteButton.setOnAction(e -> {
                    AdminAdvertisementResponse ad = getTableView().getItems().get(getIndex());
                    if ("DELETED".equals(ad.getStatus())) {
                        AlertUtil.showError("این آگهی قبلاً حذف شده است.");
                        return;
                    }
                    onDeleteAdClick(ad);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                AdminAdvertisementResponse ad = getTableView().getItems().get(getIndex());
                deleteButton.setDisable("DELETED".equals(ad.getStatus()));
                setGraphic(box);
            }
        });

        allAdsTable.setItems(FXCollections.observableArrayList());
    }

    /**
     * بارگذاری غیرهمزمان لیست تمام آگهی‌های سامانه (بدون فیلتر وضعیت) از سرور.
     */
    private void loadAllAds() {
        Task<List<AdminAdvertisementResponse>> task = new Task<>() {
            @Override
            protected List<AdminAdvertisementResponse> call() throws Exception {
                return adminService.getAllAdvertisements();
            }
        };
        task.setOnSucceeded(e -> allAdsTable.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> showError(task.getException()));
        new Thread(task).start();
    }

    /**
     * پردازش کلیک روی دکمه‌ی «مشاهده» یک آگهی در تب همه‌ی آگهی‌ها: هدایت به
     * صفحه‌ی جزئیات با بازگشت به تب مربوطه‌ی پنل ادمین.
     *
     * @param ad آگهی‌ای که باید جزئیات آن مشاهده شود
     */
    private void onViewAllAdClick(AdminAdvertisementResponse ad) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(ad.getId());
        controller.setReturnPage("/com/example/secondhandfx/fxml/admin-panel.fxml");
        controller.setReturnTabIndex(2);
    }

    /**
     * نمایش پیام خطای مناسب برای یک {@link Throwable} پرتاب‌شده از یک
     * {@link Task} ناموفق، و ثبت جزئیات آن در کنسول.
     *
     * @param ex استثنای پرتاب‌شده
     */
    private void showError(Throwable ex) {
        ex.printStackTrace();
        String message = (ex instanceof ApiException) ? ex.getMessage() : "خطای ناشناخته‌ای رخ داد.";
        AlertUtil.showError(message);
    }

    /**
     * انتخاب یک تب مشخص در پنل ادمین بر اساس شاخص آن؛ عمدتاً هنگام بازگشت
     * از صفحه‌ی جزئیات آگهی به پنل ادمین استفاده می‌شود.
     *
     * @param index شاخص تب مورد نظر برای انتخاب
     */
    public void setSelectedTabIndex(int index) {
        if (adminTabPane != null) {
            adminTabPane.getSelectionModel().select(index);
        }
    }
}
