package com.example.secondhandfx.controller;

import com.example.secondhandfx.model.AdvertisementResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * <h2>MyAdvertisementCardController</h2>
 * <p>
 * کنترلر کارت نمایش یکی از <b>آگهی‌های خود کاربر</b> در صفحه‌ی «آگهی‌های من».
 * برخلاف {@link AdvertisementCardController} (که برای نمایش عمومی آگهی‌هاست)،
 * این کارت شامل دکمه‌های عملیاتی مالک آگهی (مشاهده، ویرایش، فروخته‌شده
 * علامت زدن، حذف) است که نمایان بودنشان بر اساس وضعیت فعلی آگهی تعیین
 * می‌شود، و در صورت رد شدن آگهی، دلیل رد را نیز نمایش می‌دهد.
 * </p>
 *
 * @author تیم فرانت‌اند
 * @see com.example.secondhandfx.controller.MyAdvertisementsController
 */
public class MyAdvertisementCardController {

    @FXML private VBox root;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label statusBadgeLabel;
    @FXML private Button viewButton;
    @FXML private Button editButton;
    @FXML private Button markAsSoldButton;
    @FXML private Button deleteButton;
    @FXML private Label rejectionReasonLabel;

    private Runnable onView;
    private Runnable onEdit;
    private Runnable onMarkAsSold;
    private Runnable onDelete;

