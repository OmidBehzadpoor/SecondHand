package com.example.secondhand.service;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.CategoryResponse;
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
        Category category = categoryRepository.save(Category.builder().name(request.getName()).build());

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی یافت نشد"));

        if (advertisementRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException("این دسته‌بندی در آگهی‌های فعال استفاده شده و قابل حذف نیست");
        }

        categoryRepository.delete(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .toList();
    }


}
