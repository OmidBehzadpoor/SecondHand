package com.example.secondhandfx.controller;

import com.example.secondhandfx.exception.ApiException;
import com.example.secondhandfx.model.AdvertisementImageResponse;
import com.example.secondhandfx.model.AdvertisementRequest;
import com.example.secondhandfx.model.AdvertisementResponse;
import com.example.secondhandfx.model.CategoryResponse;
import com.example.secondhandfx.model.CityResponse;
import com.example.secondhandfx.service.AdvertisementService;
import com.example.secondhandfx.service.AdvertisementServiceImpl;
import com.example.secondhandfx.service.CategoryService;
import com.example.secondhandfx.service.CategoryServiceImpl;
import com.example.secondhandfx.service.CityService;
import com.example.secondhandfx.service.CityServiceImpl;
import com.example.secondhandfx.util.AlertUtil;
import com.example.secondhandfx.util.Config;
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * <h2>AdvertisementFormController</h2>
 * <p>
 * کنترلر فرم <b>ایجاد یا ویرایش یک آگهی</b>. این کنترلر یک فرم واحد است که
 * هم برای ثبت آگهی جدید و هم برای ویرایش آگهی موجود استفاده می‌شود؛ حالت
 * فرم بر اساس فراخوانی یا عدم فراخوانی {@link #setAdvertisementId(Long)}
 * تعیین می‌شود. شامل انتخاب دسته‌بندی (با نمایش سلسله‌مراتبی درختی) و شهر،
 * و مدیریت تصاویر آگهی (تصاویر موجود و تصاویر تازه انتخاب‌شده برای آپلود) است.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.service.AdvertisementService
 */
public class AdvertisementFormController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<CategoryResponse> categoryComboBox;
    @FXML
    private ComboBox<CityResponse> cityComboBox;
    @FXML
    private VBox existingImagesSection;
    @FXML
    private ListView<AdvertisementImageResponse> existingImagesListView;
    @FXML
    private ListView<File> selectedImagesListView;
    @FXML
    private Button submitButton;
    @FXML
    private Label pageTitleLabel;

    private Long editingAdvertisementId;

    private final AdvertisementService advertisementService = new AdvertisementServiceImpl();
    private final CategoryService categoryService = new CategoryServiceImpl();
    private final CityService cityService = new CityServiceImpl();
    private static final int MAX_IMAGES_PER_ADVERTISEMENT = 6;

    private final Map<Long, Integer> categoryDepthMap = new HashMap<>();

    /**
     * مقداردهی اولیه‌ی فرم پس از بارگذاری FXML.
     * <p>
     * لیست تصاویر انتخاب‌شده مقداردهی می‌شود، بخش تصاویر موجود در حالت
     * پیش‌فرض (ایجاد آگهی جدید) مخفی می‌شود، مبدل‌های نمایشی (Converter) و
     * سلول‌های سفارشی برای کمبوباکس‌های دسته‌بندی و شهر تنظیم می‌شوند، و در
     * نهایت دسته‌بندی‌ها و شهرها از سرور بارگذاری می‌شوند.
     * </p>
     */
    @FXML
    private void initialize() {
        selectedImagesListView.setItems(FXCollections.observableArrayList());

        existingImagesSection.setVisible(false);
        existingImagesSection.setManaged(false);

        categoryComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(CategoryResponse category) {
                return category == null ? "" : category.getName();
            }

            @Override
            public CategoryResponse fromString(String string) {
                return null;
            }
        });

        cityComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(CityResponse city) {
                return city == null ? "" : city.getName();
            }

            @Override
            public CityResponse fromString(String string) {
                return null;
            }
        });

        categoryComboBox.setCellFactory(listView -> new ListCell<>() {
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

        setupExistingImagesListView();
        setupSelectedImagesListView();

        loadCategories();
        loadCities();
    }

    /**
     * بارگذاری غیرهمزمان لیست دسته‌بندی‌ها از سرور و افزودن نسخه‌ی
     * مسطح‌شده‌ی (Flattened) آن‌ها به کمبوباکس دسته‌بندی.
     */
    private void loadCategories() {
        runAsync(categoryService::getAllCategories, categories -> {
            categoryComboBox.getItems().addAll(flattenCategories(categories, 0));
        }, "خطا در دریافت دسته‌بندی‌ها");
    }

    /**
     * تبدیل بازگشتی درخت دسته‌بندی‌ها به یک لیست مسطح، به‌همراه ثبت عمق هر
     * دسته‌بندی در {@link #categoryDepthMap} برای استفاده در نمایش تورفتگی.
     *
     * @param categories لیست دسته‌بندی‌های سطح فعلی (در ابتدا، ریشه‌ها)
     * @param depth      عمق فعلی در درخت (ریشه = ۰)
     * @return لیست مسطح‌شده‌ی تمام دسته‌بندی‌ها به ترتیب پیمایش عمقی
     */
    private List<CategoryResponse> flattenCategories(List<CategoryResponse> categories, int depth) {
        List<CategoryResponse> result = new ArrayList<>();
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
     * بارگذاری غیرهمزمان لیست شهرها از سرور و افزودن آن‌ها به کمبوباکس شهر.
     */
    private void loadCities() {
        runAsync(cityService::getAllCities, cities -> cityComboBox.getItems().addAll(cities),
                "خطا در دریافت شهرها");
    }

    /**
     * تنظیم سلول سفارشی نمایش برای فهرست تصاویر موجود (متعلق به آگهی، در
     * حالت ویرایش)، شامل تصویر بندانگشتی، نام فایل، و دکمه‌ی حذف.
     */
    private void setupExistingImagesListView() {
        existingImagesListView.setCellFactory(listView -> new ListCell<>() {
            private final ImageView thumb = new ImageView();
            private final Label nameLabel = new Label();
            private final Button deleteButton = new Button("حذف");
            private final HBox box = new HBox(10, thumb, nameLabel, deleteButton);

            {
                thumb.setFitWidth(80);
                thumb.setFitHeight(80);
                thumb.setPreserveRatio(true);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                deleteButton.getStyleClass().addAll("btn", "btn-danger");
                deleteButton.setOnAction(e -> onDeleteExistingImageClick(getItem()));
            }

            @Override
            protected void updateItem(AdvertisementImageResponse image, boolean empty) {
                super.updateItem(image, empty);
                if (empty || image == null) {
                    setGraphic(null);
                } else {
                    String fullUrl = Config.getApiBaseUrl() + image.getImageUrl();
                    thumb.setImage(new Image(fullUrl, true));
                    nameLabel.setText(fileNameOf(image.getImageUrl()));
                    setGraphic(box);
                }
            }
        });
        existingImagesListView.setPrefHeight(120);
    }

    /**
     * تنظیم سلول سفارشی نمایش برای فهرست تصاویر تازه انتخاب‌شده (روی دیسک
     * محلی کاربر، هنوز آپلود نشده)، شامل پیش‌نمایش تصویر و نام فایل.
     */
    private void setupSelectedImagesListView() {
        selectedImagesListView.setCellFactory(listView -> new ListCell<>() {
            private final ImageView thumb = new ImageView();
            private final Label nameLabel = new Label();
            private final HBox box = new HBox(10, thumb, nameLabel);

            {
                thumb.setFitWidth(80);
                thumb.setFitHeight(80);
                thumb.setPreserveRatio(true);
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
            }

            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image(file.toURI().toString(), true);
                        thumb.setImage(image);
                    } catch (Exception e) {
                        thumb.setImage(null);
                    }
                    nameLabel.setText(file.getName());
                    setGraphic(box);
                }
            }
        });
        selectedImagesListView.setPrefHeight(120);
    }

    /**
     * استخراج نام فایل از انتهای یک آدرس (URL) تصویر.
     *
     * @param imageUrl آدرس کامل یا نسبی تصویر
     * @return بخش نام فایل، یا رشته‌ی خالی در صورت {@code null} بودن ورودی
     */
    private String fileNameOf(String imageUrl) {
        if (imageUrl == null) return "";
        int lastSlash = imageUrl.lastIndexOf('/');
        return lastSlash >= 0 ? imageUrl.substring(lastSlash + 1) : imageUrl;
    }

    /**
     * بارگذاری غیرهمزمان لیست تصاویر موجود یک آگهی (در حالت ویرایش) از سرور.
     *
     * @param advertisementId شناسه آگهی‌ای که تصاویر آن باید دریافت شود
     */
    private void loadExistingImages(Long advertisementId) {
        runAsync(
                () -> advertisementService.getImages(advertisementId),
                images -> existingImagesListView.setItems(FXCollections.observableArrayList(images)),
                "خطا در دریافت تصاویر آگهی"
        );
    }

    /**
     * پردازش کلیک روی دکمه‌ی حذف یک تصویر موجود: حذف تصویر از سرور و از
     * فهرست نمایشی، در صورت موفقیت.
     *
     * @param image تصویری که باید حذف شود
     */
    private void onDeleteExistingImageClick(AdvertisementImageResponse image) {
        if (image == null || editingAdvertisementId == null) {
            return;
        }

        runAsync(() -> {
            advertisementService.deleteImage(editingAdvertisementId, image.getId());
            return image;
        }, deleted -> {
            existingImagesListView.getItems().remove(deleted);
            AlertUtil.showSuccess("تصویر حذف شد.");
        }, "خطا در حذف تصویر");
    }

    /**
     * پردازش کلیک روی دکمه‌ی «افزودن تصویر»: باز کردن انتخابگر فایل برای
     * انتخاب یک یا چند تصویر، با بررسی عدم عبور از سقف مجاز
     * {@value #MAX_IMAGES_PER_ADVERTISEMENT} تصویر برای هر آگهی (با احتساب
     * تصاویر موجود و تصاویر قبلاً انتخاب‌شده).
     */
    @FXML
    private void onAddImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("انتخاب تصویر آگهی");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("تصویر", "*.jpg", "*.jpeg", "*.png"));

        Window window = submitButton.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files == null || files.isEmpty()) {
            return;
        }

        int existingCount = existingImagesListView.getItems() != null ? existingImagesListView.getItems().size() : 0;
        int selectedCount = selectedImagesListView.getItems().size();
        int currentTotal = existingCount + selectedCount;

        if (currentTotal + files.size() > MAX_IMAGES_PER_ADVERTISEMENT) {
            int remainingSlots = MAX_IMAGES_PER_ADVERTISEMENT - currentTotal;
            if (remainingSlots <= 0) {
                AlertUtil.showError("هر آگهی حداکثر می‌تواند " + MAX_IMAGES_PER_ADVERTISEMENT
                        + " تصویر داشته باشد؛ ظرفیت تصویر این آگهی پر شده است.");
            } else {
                AlertUtil.showError("هر آگهی حداکثر می‌تواند " + MAX_IMAGES_PER_ADVERTISEMENT
                        + " تصویر داشته باشد. شما در حال حاضر " + currentTotal
                        + " تصویر دارید و فقط می‌توانید " + remainingSlots + " تصویر دیگر اضافه کنید.");
            }
            return;
        }

        selectedImagesListView.getItems().addAll(files);
    }

    /**
     * پردازش کلیک روی دکمه‌ی «حذف تصویر انتخاب‌شده»: حذف تصویر انتخاب‌شده‌ی
     * فعلی از فهرست تصاویر در انتظار آپلود (بدون تماس با سرور).
     */
    @FXML
    private void onRemoveImageClick() {
        File selected = selectedImagesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedImagesListView.getItems().remove(selected);
        }
    }

    /**
     * پردازش کلیک روی دکمه‌ی ثبت/ذخیره‌ی فرم.
     * <p>
     * پس از اعتبارسنجی کامل فیلدها (خالی نبودن، انتخاب دسته‌بندی و شهر،
     * معتبر و مثبت بودن قیمت)، بسته به اینکه در حالت ویرایش باشیم یا ایجاد،
     * درخواست به‌روزرسانی یا ایجاد آگهی ارسال می‌شود و در صورت موفقیت،
     * آپلود تصاویر تازه‌انتخاب‌شده از طریق
     * {@link #uploadImagesThenNavigate(AdvertisementResponse, List)} آغاز می‌شود.
     * </p>
     */
    @FXML
    private void onSubmitClick() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String priceText = priceField.getText().trim();
        CategoryResponse category = categoryComboBox.getSelectionModel().getSelectedItem();
        CityResponse city = cityComboBox.getSelectionModel().getSelectedItem();

        if (ValidationUtil.isBlank(title) || ValidationUtil.isBlank(description) || ValidationUtil.isBlank(priceText)) {
            AlertUtil.showError("لطفاً همه‌ی فیلدهای اجباری را پر کنید.");
            return;
        }

        if (category == null) {
            AlertUtil.showError("لطفاً یک دسته‌بندی انتخاب کنید.");
            return;
        }

        if (city == null) {
            AlertUtil.showError("لطفاً یک شهر انتخاب کنید.");
            return;
        }

        Long price;
        try {
            price = Long.parseLong(priceText);
            if (price <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("قیمت باید یک عدد مثبت باشد.");
            return;
        }

        AdvertisementRequest request = AdvertisementRequest.builder()
                .title(title)
                .description(description)
                .price(price)
                .categoryId(category.getId())
                .cityId(city.getId())
                .build();

        List<File> imagesToUpload = new ArrayList<>(selectedImagesListView.getItems());
        submitButton.setDisable(true);

        if (editingAdvertisementId != null) {
            runAsync(
                    () -> advertisementService.update(editingAdvertisementId, request),
                    updatedAd -> uploadImagesThenNavigate(updatedAd, imagesToUpload),
                    "خطا در ویرایش آگهی"
            );
        } else {
            runAsync(
                    () -> advertisementService.create(request),
                    createdAd -> uploadImagesThenNavigate(createdAd, imagesToUpload),
                    "خطا در ثبت آگهی"
            );
        }
    }

    /**
     * آپلود پیوسته‌ی تمام تصاویر تازه‌انتخاب‌شده برای آگهی ایجاد/ویرایش‌شده،
     * و در پایان هدایت کاربر به صفحه‌ی جزئیات همان آگهی، همراه با پیام
     * مناسب بر اساس موفقیت کامل، موفقیت جزئی، یا شکست کامل آپلود تصاویر.
     *
     * @param createdAd آگهی‌ای که ایجاد یا ویرایش شده و تصاویر باید برای آن آپلود شوند
     * @param images    لیست فایل‌های محلی تصاویری که باید آپلود شوند
     */
    private void uploadImagesThenNavigate(AdvertisementResponse createdAd, List<File> images) {
        CompletableFuture.supplyAsync(() -> {
            List<String> failedFileNames = new ArrayList<>();
            for (File image : images) {
                try {
                    advertisementService.uploadImage(createdAd.getId(), image);
                } catch (ApiException e) {
                    failedFileNames.add(image.getName());
                }
            }
            return failedFileNames;
        }, Executors.newVirtualThreadPerTaskExecutor()).whenComplete((failedFileNames, throwable) -> {
            Platform.runLater(() -> {
                submitButton.setDisable(false);

                if (throwable != null) {
                    AlertUtil.showError("آگهی ثبت شد، اما در آپلود تصاویر مشکلی پیش آمد.");
                } else if (!failedFileNames.isEmpty()) {
                    AlertUtil.showError("آگهی ثبت شد، اما آپلود " + failedFileNames.size() + " تصویر ناموفق بود.");
                } else {
                    if (editingAdvertisementId != null) {
                        AlertUtil.showSuccess("آگهی با موفقیت ویرایش شد و پس از بررسی مدیر دوباره نمایش داده می‌شود.");
                    } else {
                        AlertUtil.showSuccess("آگهی با موفقیت ثبت شد و پس از بررسی مدیر نمایش داده می‌شود.");
                    }
                }

                openCreatedAdvertisement(createdAd.getId());
            });
        });
    }

    /**
     * هدایت کاربر به صفحه‌ی جزئیات آگهی‌ای که تازه ایجاد یا ویرایش شده است.
     *
     * @param id شناسه آگهی مورد نظر
     */
    private void openCreatedAdvertisement(Long id) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(id);
    }

    /**
     * پردازش کلیک روی دکمه‌ی «انصراف»: هدایت کاربر به صفحه‌ی آگهی‌ها بدون
     * ذخیره‌ی تغییرات فرم.
     */
    @FXML
    private void onCancelClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }

    // ====== متد جدید برای دکمه‌ی بازگشت ======
    /**
     * پردازش کلیک روی دکمه‌ی بازگشت: هدایت کاربر به صفحه‌ی آگهی‌ها.
     */
    @FXML
    private void onBackClick() {
        SceneNavigator.navigateTo("/com/example/secondhandfx/fxml/home.fxml", "آگهی‌ها");
    }
    // ========================================

    /**
     * اجرای غیرهمزمان یک عملیات بازگرداننده‌ی مقدار (روی یک {@code Virtual Thread})
     * و بازگرداندن نتیجه یا نمایش خطای مناسب در نخ رابط کاربری؛ در پایان
     * (چه موفق و چه ناموفق) دکمه‌ی ثبت دوباره فعال می‌شود.
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
                submitButton.setDisable(false);
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
     * تنظیم شناسه‌ی آگهی مورد ویرایش، تغییر فرم به حالت ویرایش (عنوان صفحه،
     * متن دکمه‌ی ثبت، نمایان‌سازی بخش تصاویر موجود)، و بارگذاری غیرهمزمان
     * اطلاعات و تصاویر فعلی آگهی برای پر کردن فرم.
     *
     * @param id شناسه آگهی‌ای که باید ویرایش شود
     */
    public void setAdvertisementId(Long id) {
        this.editingAdvertisementId = id;
        pageTitleLabel.setText("ویرایش آگهی");
        submitButton.setText("ذخیره‌ی تغییرات");

        existingImagesSection.setVisible(true);
        existingImagesSection.setManaged(true);

        runAsync(
                () -> advertisementService.getById(id),
                this::populateFormForEdit,
                "خطا در دریافت اطلاعات آگهی"
        );

        loadExistingImages(id);
    }

    /**
     * پر کردن فیلدهای فرم با اطلاعات آگهی موجود، در حالت ویرایش، شامل
     * انتخاب دسته‌بندی و شهر متناظر با نام‌های ذخیره‌شده در آگهی.
     *
     * @param ad داده‌ی آگهی‌ای که باید فرم بر اساس آن پر شود
     */
    private void populateFormForEdit(AdvertisementResponse ad) {
        titleField.setText(ad.getTitle());
        descriptionArea.setText(ad.getDescription());
        priceField.setText(String.valueOf(ad.getPrice()));

        CategoryResponse matchedCategory = categoryComboBox.getItems().stream()
                .filter(category -> category.getName().equals(ad.getCategoryName()))
                .findFirst()
                .orElse(null);
        if (matchedCategory != null) {
            categoryComboBox.getSelectionModel().select(matchedCategory);
        }

        CityResponse matchedCity = cityComboBox.getItems().stream()
                .filter(city -> city.getName().equals(ad.getCityName()))
                .findFirst()
                .orElse(null);
        if (matchedCity != null) {
            cityComboBox.getSelectionModel().select(matchedCity);
        }
    }
}
