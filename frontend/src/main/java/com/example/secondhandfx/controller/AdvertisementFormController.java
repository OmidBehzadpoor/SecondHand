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
import com.example.secondhandfx.util.SceneNavigator;
import com.example.secondhandfx.util.ValidationUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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

    // برای نمایش تورفته‌ی درخت دسته‌بندی، دقیقاً مثل صفحه‌ی اصلی
    private final Map<Long, Integer> categoryDepthMap = new HashMap<>();

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

        // نمایش زیردسته‌ها با تورفتگی واقعی (هماهنگ با صفحه‌ی اصلی)، نه با پیشوند خط‌تیره
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

        loadCategories();
        loadCities();
    }

    private void loadCategories() {
        runAsync(categoryService::getAllCategories, categories -> {
            categoryComboBox.getItems().addAll(flattenCategories(categories, 0));
        }, "خطا در دریافت دسته‌بندی‌ها");
    }

    // درخت دسته‌بندی‌ها را به یک لیست تخت تبدیل می‌کند و عمق هرکدام را برای نمایش تورفته ذخیره می‌کند
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

    private void loadCities() {
        runAsync(cityService::getAllCities, cities -> cityComboBox.getItems().addAll(cities),
                "خطا در دریافت شهرها");
    }

    // ====== تصاویر فعلی (آپلودشده‌ی قبلی، فقط در حالت ویرایش) ======

    private void setupExistingImagesListView() {
        existingImagesListView.setCellFactory(listView -> new ListCell<>() {
            private final Label urlLabel = new Label();
            private final Button deleteButton = new Button("حذف");
            private final HBox box = new HBox(10, urlLabel, deleteButton);

            {
                HBox.setHgrow(urlLabel, Priority.ALWAYS);
                deleteButton.getStyleClass().addAll("btn", "btn-danger");
                deleteButton.setOnAction(e -> onDeleteExistingImageClick(getItem()));
            }

            @Override
            protected void updateItem(AdvertisementImageResponse image, boolean empty) {
                super.updateItem(image, empty);
                if (empty || image == null) {
                    setGraphic(null);
                } else {
                    urlLabel.setText(fileNameOf(image.getImageUrl()));
                    setGraphic(box);
                }
            }
        });
    }

    private String fileNameOf(String imageUrl) {
        if (imageUrl == null) return "";
        int lastSlash = imageUrl.lastIndexOf('/');
        return lastSlash >= 0 ? imageUrl.substring(lastSlash + 1) : imageUrl;
    }

    private void loadExistingImages(Long advertisementId) {
        runAsync(
                () -> advertisementService.getImages(advertisementId),
                images -> existingImagesListView.setItems(FXCollections.observableArrayList(images)),
                "خطا در دریافت تصاویر آگهی"
        );
    }

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

    // ====== تصاویر جدید (برای آپلود) ======

    @FXML
    private void onAddImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("انتخاب تصویر آگهی");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("تصویر", "*.jpg", "*.jpeg", "*.png"));

        Window window = submitButton.getScene().getWindow();
        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files != null) {
            selectedImagesListView.getItems().addAll(files);
        }
    }

    @FXML
    private void onRemoveImageClick() {
        File selected = selectedImagesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedImagesListView.getItems().remove(selected);
        }
    }

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

    // بعد از ساخته‌شدن آگهی، تصویرهای جدید را یکی‌یکی آپلود می‌کند و در پایان کاربر را به صفحه‌ی جزئیات می‌فرستد
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

    private void openCreatedAdvertisement(Long id) {
        FXMLLoader loader = SceneNavigator.navigateTo(
                "/com/example/secondhandfx/fxml/advertisement-details.fxml", "جزئیات آگهی");
        AdvertisementDetailsController controller = loader.getController();
        controller.setAdvertisementId(id);
    }

    @FXML
    private void onCancelClick() {
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

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws ApiException;
    }

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

    private void populateFormForEdit(AdvertisementResponse ad) {
        titleField.setText(ad.getTitle());
        descriptionArea.setText(ad.getDescription());
        priceField.setText(String.valueOf(ad.getPrice()));

        // چون دیگر پیشوند خط‌تیره به نام دسته‌بندی اضافه نمی‌شود، مقایسه‌ی مستقیم کافی است
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