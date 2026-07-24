package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.FavoriteResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * <h2>FavoriteCardController</h2>
 * <p>
 * کنترلر کارت نمایش یک <b>آگهی موردعلاقه</b> در لیست علاقه‌مندی‌های کاربر.
 * این کنترلر معمولاً به‌عنوان یک FXML جزئی (partial) در داخل یک {@code ListView}
 * یا {@code FlowPane} بارگذاری می‌شود و کنترل رویدادهای مشاهده و حذف را به
 * کنترلر والد از طریق {@link Runnable} handler ها واگذار می‌کند.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.controller.FavoritesController
 */
public class FavoriteCardController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label cityLabel;
    @FXML private Label statusBadgeLabel;
    @FXML private Button viewButton;
    @FXML private Button removeButton;

    private Runnable onView;
    private Runnable onRemove;

    /**
     * مقداردهی محتوای کارت بر اساس اطلاعات یک آگهی موردعلاقه.
     *
     * @param favorite داده‌ی آگهی موردعلاقه که باید در کارت نمایش داده شود
     */
    public void setData(FavoriteResponse favorite) {
        titleLabel.setText(favorite.getAdvertisementTitle());
        priceLabel.setText(formatPrice(favorite.getPrice()) + " تومان");
        cityLabel.setText(favorite.getCityName());
        applyStatusBadge(favorite.getAdvertisementStatus());
    }

    /**
     * ثبت متد اجراشونده هنگام کلیک روی دکمه‌ی «مشاهده».
     *
     * @param handler عملیاتی که باید هنگام کلیک روی دکمه‌ی مشاهده اجرا شود
     */
    public void setOnView(Runnable handler) {
        this.onView = handler;
    }

    /**
     * ثبت متد اجراشونده هنگام کلیک روی دکمه‌ی «حذف».
     *
     * @param handler عملیاتی که باید هنگام کلیک روی دکمه‌ی حذف اجرا شود
     */
    public void setOnRemove(Runnable handler) {
        this.onRemove = handler;
    }

    /**
     * تنظیم متن و کلاس استایل نشان (Badge) وضعیت آگهی، بر اساس مقدار وضعیت.
     *
     * @param status مقدار خام وضعیت آگهی (مانند {@code "APPROVED"}, {@code "PENDING"} و غیره)
     */
    private void applyStatusBadge(String status) {
        String text;
        String styleClass;
        switch (status) {
            case "APPROVED" -> { text = "فعال"; styleClass = "status-approved"; }
            case "PENDING" -> { text = "در انتظار بررسی"; styleClass = "status-pending"; }
            case "REJECTED" -> { text = "رد شده"; styleClass = "status-rejected"; }
            case "SOLD" -> { text = "فروخته‌شده"; styleClass = "status-sold"; }
            case "DELETED" -> { text = "حذف‌شده توسط مدیر"; styleClass = "status-deleted"; }
            default -> { text = status; styleClass = "status-deleted"; }
        }
        statusBadgeLabel.setText(text);
        statusBadgeLabel.getStyleClass().removeIf(c -> c.startsWith("status-") && !c.equals("status-badge"));
        statusBadgeLabel.getStyleClass().add(styleClass);
    }

    /**
     * پردازش کلیک روی دکمه‌ی «مشاهده»؛ در صورت ثبت‌شدن یک handler، آن را اجرا می‌کند.
     */
    @FXML
    private void onViewClick() {
        if (onView != null) onView.run();
    }

    /**
     * پردازش کلیک روی دکمه‌ی «حذف»؛ در صورت ثبت‌شدن یک handler، آن را اجرا می‌کند.
     */
    @FXML
    private void onRemoveClick() {
        if (onRemove != null) onRemove.run();
    }

    /**
     * قالب‌بندی مقدار قیمت برای نمایش خوانا با جداکننده‌ی هزارگان.
     *
     * @param price مقدار قیمت؛ می‌تواند {@code null} باشد
     * @return رشته‌ی قیمت قالب‌بندی‌شده، یا {@code "-"} در صورت {@code null} بودن مقدار ورودی
     */
    private String formatPrice(Long price) {
        if (price == null) return "-";
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }
}
