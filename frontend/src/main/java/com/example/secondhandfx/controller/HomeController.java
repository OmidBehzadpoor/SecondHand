package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.model.CategoryResponse;
import com.example.secondhandfx.model.CityResponse;
import com.example.secondhandfx.model.PageResponse;
import com.example.secondhandfx.service.AdvertisementService;
import com.example.secondhandfx.service.AdvertisementServiceImpl;
import com.example.secondhandfx.service.CategoryService;
import com.example.secondhandfx.service.CategoryServiceImpl;
import com.example.secondhandfx.service.CityService;
import com.example.secondhandfx.service.CityServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import com.example.secondhandfx.util.ThemeManager;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class HomeController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<CategoryResponse> categoryComboBox;
    @FXML private ComboBox<CityResponse> cityComboBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private FlowPane advertisementsContainer;
    @FXML private Label pageIndicatorLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;

    // ناحیه‌ی سمت راست بالای صفحه که بین حالت مهمون و حالت لاگین‌شده جابه‌جا می‌شود
    @FXML private HBox guestAuthBox;
    @FXML private HBox userAuthBox;
    @FXML private Label welcomeLabel;

    private final AdvertisementService advertisementService = new AdvertisementServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final CityService cityService = new CityServiceImpl();

    private int currentPage = 0;
    private static final int PAGE_SIZE = 12;
    private int totalPages = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAuthArea();

        sortComboBox.getItems().addAll("جدیدترین", "قدیمی‌ترین", "ارزان‌ترین", "گران‌ترین");
        sortComboBox.getSelectionModel().selectFirst();

        loadCategories();
        loadCities();
        loadAdvertisements();
        categoryComboBox.setConverter(new javafx.util.StringConverter<CategoryResponse>() {
            @Override
            public String toString(CategoryResponse category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public CategoryResponse fromString(String string) {
                return null; // فقط برای انتخاب از لیست استفاده می‌شود، نه تایپ آزاد
            }
        });

        cityComboBox.setConverter(new javafx.util.StringConverter<CityResponse>() {
            @Override
            public String toString(CityResponse city) {
                return city == null ? "" : city.getName();
            }

            @Override
            public CityResponse fromString(String string) {
                return null;
            }
        });

        categoryComboBox.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(CategoryResponse category, boolean empty) {
                super.updateItem(category, empty);
                getStyleClass().removeAll("category-root-item", "category-sub-item");
                if (empty || category == null) {
                    setText(null);
                } else {
                    int depth = categoryDepthMap.getOrDefault(category.getId(), 0);
                    setText(category.getName());
                    setStyle("-fx-padding: 4 4 4 " + (depth * 18) + "px;");
                    getStyleClass().add(depth == 0 ? "category-root-item" : "category-sub-item");
                }
            }
        });
    }

    // بر اساس اینکه کاربر لاگین کرده یا نه، یکی از دو باکس بالای صفحه رو نشون می‌ده
    private void setupAuthArea() {
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();

        guestAuthBox.setVisible(!loggedIn);
        guestAuthBox.setManaged(!loggedIn);

        userAuthBox.setVisible(loggedIn);
        userAuthBox.setManaged(loggedIn);

        if (loggedIn) {
            welcomeLabel.setText("سلام، " + SessionManager.getInstance().getUsername());
        }
    }

    private void loadCategories() {
        runAsync(categoryService::getAllCategories, categories -> {
            CategoryResponse allOption = CategoryResponse.builder().id(null).name("همه دسته‌بندی‌ها").build();
            categoryComboBox.getItems().add(allOption);
            categoryComboBox.getItems().addAll(flattenCategories(categories, 0));
            categoryComboBox.getSelectionModel().select(allOption);
        }, "خطا در دریافت دسته‌بندی‌ها");
    }

    // درخت دسته‌بندی‌ها را به یک لیست تخت با نمایش تورفته تبدیل می‌کند
