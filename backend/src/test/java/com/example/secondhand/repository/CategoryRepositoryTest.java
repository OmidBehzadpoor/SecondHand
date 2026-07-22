package com.example.secondhand.repository;

import com.example.secondhand.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    // ==================== unique name constraint ====================

    @Test
    void save_shouldPersistCategory_whenNameIsUnique() {
        Category saved = categoryRepository.saveAndFlush(Category.builder().name("Electronics").build());

        assertTrue(categoryRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void save_shouldThrowDataAccessException_whenNameIsDuplicated() {
        categoryRepository.saveAndFlush(Category.builder().name("Electronics").build());

        Category duplicate = Category.builder().name("Electronics").build();

        assertThrows(DataAccessException.class,
                () -> categoryRepository.saveAndFlush(duplicate));
    }

    // ==================== findByParentIsNull ====================

    @Test
    void findByParentIsNull_shouldReturnOnlyTopLevelCategories() {
        Category parent = categoryRepository.save(Category.builder().name("Electronics").build());
        categoryRepository.save(Category.builder().name("Laptops").parent(parent).build());

        List<Category> result = categoryRepository.findByParentIsNull();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    // ==================== findByParentIsNullAndActiveTrue ====================

    @Test
    void findByParentIsNullAndActiveTrue_shouldExcludeInactiveTopLevelCategories() {
        categoryRepository.save(Category.builder().name("Electronics").active(true).build());
        categoryRepository.save(Category.builder().name("Discontinued").active(false).build());

        List<Category> result = categoryRepository.findByParentIsNullAndActiveTrue();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    @Test
    void findByParentIsNullAndActiveTrue_shouldExcludeActiveSubCategories() {
        Category parent = categoryRepository.save(Category.builder().name("Electronics").active(true).build());
        categoryRepository.save(Category.builder().name("Laptops").parent(parent).active(true).build());

        List<Category> result = categoryRepository.findByParentIsNullAndActiveTrue();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    // ==================== existsByParentId ====================

    @Test
    void existsByParentId_shouldReturnTrue_whenCategoryHasChildren() {
        Category parent = categoryRepository.save(Category.builder().name("Electronics").build());
        categoryRepository.save(Category.builder().name("Laptops").parent(parent).build());

        assertTrue(categoryRepository.existsByParentId(parent.getId()));
    }

    @Test
    void existsByParentId_shouldReturnFalse_whenCategoryHasNoChildren() {
        Category parent = categoryRepository.save(Category.builder().name("Electronics").build());

        assertFalse(categoryRepository.existsByParentId(parent.getId()));
    }
}
