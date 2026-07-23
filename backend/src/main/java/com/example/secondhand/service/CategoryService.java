package com.example.secondhand.service;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.exception.*;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.Category;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <h2>CategoryService</h2>
 * <p>
 * سرویس مدیریت <b>دسته‌بندی‌های</b> سلسله‌مراتبی (درختی) آگهی‌ها. هر دسته‌بندی
 * می‌تواند یک والد و چندین فرزند داشته باشد.
 * </p>
 * <ul>
 *   <li>ایجاد، ویرایش و حذف دسته‌بندی‌ها با رعایت محدودیت‌های ساختار درختی</li>
 *   <li>جلوگیری از ایجاد <b>چرخه (Cycle)</b> در سلسله‌مراتب دسته‌بندی‌ها هنگام ویرایش والد</li>
 *   <li>فعال‌سازی و غیرفعال‌سازی دسته‌بندی‌ها با درنظرگرفتن آگهی‌های فعال یا در انتظار</li>
 *   <li>ممانعت از حذف دسته‌بندی‌هایی که دارای زیردسته یا آگهی فعال/در انتظار هستند</li>
 * </ul>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.Category
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AdvertisementRepository advertisementRepository;

    /**
     * ایجاد یک دسته‌بندی جدید، به‌صورت اختیاری زیرمجموعه یک دسته‌بندی والد.
     *
     * @param request اطلاعات دسته‌بندی جدید شامل نام و شناسه والد (اختیاری)
     * @return {@link CategoryResponse} حاوی اطلاعات دسته‌بندی تازه‌ایجادشده
     * @throws CategoryNotFoundException در صورتی که شناسه والد داده‌شده معتبر نباشد
     */
    public CategoryResponse create(CategoryRequest request) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی والد یافت نشد"));
        }

        Category category = categoryRepository.save(
                Category.builder()
                        .name(request.getName())
                        .parent(parent)
                        .build()
        );

        return mapToResponse(category, false);
    }

    /**
     * حذف یک دسته‌بندی از سامانه.
     * <p>
     * دسته‌بندی‌هایی که زیردسته دارند یا دارای آگهی فعال ({@code APPROVED})
     * یا در انتظار بررسی ({@code PENDING}) هستند، قابل حذف نیستند.
     * </p>
     *
     * @param id شناسه دسته‌بندی‌ای که باید حذف شود
     * @throws CategoryNotFoundException در صورتی که دسته‌بندی یافت نشود
     * @throws CategoryHasChildrenException در صورتی که دسته‌بندی دارای زیردسته باشد
     * @throws CategoryInUseException در صورتی که دسته‌بندی دارای آگهی فعال یا در انتظار باشد
     */
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        if (categoryRepository.existsByParentId(id)) {
            throw new CategoryHasChildrenException("این دسته‌بندی زیردسته دارد و قابل حذف نیست");
        }

        boolean hasActiveOrPendingAds = advertisementRepository.existsByCategoryIdAndStatusIn(
                id,
                List.of(AdvertisementStatus.APPROVED, AdvertisementStatus.PENDING)
        );
        if (hasActiveOrPendingAds) {
            throw new CategoryInUseException("این دسته‌بندی دارای آگهی فعال یا در انتظار است و قابل حذف نیست");
        }

        categoryRepository.delete(category);
    }

    /**
     * دریافت لیست دسته‌بندی‌های ریشه (بدون والد) که <b>فعال</b> هستند، به‌همراه
     * زیردسته‌های آن‌ها به‌صورت بازگشتی. مخصوص نمایش عمومی.
     *
     * @return لیستی از {@link CategoryResponse} شامل درخت کامل دسته‌بندی‌های فعال
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrue()
                .stream()
                .map(category -> mapToResponse(category, true))
                .toList();
    }

    /**
     * دریافت لیست تمام دسته‌بندی‌های ریشه (فعال و غیرفعال)، به‌همراه زیردسته‌های
     * آن‌ها به‌صورت بازگشتی. مخصوص پنل ادمین.
     *
     * @return لیستی از {@link CategoryResponse} شامل درخت کامل تمام دسته‌بندی‌ها
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesForAdmin() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(category -> mapToAdminResponse(category, true))
                .toList();
    }

    /**
     * فعال‌سازی یک دسته‌بندی غیرفعال.
     *
     * @param id شناسه دسته‌بندی‌ای که باید فعال شود
     * @return {@link CategoryResponse} حاوی اطلاعات به‌روزشده دسته‌بندی
     * @throws CategoryNotFoundException در صورتی که دسته‌بندی یافت نشود
     * @throws CategoryStateConflictException در صورتی که دسته‌بندی از قبل فعال باشد
     */
    @Transactional
    public CategoryResponse activate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        if (category.isActive()) {
            throw new CategoryStateConflictException("دسته‌بندی از قبل فعال است");
        }
        category.setActive(true);
        return mapToResponse(categoryRepository.save(category), false);
    }

    /**
     * غیرفعال‌سازی یک دسته‌بندی فعال.
     * <p>
     * در صورتی که خود دسته‌بندی یا هر یک از زیرمجموعه‌های آن دارای آگهی فعال
     * یا در انتظار بررسی باشند، امکان غیرفعال‌سازی وجود ندارد.
     * </p>
     *
     * @param id شناسه دسته‌بندی‌ای که باید غیرفعال شود
     * @return {@link CategoryResponse} حاوی اطلاعات به‌روزشده دسته‌بندی
     * @throws CategoryNotFoundException در صورتی که دسته‌بندی یافت نشود
     * @throws CategoryStateConflictException در صورتی که دسته‌بندی از قبل غیرفعال باشد
     * @throws CategoryInUseException در صورتی که دسته‌بندی یا زیرمجموعه‌های آن دارای
     *         آگهی فعال یا در انتظار باشند
     */
    @Transactional
    public CategoryResponse deactivate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        if (!category.isActive()) {
            throw new CategoryStateConflictException("دسته‌بندی از قبل غیرفعال است");
        }

        List<Long> allCategoryIds = collectCategoryAndDescendantIds(category);
        boolean hasActiveOrPendingAds = advertisementRepository.existsByCategoryIdInAndStatusIn(
                allCategoryIds,
                List.of(AdvertisementStatus.APPROVED, AdvertisementStatus.PENDING)
        );
        if (hasActiveOrPendingAds) {
            throw new CategoryInUseException("این دسته‌بندی یا زیرمجموعه‌های آن دارای آگهی فعال یا در انتظار هستند و نمی‌توان غیرفعال کرد.");
        }

        category.setActive(false);
        return mapToResponse(categoryRepository.save(category), false);
    }

    /**
     * جمع‌آوری بازگشتی شناسه‌ی یک دسته‌بندی و تمام زیردسته‌های آن در هر عمقی.
     *
     * @param category دسته‌بندی ریشه برای شروع جمع‌آوری
     * @return لیستی از شناسه‌های دسته‌بندی داده‌شده و تمام فرزندانش
     */
    private List<Long> collectCategoryAndDescendantIds(Category category) {
        List<Long> ids = new java.util.ArrayList<>();
        ids.add(category.getId());
        for (Category child : category.getChildren()) {
            ids.addAll(collectCategoryAndDescendantIds(child));
        }
        return ids;
    }

    /**
     * ویرایش نام و/یا والد یک دسته‌بندی موجود.
     * <p>
     * پیش از تغییر والد، بررسی می‌شود که این تغییر منجر به ایجاد چرخه در
     * ساختار درختی دسته‌بندی‌ها نشود.
     * </p>
     *
     * @param id      شناسه دسته‌بندی‌ای که باید ویرایش شود
     * @param request اطلاعات جدید شامل نام و شناسه والد جدید (اختیاری)
     * @return {@link CategoryResponse} حاوی اطلاعات به‌روزشده دسته‌بندی
     * @throws CategoryNotFoundException در صورتی که دسته‌بندی یا والد جدید یافت نشود
     * @throws InvalidCategoryHierarchyException در صورتی که تغییر والد منجر به
     *         ایجاد چرخه در سلسله‌مراتب دسته‌بندی شود
     */
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        Category newParent = null;
        if (request.getParentId() != null) {
            newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی والد یافت نشد"));
        }

        ensureNoCycle(category, newParent);

        category.setName(request.getName());
        category.setParent(newParent);

        return mapToResponse(categoryRepository.save(category), false);
    }

    /**
     * اطمینان از عدم ایجاد چرخه در سلسله‌مراتب دسته‌بندی‌ها هنگام تغییر والد.
     * <p>
     * این متد بررسی می‌کند که دسته‌بندی والد خودش نباشد و همچنین در مسیر
     * والدهای {@code newParent} به سمت بالا، خود دسته‌بندی {@code category}
     * قرار نگرفته باشد.
     * </p>
     *
     * @param category  دسته‌بندی‌ای که در حال ویرایش والد آن هستیم
     * @param newParent والد جدید پیشنهادی برای این دسته‌بندی
     * @throws InvalidCategoryHierarchyException در صورتی که والد جدید خود دسته‌بندی
     *         باشد یا منجر به ایجاد چرخه در سلسله‌مراتب شود
     */
    private void ensureNoCycle(Category category, Category newParent) {
        if (newParent == null) {
            return;
        }

        if (category.getId().equals(newParent.getId())) {
            throw new InvalidCategoryHierarchyException("دسته‌بندی نمی‌تواند والد خودش باشد");
        }

        Category current = newParent.getParent();
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new InvalidCategoryHierarchyException("این دسته‌بندی را نمی‌توان زیرمجموعه‌ی یکی از زیردسته‌های خودش قرار داد");            }
            current = current.getParent();
        }
    }

    /**
     * تبدیل شیء {@link Category} به DTO خروجی عمومی {@link CategoryResponse}.
     * <p>
     * در صورت درخواست شامل‌کردن زیردسته‌ها، فقط زیردسته‌های <b>فعال</b> در
     * خروجی قرار می‌گیرند.
     * </p>
     *
     * @param category         موجودیت دسته‌بندی
     * @param includeChildren  در صورت {@code true} بودن، زیردسته‌های فعال نیز
     *                         به‌صورت بازگشتی در خروجی قرار می‌گیرند
     * @return شیء {@link CategoryResponse} متناظر با دسته‌بندی
     */
    private CategoryResponse mapToResponse(Category category, boolean includeChildren) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .active(category.isActive())
                .subCategories(includeChildren
                        ? category.getChildren().stream()
                        .filter(Category::isActive)
                        .map(child -> mapToResponse(child, true))
                        .toList()
                        : null)
                .build();
    }

    /**
     * تبدیل شیء {@link Category} به DTO خروجی مخصوص ادمین {@link CategoryResponse}.
     * <p>
     * برخلاف {@link #mapToResponse(Category, boolean)}، در این نسخه تمام
     * زیردسته‌ها (فعال و غیرفعال) در خروجی قرار می‌گیرند.
     * </p>
     *
     * @param category         موجودیت دسته‌بندی
     * @param includeChildren  در صورت {@code true} بودن، تمام زیردسته‌ها به‌صورت
     *                         بازگشتی در خروجی قرار می‌گیرند
     * @return شیء {@link CategoryResponse} متناظر با دسته‌بندی، مخصوص نمایش ادمین
     */
    private CategoryResponse mapToAdminResponse(Category category, boolean includeChildren) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .active(category.isActive())
                .subCategories(includeChildren
                        ? category.getChildren().stream()
                        .map(child -> mapToAdminResponse(child, true))
                        .toList()
                        : null)
                .build();
    }
}
