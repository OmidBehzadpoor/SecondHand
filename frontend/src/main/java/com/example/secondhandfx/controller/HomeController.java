package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.model.CategoryResponse;
import com.example.secondhandfx.model.CityResponse;
import com.example.secondhandfx.model.PageResponse;
import com.example.secondhandfx.model.Role;
import com.example.secondhandfx.service.AdvertisementService;
import com.example.secondhandfx.service.AdvertisementServiceImpl;
import com.example.secondhandfx.service.CategoryService;
import com.example.secondhandfx.service.CategoryServiceImpl;
import com.example.secondhandfx.service.CityService;
import com.example.secondhandfx.service.CityServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.SessionManager;
import com.example.secondhandfx.util.ThemeManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * <h2>HomeController</h2>
 * <p>
 * کنترلر صفحه‌ی <b>خانه</b>، صفحه‌ی اصلی و ورودی سامانه که فهرست عمومی
 * آگهی‌ها را به‌صورت صفحه‌بندی‌شده (Paginated) و قابل جست‌وجو/فیلتر (بر اساس
 * کلمه کلیدی، دسته‌بندی، شهر و محدوده قیمت) نمایش می‌دهد. همچنین شامل ناحیه‌ی
 * احراز هویت (ورود/ثبت‌نام برای مهمان، یا منوی کاربر برای کاربر واردشده) و
 * یک ساید‌بار کشویی برای ناوبری سریع است.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.AdvertisementService
 */
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

    @FXML private HBox guestAuthBox;
    @FXML private HBox userAuthBox;
    @FXML private Label welcomeLabel;

    @FXML private Button conversationsButton;
    @FXML private Button adminPanelButton;
    @FXML private Button sidebarLogoutButton;

    @FXML private ComboBox<Integer> pageSizeComboBox;

    @FXML private Button sidebarToggleButton;
    @FXML private StackPane sidebarOverlay;
    @FXML private VBox sidebarPane;
    @FXML private Label sidebarWelcomeLabel;

    private boolean sidebarOpen = false;

    private final AdvertisementService advertisementService = new AdvertisementServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final CityService cityService = new CityServiceImpl();

    private int pageSize = 12;
    private int currentPage = 0;
    private int totalPages = 1;

    /**
     * مقداردهی اولیه‌ی صفحه پس از بارگذاری FXML.
     * <p>
     * ناحیه‌ی احراز هویت بر اساس وضعیت ورود کاربر تنظیم می‌شود، گزینه‌های
     * مرتب‌سازی و اندازه‌ی صفحه مقداردهی می‌شوند، دسته‌بندی‌ها/شهرها/آگهی‌ها
     * بارگذاری می‌شوند، ساید‌بار در حالت بسته و مخفی قرار می‌گیرد، و مبدل‌های
     * نمایشی (Converter) و سلول سفارشی کمبوباکس دسته‌بندی تنظیم می‌شوند.
     * </p>
     *
     * @param location  آدرس مورد استفاده برای تفکیک مسیرهای نسبی در فایل FXML (استفاده‌نشده)
     * @param resources منابع بین‌المللی‌سازی (استفاده‌نشده)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAuthArea();

        sortComboBox.getItems().addAll("جدیدترین", "قدیمی‌ترین", "ارزان‌ترین", "گران‌ترین");
        sortComboBox.getSelectionModel().selectFirst();

        pageSizeComboBox.getItems().addAll(12, 24, 48);
        pageSizeComboBox.getSelectionModel().select(Integer.valueOf(pageSize));
        pageSizeComboBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                return value == null ? "" : String.valueOf(value);
            }

            @Override
            public Integer fromString(String text) {
                if (text == null || text.isBlank()) {
                    return pageSize;
                }
                try {
                    int typed = Integer.parseInt(text.trim());
                    return Math.max(1, Math.min(typed, 500));
                } catch (NumberFormatException e) {
                    return pageSize;
                }
            }
        });
        pageSizeComboBox.setOnAction(event -> {
            Integer selected = pageSizeComboBox.getValue();
            if (selected == null) {
                return;
            }
            pageSize = selected;
            currentPage = 0;
            loadAdvertisements();
        });

        loadCategories();
        loadCities();
        loadAdvertisements();

        StackPane.setAlignment(sidebarPane, javafx.geometry.Pos.TOP_LEFT);
        sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
        sidebarPane.setVisible(false);
        sidebarOverlay.setVisible(false);
        sidebarOverlay.setManaged(false);

        categoryComboBox.setConverter(new javafx.util.StringConverter<CategoryResponse>() {
            @Override
            public String toString(CategoryResponse category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public CategoryResponse fromString(String string) {
                return null;
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

    /**
     * نمایان‌سازی ناحیه‌ی مناسب احراز هویت (مهمان یا کاربر واردشده) و
     * شخصی‌سازی پیام‌های خوش‌آمدگویی و نمایان بودن دکمه‌ی پنل ادمین، بر
     * اساس وضعیت ورود و نقش کاربر جاری.
     */
    private void setupAuthArea() {
        boolean loggedIn = SessionManager.getInstance().isLoggedIn();

        guestAuthBox.setVisible(!loggedIn);
        guestAuthBox.setManaged(!loggedIn);

        userAuthBox.setVisible(loggedIn);
        userAuthBox.setManaged(loggedIn);

        conversationsButton.setVisible(loggedIn);
        conversationsButton.setManaged(loggedIn);

        sidebarLogoutButton.setVisible(loggedIn);
        sidebarLogoutButton.setManaged(loggedIn);

        if (loggedIn) {
            String name = SessionManager.getInstance().getName();
            welcomeLabel.setText("سلام، " + name);
            sidebarWelcomeLabel.setText("خوش آمدید، " + name);

            boolean isAdmin = SessionManager.getInstance().getRole() == Role.ADMIN;
            adminPanelButton.setVisible(isAdmin);
            adminPanelButton.setManaged(isAdmin);
        } else {
            sidebarWelcomeLabel.setText("خوش آمدید!");
            adminPanelButton.setVisible(false);
            adminPanelButton.setManaged(false);
        }
    }

    /**
     * پردازش کلیک روی دکمه‌ی «☰ منو»: باز یا بسته کردن ساید‌بار با انیمیشن
     * کشویی، به‌همراه نمایان/مخفی کردن پوشش (Overlay) پس‌زمینه.
     */
    @FXML
    private void onToggleSidebarClick() {
        sidebarOpen = !sidebarOpen;

        if (sidebarOpen) {
            sidebarPane.setVisible(true);
            sidebarOverlay.setVisible(true);
            sidebarOverlay.setManaged(true);
            TranslateTransition transition = new TranslateTransition(Duration.millis(220), sidebarPane);
            transition.setToX(0);
            transition.play();
            sidebarToggleButton.setText("‹");
        } else {
            TranslateTransition transition = new TranslateTransition(Duration.millis(220), sidebarPane);
            transition.setToX(-sidebarPane.getPrefWidth());
            transition.setOnFinished(e -> {
                sidebarPane.setVisible(false);
                sidebarOverlay.setVisible(false);
                sidebarOverlay.setManaged(false);
            });
            transition.play();
            sidebarToggleButton.setText("☰");
        }
    }

    /**
     * پردازش کلیک روی پوشش (Overlay) پشت ساید‌بار: بستن ساید‌بار در صورتی
     * که کلیک دقیقاً روی خود پوشش (و نه محتوای داخل آن) رخ داده باشد.
     *
     * @param event رویداد کلیک ماوس
     */
    @FXML
    private void onOverlayClick(javafx.scene.input.MouseEvent event) {
        if (event.getTarget() == sidebarOverlay) {
            onToggleSidebarClick();
        }
    }

    /**
     * بستن ساید‌بار در صورت باز بودن؛ معمولاً پیش از ناوبری به صفحه‌ی دیگر فراخوانی می‌شود.
     */
    private void closeSidebarIfOpen() {
        if (sidebarOpen) {
            onToggleSidebarClick();
        }
    }

    /**
     * بارگذاری غیرهمزمان لیست دسته‌بندی‌ها از سرور، افزودن گزینه‌ی «همه
     * دسته‌بندی‌ها» به ابتدای لیست، و انتخاب آن به‌عنوان مقدار پیش‌فرض.
     */
    private void loadCategories() {
        runAsync(categoryService::getAllCategories, categories -> {
            CategoryResponse allOption = CategoryResponse.builder().id(null).name("همه دسته‌بندی‌ها").build();
            categoryComboBox.getItems().add(allOption);
            categoryComboBox.getItems().addAll(flattenCategories(categories, 0));
            categoryComboBox.getSelectionModel().select(allOption);
        }, "خطا در دریافت دسته‌بندی‌ها");
    }

    private final java.util.Map<Long, Integer> categoryDepthMap = new java.util.HashMap<>();

    /**
     * تبدیل بازگشتی درخت دسته‌بندی‌ها به یک لیست مسطح، به‌همراه ثبت عمق هر
     * دسته‌بندی در {@link #categoryDepthMap} برای استفاده در نمایش تورفتگی.
     *
     * @param categories لیست دسته‌بندی‌های سطح فعلی (در ابتدا، ریشه‌ها)
     * @param depth      عمق فعلی در درخت (ریشه = ۰)
     * @return لیست مسطح‌شده‌ی تمام دسته‌بندی‌ها به ترتیب پیمایش عمقی
     */
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

    /**
     * بارگذاری غیرهمزمان لیست شهرها از سرور، افزودن گزینه‌ی «همه شهرها» به
     * ابتدای لیست، و انتخاب آن به‌عنوان مقدار پیش‌فرض.
     */
    private void loadCities() {
        runAsync(cityService::getAllCities, cities -> {
            CityResponse allOption = CityResponse.builder().id(null).name("همه شهرها").build();
            cityComboBox.getItems().add(allOption);
            cityComboBox.getItems().addAll(cities);
            cityComboBox.getSelectionModel().select(allOption);
        }, "خطا در دریافت شهرها");
    }

    /**
     * پردازش کلیک روی دکمه‌ی «جست‌وجو»: بازگشت به صفحه‌ی اول و بارگذاری
     * مجدد آگهی‌ها بر اساس فیلترهای فعلی.
     */
    @FXML
    private void onSearchClick() {
        currentPage = 0;
        loadAdvertisements();
    }

    /**
     * پردازش کلیک روی دکمه‌ی صفحه‌ی قبلی: رفتن به صفحه‌ی قبل (در صورت وجود) و بارگذاری مجدد.
     */
    @FXML
    private void onPrevPageClick() {
        if (currentPage > 0) {
            currentPage--;
            loadAdvertisements();
        }
    }

    /**
     * پردازش کلیک روی دکمه‌ی صفحه‌ی بعدی: رفتن به صفحه‌ی بعد (در صورت وجود) و بارگذاری مجدد.
     */
    @FXML
    private void onNextPageClick() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadAdvertisements();
        }
    }

    /**
     * پردازش کلیک روی دکمه‌ی «ورود»، و هدایت کاربر به صفحه‌ی ورود.
     */
    @FXML
    private void onLoginClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
    }

    /**
     * پردازش کلیک روی دکمه‌ی «ثبت‌نام»، و هدایت کاربر به صفحه‌ی ثبت‌نام.
     */
    @FXML
    private void onRegisterClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/register.fxml", "ثبت‌نام");
    }

    /**
     * پردازش کلیک روی دکمه‌ی خروج از حساب کاربری: بستن ساید‌بار، پاک کردن
     * نشست جاری، و بازگشت به صفحه‌ی خانه.
     */
    @FXML
    private void onLogoutButtonClick() {
        closeSidebarIfOpen();
        SessionManager.getInstance().clearSession();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    // ====== متد اصلاح‌شده با استفاده از AlertUtil ======
    /**
     * پردازش کلیک روی دکمه‌ی «ثبت آگهی»: در صورت وارد نبودن کاربر، هدایت به
     * صفحه‌ی ورود؛ در غیر این صورت هدایت به فرم ثبت آگهی جدید. هرگونه خطای
     * غیرمنتظره نیز گرفته شده و پیام مناسب نمایش داده می‌شود.
     */
    @FXML
    private void onCreateAdvertisementClick() {
        System.out.println("onCreateAdvertisementClick called");
        try {
            closeSidebarIfOpen();
            if (!SessionManager.getInstance().isLoggedIn()) {
                AlertUtil.showError("لطفاً ابتدا وارد شوید.");
                SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/login.fxml", "ورود");
                return;
            }
            SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/advertisement-form.fxml", "ثبت آگهی جدید");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("خطا در بارگذاری صفحه: " + e.getMessage());
        }
    }
    // ========================================================

    /**
     * پردازش کلیک روی دکمه‌ی «آگهی‌های من»: بستن ساید‌بار و هدایت کاربر (در
     * صورت وارد بودن) به صفحه‌ی آگهی‌های خودش.
     */
    @FXML
    private void onMyAdvertisementsClick() {
        closeSidebarIfOpen();
        requireLoginThen(() ->
                SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/my-advertisements.fxml", "آگهی‌های من"));
    }

    /**
     * پردازش کلیک روی دکمه‌ی «علاقه‌مندی‌ها»: بستن ساید‌بار و هدایت کاربر
     * (در صورت وارد بودن) به صفحه‌ی علاقه‌مندی‌های خودش.
     */
    @FXML
    private void onFavoritesClick() {
        closeSidebarIfOpen();
        requireLoginThen(() ->
                SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/favorites.fxml", "علاقه‌مندی‌های من"));
    }

    /**
     * پردازش کلیک روی دکمه‌ی «گفت‌وگوها»: بستن ساید‌بار و هدایت کاربر (در
     * صورت وارد بودن) به صفحه‌ی گفت‌وگوهای خودش.
     */
    @FXML
    private void onConversationsClick() {
        closeSidebarIfOpen();
        requireLoginThen(() ->
                SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/conversation-list.fxml", "گفتگوها"));
    }

    /**
     * پردازش کلیک روی دکمه‌ی «پنل ادمین»: بستن ساید‌بار و هدایت کاربر به پنل مدیریت.
     */
    @FXML
    private void onAdminPanelClick() {
        closeSidebarIfOpen();
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/admin-panel.fxml", "پنل ادمین");
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
     * جمع‌آوری مقادیر فعلی فیلترها (کلمه کلیدی، دسته‌بندی، شهر، محدوده
     * قیمت، مرتب‌سازی) و بارگذاری غیرهمزمان صفحه‌ی متناظر از آگهی‌ها از سرور.
     */
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
                () -> advertisementService.getAll(keyword, categoryId, cityId, minPrice, maxPrice, sortBy, pageToLoad, pageSize),
                this::renderPage,
                "خطا در دریافت آگهی‌ها"
        );
    }

    /**
     * نمایش یک صفحه از نتایج آگهی‌ها: ساخت کارت برای هر آگهی، به‌روزرسانی
     * برچسب و دکمه‌های صفحه‌بندی، و نمایش پیام «آگهی‌ای یافت نشد» در صورت
     * خالی بودن نتایج.
     *
     * @param page صفحه‌ی نتایج دریافت‌شده از سرور
     */
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

    /**
     * هدایت کاربر به صفحه‌ی جزئیات یک آگهی، با مبدأ بازگشت به صفحه‌ی خانه.
     *
     * @param id شناسه آگهی مورد نظر
     */
    private void openAdvertisementDetails(Long id) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(id);
        controller.setReturnPage("/com/example/secondhandfx/fxml/home.fxml");
    }

    /**
     * تبدیل برچسب فارسی گزینه‌ی مرتب‌سازی انتخاب‌شده به مقدار مورد انتظار API.
     *
     * @param label برچسب فارسی نمایش‌داده‌شده در کمبوباکس مرتب‌سازی
     * @return کد مرتب‌سازی متناظر (مانند {@code "NEWEST"})، یا {@code null} در صورت عدم تطابق یا {@code null} بودن ورودی
     */
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

    /**
     * تبدیل ایمن یک رشته به {@link Long}، بدون پرتاب استثنا در صورت نامعتبر بودن.
     *
     * @param text مقدار متنی ورودی
     * @return مقدار {@link Long} تبدیل‌شده، یا {@code null} در صورت خالی بودن یا نامعتبر بودن ورودی
     */
    private Long parseLongOrNull(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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
     * پردازش کلیک روی دکمه‌ی تعویض تم: تغییر تم روشن/تاریک برنامه بر اساس
     * صحنه‌ی منبع رویداد.
     *
     * @param event رویداد کلیک که منبع آن برای دسترسی به صحنه‌ی جاری استفاده می‌شود
     */
    @FXML
    private void onToggleThemeClick(ActionEvent event) {
        Node source = (Node) event.getSource();
        ThemeManager.toggleTheme(source.getScene());
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
}
