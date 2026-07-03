package com.example.secondhand.service;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.exception.CategoryNotFoundException;
import com.example.secondhand.model.Category;
import com.example.secondhand.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category create(CategoryRequest request) {
        Category category = Category.builder().name(request.getName()).build();
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("دسته‌بندی یافت نشد"));
        categoryRepository.delete(category);
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

}
