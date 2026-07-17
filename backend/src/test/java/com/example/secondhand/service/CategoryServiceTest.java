package com.example.secondhand.service;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.exception.CategoryInUseException;
import com.example.secondhand.exception.CategoryNotFoundException;
import com.example.secondhand.model.Category;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private CategoryService categoryService;

    // ==================== create ====================

    @Test
    void create_shouldCreateCategory_whenNameIsValid() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        Category savedCategory = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = categoryService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Electronics", response.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    // ==================== delete ====================

    @Test
    void delete_shouldDeleteCategory_whenNotInUse() {
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(advertisementRepository.existsByCategoryId(1L)).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void delete_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExist() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.delete(99L));

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowCategoryInUseException_whenCategoryIsUsedByAdvertisement() {
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(advertisementRepository.existsByCategoryId(1L)).thenReturn(true);

        assertThrows(CategoryInUseException.class,
                () -> categoryService.delete(1L));

        verify(categoryRepository, never()).delete(any());
    }

    // ==================== getAllCategories ====================

    @Test
    void getAllCategories_shouldReturnAllCategories_whenSomeExist() {
        Category category1 = Category.builder().id(1L).name("Electronics").build();
        Category category2 = Category.builder().id(2L).name("Furniture").build();

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertEquals(2, responses.size());
        assertEquals("Electronics", responses.get(0).getName());
        assertEquals("Furniture", responses.get(1).getName());
    }

    @Test
    void getAllCategories_shouldReturnEmptyList_whenNoneExist() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}