    /**
     * مقداردهی محتوای کارت بر اساس اطلاعات یک آگهی متعلق به کاربر جاری.
     * <p>
     * علاوه بر تنظیم عنوان و قیمت، نشان وضعیت، نمایان بودن دکمه‌های عملیاتی
     * (از طریق {@link #applyActionVisibility(String)}) و دلیل رد آگهی (از
     * طریق {@link #applyRejectionReason(String, String)}) نیز به‌روزرسانی
     * می‌شوند. در صورت بروز هرگونه خطای غیرمنتظره، خطا در کنسول ثبت می‌شود
     * تا از خرابی کل فهرست جلوگیری شود.
     * </p>
     *
     * @param ad داده‌ی آگهی که باید در کارت نمایش داده شود
     */
    public void setData(AdvertisementResponse ad) {
        try {
            titleLabel.setText(ad.getTitle() != null ? ad.getTitle() : "بدون عنوان");
            priceLabel.setText((ad.getPrice() != null ? formatPrice(ad.getPrice()) : "۰") + " تومان");

            applyStatusBadge(ad.getStatus());
            applyActionVisibility(ad.getStatus());
            applyRejectionReason(ad.getStatus(), ad.getRejectionReason());

            System.out.println("✅ کارت برای آگهی ID: " + ad.getId() + " ساخته شد.");
        } catch (Exception e) {
            System.err.println("❌ خطا در setData برای آگهی ID: " + ad.getId());
            e.printStackTrace();
        }
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
     * ثبت متد اجراشونده هنگام کلیک روی دکمه‌ی «ویرایش».
     *
     * @param handler عملیاتی که باید هنگام کلیک روی دکمه‌ی ویرایش اجرا شود
     */
    public void setOnEdit(Runnable handler) {
        this.onEdit = handler;
    }

    /**
     * ثبت متد اجراشونده هنگام کلیک روی دکمه‌ی «فروخته‌شد».
     *
     * @param handler عملیاتی که باید هنگام کلیک روی دکمه‌ی فروخته‌شدن اجرا شود
     */
    public void setOnMarkAsSold(Runnable handler) {
        this.onMarkAsSold = handler;
    }

    /**
     * ثبت متد اجراشونده هنگام کلیک روی دکمه‌ی «حذف».
     *
     * @param handler عملیاتی که باید هنگام کلیک روی دکمه‌ی حذف اجرا شود
     */
    public void setOnDelete(Runnable handler) {
        this.onDelete = handler;
    }

    /**
     * تنظیم متن و کلاس استایل نشان (Badge) وضعیت آگهی، بر اساس مقدار وضعیت.
     *
     * @param status مقدار خام وضعیت آگهی؛ در صورت {@code null} بودن، وضعیت «نامشخص» نمایش داده می‌شود
     */
    private void applyStatusBadge(String status) {
        String text;
        String styleClass;
        if (status == null) {
            text = "نامشخص";
            styleClass = "status-deleted";
        } else {
            switch (status) {
                case "APPROVED":
                    text = "فعال";
                    styleClass = "status-approved";
                    break;
                case "PENDING":
                    text = "در انتظار بررسی";
                    styleClass = "status-pending";
                    break;
                case "REJECTED":
                    text = "رد شده";
                    styleClass = "status-rejected";
                    break;
                case "SOLD":
                    text = "فروخته‌شده";
                    styleClass = "status-sold";
                    break;
                case "DELETED":
                    text = "حذف‌شده";
                    styleClass = "status-deleted";
                    break;
                default:
                    text = status;
                    styleClass = "status-deleted";
                    break;
            }
        }
        statusBadgeLabel.setText(text);
        statusBadgeLabel.getStyleClass().removeIf(c -> c.startsWith("status-") && !c.equals("status-badge"));
        statusBadgeLabel.getStyleClass().add(styleClass);
    }

    /**
     * تعیین نمایان و فعال بودن دکمه‌های عملیاتی (ویرایش، فروخته‌شد، حذف) بر
     * اساس وضعیت فعلی آگهی.
     * <p>
     * دکمه‌ی ویرایش برای آگهی‌های حذف‌شده یا فروخته‌شده مخفی می‌شود، دکمه‌ی
     * فروخته‌شد فقط برای آگهی‌های تاییدشده نمایش داده می‌شود، و دکمه‌ی حذف
     * برای آگهی‌های حذف‌شده مخفی می‌شود.
     * </p>
     *
     * @param status مقدار خام وضعیت فعلی آگهی
     */
    private void applyActionVisibility(String status) {
        boolean isDeleted = "DELETED".equals(status);
        boolean isSold = "SOLD".equals(status);
        boolean isApproved = "APPROVED".equals(status);

        editButton.setVisible(!isDeleted && !isSold);
        editButton.setManaged(!isDeleted && !isSold);

        markAsSoldButton.setVisible(isApproved);
        markAsSoldButton.setManaged(isApproved);

        deleteButton.setVisible(!isDeleted);
        deleteButton.setManaged(!isDeleted);
    }

    /**
     * پردازش کلیک روی دکمه‌ی «مشاهده»؛ در صورت ثبت‌شدن یک handler، آن را اجرا می‌کند.
     */
    @FXML
    private void onViewClick() {
        if (onView != null) onView.run();
    }

    /**
     * پردازش کلیک روی دکمه‌ی «ویرایش»؛ در صورت ثبت‌شدن یک handler، آن را اجرا می‌کند.
     */
    @FXML
    private void onEditClick() {
        if (onEdit != null) onEdit.run();
    }

    /**
     * پردازش کلیک روی دکمه‌ی «فروخته‌شد»؛ در صورت ثبت‌شدن یک handler، آن را اجرا می‌کند.
     */
    @FXML
    private void onMarkAsSoldClick() {
        if (onMarkAsSold != null) onMarkAsSold.run();
    }

    /**
     * پردازش کلیک روی دکمه‌ی «حذف»؛ در صورت ثبت‌شدن یک handler، آن را اجرا می‌کند.
     */
    @FXML
    private void onDeleteClick() {
        if (onDelete != null) onDelete.run();
    }

    /**
     * قالب‌بندی مقدار قیمت برای نمایش خوانا با جداکننده‌ی هزارگان.
     *
     * @param price مقدار قیمت؛ می‌تواند {@code null} باشد
     * @return رشته‌ی قیمت قالب‌بندی‌شده، یا {@code "۰"} در صورت {@code null} بودن مقدار ورودی
     */
    private String formatPrice(Long price) {
        if (price == null) return "۰";
        return NumberFormat.getNumberInstance(Locale.US).format(price);
    }

    /**
     * نمایش یا مخفی‌سازی برچسب دلیل رد آگهی، بر اساس وضعیت آگهی و موجود
     * بودن دلیل رد.
     * <p>
     * این برچسب فقط در صورتی نمایش داده می‌شود که وضعیت آگهی {@code REJECTED}
     * باشد و مقدار {@code rejectionReason} خالی یا {@code null} نباشد.
     * </p>
     *
     * @param status           مقدار خام وضعیت فعلی آگهی
     * @param rejectionReason  دلیل رد آگهی (در صورت وجود)
     */
    private void applyRejectionReason(String status, String rejectionReason) {
        boolean shouldShow = "REJECTED".equals(status)
                && rejectionReason != null && !rejectionReason.isBlank();

        rejectionReasonLabel.setVisible(shouldShow);
        rejectionReasonLabel.setManaged(shouldShow);

        if (shouldShow) {
            rejectionReasonLabel.setText("دلیل رد: " + rejectionReason);
        }
    }
}
