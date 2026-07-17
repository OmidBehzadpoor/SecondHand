package com.example.secondhand.service;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.exception.CategoryHasChildrenException;
import com.example.secondhand.exception.CategoryInUseException;
import com.example.secondhand.exception.CategoryNotFoundException;
import com.example.secondhand.model.Category;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .map(category -> mapToResponse(category, true))
                .toList();
    }

    private CategoryResponse mapToResponse(Category category, boolean includeChildren) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .subCategories(includeChildren
                        ? category.getChildren().stream()
                            .map(child -> mapToResponse(child, true))
                            .toList()
                        : null)
                .build();
    }
}