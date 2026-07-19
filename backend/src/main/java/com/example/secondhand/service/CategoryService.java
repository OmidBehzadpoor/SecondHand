package com.example.secondhand.service;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.exception.*;
import com.example.secondhand.model.Category;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AdvertisementRepository advertisementRepository;

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

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        if (categoryRepository.existsByParentId(id)) {
            throw new CategoryHasChildrenException("این دسته‌بندی زیردسته دارد و قابل حذف نیست");
        }

        if (advertisementRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException("این دسته‌بندی در آگهی‌های فعال استفاده شده و قابل حذف نیست");
        }

        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrue()
                .stream()
                .map(category -> mapToResponse(category, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategoriesForAdmin() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(category -> mapToAdminResponse(category, true))
                .toList();
    }

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

    @Transactional
    public CategoryResponse deactivate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        if (!category.isActive()) {
            throw new CategoryStateConflictException("دسته‌بندی از قبل غیرفعال است");
        }
        category.setActive(false);
        return mapToResponse(categoryRepository.save(category), false);
    }

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