// تا کاربر بتواند هم دسته‌ی والد و هم هر زیردسته را مستقیماً انتخاب کند
    private final java.util.Map<Long, Integer> categoryDepthMap = new java.util.HashMap<>();

    private List<CategoryResponse> flattenCategories(List<CategoryResponse> categories, int depth) {
        List<CategoryResponse> result = new java.util.ArrayList<>();
        for (CategoryResponse category : categories) {
            categoryDepthMap.put(category.getId(), depth);
            result.add(category);

            if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
                result.addAll(flattenCategories(category.getSubCategories(), depth + 1));
            }
        }
        return result;
    }

    private void loadCities() {
        runAsync(cityService::getAllCities, cities -> {
            CityResponse allOption = CityResponse.builder().id(null).name("همه شهرها").build();
            cityComboBox.getItems().add(allOption);
            cityComboBox.getItems().addAll(cities);
            cityComboBox.getSelectionModel().select(allOption);
        }, "خطا در دریافت شهرها");
    }

    @FXML
    private void onSearchClick() {
        currentPage = 0;
        loadAdvertisements();
    }

    @FXML
    private void onPrevPageClick() {
        if (currentPage > 0) {
            currentPage--;
            loadAdvertisements();
        }
    }

    @FXML
    private void onNextPageClick() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadAdvertisements();
        }
    }

    @FXML
    private void onLoginClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
    }

    @FXML
    private void onRegisterClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/register.fxml", "ثبت‌نام");
    }

    @FXML
    private void onLogoutButtonClick() {
        SessionManager.getInstance().clearSession();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    @FXML
    private void onCreateAdvertisementClick() {
        requireLoginThen(() ->
                SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/advertisement-form.fxml", "ثبت آگهی جدید"));
    }

    @FXML
    private void onMyAdvertisementsClick() {
        requireLoginThen(() ->
                SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/my-advertisements.fxml", "آگهی‌های من"));
    }

    @FXML
    private void onFavoritesClick() {
        requireLoginThen(() ->
                SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/favorites.fxml", "علاقه‌مندی‌های من"));
    }

    // اگه کاربر لاگین نکرده، به‌جای اجرای عملیات، می‌فرسته‌ش صفحه‌ی لاگین
    private void requireLoginThen(Runnable action) {
        if (!SessionManager.getInstance().isLoggedIn()) {
            AlertUtil.showError("برای این کار ابتدا باید وارد حساب کاربری خود شوید.");
            SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
            return;
        }
        action.run();
    }

    private void loadAdvertisements() {
        String keyword = searchField.getText();
        CategoryResponse selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        CityResponse selectedCity = cityComboBox.getSelectionModel().getSelectedItem();

        Long categoryId = selectedCategory != null ? selectedCategory.getId() : null;
        Long cityId = selectedCity != null ? selectedCity.getId() : null;
        Long minPrice = parseLongOrNull(minPriceField.getText());
        Long maxPrice = parseLongOrNull(maxPriceField.getText());
        String sortBy = mapSortOption(sortComboBox.getSelectionModel().getSelectedItem());

        int pageToLoad = currentPage;

        runAsync(
                () -> advertisementService.getAll(keyword, categoryId, cityId, minPrice, maxPrice, sortBy, pageToLoad, PAGE_SIZE),
                this::renderPage,
                "خطا در دریافت آگهی‌ها"
        );
    }

    private void renderPage(PageResponse<AdvertisementResponse> page) {
        advertisementsContainer.getChildren().clear();
        totalPages = Math.max(page.getTotalPages(), 1);

        for (AdvertisementResponse ad : page.getContent()) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/secondhandfx/fxml/advertisement-card.fxml"));
                Parent card = loader.load();
                AdvertisementCardController controller = loader.getController();
                controller.setData(ad);
                controller.setOnClickHandler(() -> openAdvertisementDetails(ad.getId()));
                advertisementsContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pageIndicatorLabel.setText((currentPage + 1) + " از " + totalPages);
        prevPageButton.setDisable(currentPage == 0);
        nextPageButton.setDisable(currentPage >= totalPages - 1);

        if (page.getContent().isEmpty()) {
            Label empty = new Label("آگهی‌ای یافت نشد.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            advertisementsContainer.getChildren().add(empty);
        }
    }

    private void openAdvertisementDetails(Long id) {
        // صفحه‌ی جزئیات آگهی بدون نیاز به لاگین باز می‌شه (طبق داک پروژه)
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(id);
    }

    private String mapSortOption(String label) {
        if (label == null) return null;
        return switch (label) {
            case "جدیدترین" -> "NEWEST";
            case "قدیمی‌ترین" -> "OLDEST";
            case "ارزان‌ترین" -> "PRICE_ASC";
            case "گران‌ترین" -> "PRICE_DESC";
            default -> null;
        };
    }

    private Long parseLongOrNull(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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

    @FXML
    private void onToggleThemeClick(ActionEvent event) {
        Node source = (Node) event.getSource();
        ThemeManager.toggleTheme(source.getScene());
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws ApiException;
    }
}