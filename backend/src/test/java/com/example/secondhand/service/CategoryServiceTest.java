package com.example.secondhand.service;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.exception.*;
import com.example.secondhand.model.Category;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryRequest request(String name, Long parentId) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        request.setParentId(parentId);
        return request;
    }

    // ==================== create ====================

    @Test
    void create_shouldCreateTopLevelCategory_whenParentIdIsNull() {
        Category saved = Category.builder().id(1L).name("Electronics").active(true)
                .children(new ArrayList<>()).build();

        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.create(request("Electronics", null));

        assertEquals("Electronics", response.getName());
        assertNull(response.getParentId());
    }

    @Test
    void create_shouldCreateSubCategory_whenParentIdIsProvided() {
        Category parent = Category.builder().id(1L).name("Electronics").active(true)
                .children(new ArrayList<>()).build();
        Category saved = Category.builder().id(2L).name("Laptops").parent(parent).active(true)
                .children(new ArrayList<>()).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.create(request("Laptops", 1L));

        assertEquals("Laptops", response.getName());
        assertEquals(1L, response.getParentId());
    }

    @Test
    void create_shouldThrowCategoryNotFoundException_whenParentDoesNotExist() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.create(request("Laptops", 99L)));

        verify(categoryRepository, never()).save(any());
    }

    // ==================== delete ====================

    @Test
    void delete_shouldDeleteCategory_whenNoChildrenAndNotInUse() {
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(1L)).thenReturn(false);
        when(advertisementRepository.existsByCategoryIdAndStatusIn(eq(1L), anyList())).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void delete_shouldThrowCategoryNotFoundException_whenCategoryDoesNotExist() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> categoryService.delete(99L));

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowCategoryHasChildrenException_whenCategoryHasSubCategories() {
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(1L)).thenReturn(true);

        assertThrows(CategoryHasChildrenException.class, () -> categoryService.delete(1L));

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowCategoryInUseException_whenApprovedOrPendingAdvertisementExists() {
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(1L)).thenReturn(false);
        when(advertisementRepository.existsByCategoryIdAndStatusIn(eq(1L), anyList())).thenReturn(true);

        assertThrows(CategoryInUseException.class, () -> categoryService.delete(1L));

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_shouldSucceed_whenOnlySoldOrDeletedAdvertisementsExist() {
        // Only APPROVED/PENDING ads should block deletion — a category whose ads are all
        // SOLD/REJECTED/DELETED should still be deletable.
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(1L)).thenReturn(false);
        when(advertisementRepository.existsByCategoryIdAndStatusIn(eq(1L), anyList())).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository, times(1)).delete(category);
    }

    // ==================== getAllCategories (public) ====================

    @Test
    void getAllCategories_shouldOnlyReturnActiveTopLevelCategories() {
        Category active = Category.builder().id(1L).name("Electronics").active(true)
                .children(new ArrayList<>()).build();

        when(categoryRepository.findByParentIsNullAndActiveTrue()).thenReturn(List.of(active));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    @Test
    void getAllCategories_shouldFilterOutInactiveChildren() {
        Category activeChild = Category.builder().id(2L).name("Laptops").active(true)
                .children(new ArrayList<>()).build();
        Category inactiveChild = Category.builder().id(3L).name("Old Category").active(false)
                .children(new ArrayList<>()).build();
        Category parent = Category.builder().id(1L).name("Electronics").active(true)
                .children(new ArrayList<>(List.of(activeChild, inactiveChild))).build();

        when(categoryRepository.findByParentIsNullAndActiveTrue()).thenReturn(List.of(parent));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertEquals(1, result.get(0).getSubCategories().size());
        assertEquals("Laptops", result.get(0).getSubCategories().get(0).getName());
    }

    // ==================== getAllCategoriesForAdmin ====================

    @Test
    void getAllCategoriesForAdmin_shouldIncludeInactiveCategoriesAndChildren() {
        Category inactiveChild = Category.builder().id(3L).name("Old Category").active(false)
                .children(new ArrayList<>()).build();
        Category parent = Category.builder().id(1L).name("Electronics").active(true)
                .children(new ArrayList<>(List.of(inactiveChild))).build();

        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(parent));

        List<CategoryResponse> result = categoryService.getAllCategoriesForAdmin();

        assertEquals(1, result.get(0).getSubCategories().size());
        assertEquals("Old Category", result.get(0).getSubCategories().get(0).getName());
        assertFalse(result.get(0).getSubCategories().get(0).isActive());
    }

    // ==================== activate / deactivate ====================

    @Test
    void activate_shouldActivateCategory_whenCurrentlyInactive() {
        Category category = Category.builder().id(1L).name("Electronics").active(false)
                .children(new ArrayList<>()).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoryResponse response = categoryService.activate(1L);

        assertTrue(response.isActive());
    }

    @Test
    void activate_shouldThrowCategoryStateConflictException_whenAlreadyActive() {
        Category category = Category.builder().id(1L).name("Electronics").active(true).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryStateConflictException.class, () -> categoryService.activate(1L));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deactivate_shouldDeactivateCategory_whenCurrentlyActiveAndNoActiveOrPendingAds() {
        Category category = Category.builder().id(1L).name("Electronics").active(true)
                .children(new ArrayList<>()).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(advertisementRepository.existsByCategoryIdInAndStatusIn(eq(List.of(1L)), anyList())).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoryResponse response = categoryService.deactivate(1L);

        assertFalse(response.isActive());
    }

    @Test
    void deactivate_shouldThrowCategoryInUseException_whenActiveOrPendingAdsExistInCategoryOrDescendants() {
        Category child = Category.builder().id(2L).name("Laptops").active(true)
                .children(new ArrayList<>()).build();
        Category category = Category.builder().id(1L).name("Electronics").active(true)
                .children(new ArrayList<>(List.of(child))).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(advertisementRepository.existsByCategoryIdInAndStatusIn(eq(List.of(1L, 2L)), anyList())).thenReturn(true);

        assertThrows(CategoryInUseException.class, () -> categoryService.deactivate(1L));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deactivate_shouldThrowCategoryStateConflictException_whenAlreadyInactive() {
        Category category = Category.builder().id(1L).name("Electronics").active(false).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(CategoryStateConflictException.class, () -> categoryService.deactivate(1L));

        verify(categoryRepository, never()).save(any());
    }

    // ==================== update (with cycle detection) ====================

    @Test
    void update_shouldUpdateNameAndParent_whenNoCycleExists() {
        Category category = Category.builder().id(2L).name("Laptops").children(new ArrayList<>()).build();
        Category newParent = Category.builder().id(1L).name("Electronics").children(new ArrayList<>()).build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(newParent));
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoryResponse response = categoryService.update(2L, request("Laptops & PCs", 1L));

        assertEquals("Laptops & PCs", response.getName());
        assertEquals(1L, response.getParentId());
    }

    @Test
    void update_shouldThrowCategoryNotFoundException_whenNewParentDoesNotExist() {
        Category category = Category.builder().id(2L).name("Laptops").build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class,
                () -> categoryService.update(2L, request("Laptops", 99L)));
    }

    @Test
    void update_shouldThrowInvalidCategoryHierarchyException_whenNewParentIsItself() {
        Category category = Category.builder().id(1L).name("Electronics").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(InvalidCategoryHierarchyException.class,
                () -> categoryService.update(1L, request("Electronics", 1L)));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_shouldThrowInvalidCategoryHierarchyException_whenNewParentIsOwnDescendant() {
        // Category (1) -> Sub (2) -> SubSub (3). Trying to move Category(1) under SubSub(3)
        // would create a cycle: 1 -> 3 -> 2 -> 1.
        Category root = Category.builder().id(1L).name("Category").build();
        Category sub = Category.builder().id(2L).name("Sub").parent(root).build();
        Category subSub = Category.builder().id(3L).name("SubSub").parent(sub).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(subSub));

        assertThrows(InvalidCategoryHierarchyException.class,
                () -> categoryService.update(1L, request("Category", 3L)));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_shouldAllowMovingToTopLevel_whenParentIdIsNull() {
        Category oldParent = Category.builder().id(1L).name("Electronics").build();
        Category category = Category.builder().id(2L).name("Laptops").parent(oldParent)
                .children(new ArrayList<>()).build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoryResponse response = categoryService.update(2L, request("Laptops", null));

        assertNull(response.getParentId());
    }
}
