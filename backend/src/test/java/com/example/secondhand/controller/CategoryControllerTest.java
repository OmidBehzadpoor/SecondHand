package com.example.secondhand.controller;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.exception.*;
import com.example.secondhand.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private CategoryRequest request(String name, Long parentId) {
        CategoryRequest request = new CategoryRequest();
        request.setName(name);
        request.setParentId(parentId);
        return request;
    }

    // ==================== @PreAuthorize enforcement ====================

    @Test
    void createCategory_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("createCategory", CategoryRequest.class);
    }

    @Test
    void deleteCategory_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("deleteCategory", Long.class);
    }

    @Test
    void updateCategory_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("updateCategory", Long.class, CategoryRequest.class);
    }

    @Test
    void getAllCategoriesForAdmin_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("getAllCategoriesForAdmin");
    }

    @Test
    void activateCategory_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("activateCategory", Long.class);
    }

    @Test
    void deactivateCategory_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("deactivateCategory", Long.class);
    }

    @Test
    void getAllCategories_shouldNotRequireAdminRole() throws NoSuchMethodException {
        Method method = CategoryController.class.getMethod("getAllCategories");
        assertNull(method.getAnnotation(PreAuthorize.class));
    }

    private void assertRequiresAdminRole(String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = CategoryController.class.getMethod(methodName, paramTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize, "Expected @PreAuthorize on " + methodName);
        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }

    // ==================== getAllCategories (public) ====================

    @Test
    void getAllCategories_shouldReturn200_withList() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").active(true).build();

        when(categoryService.getAllCategories()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    // ==================== createCategory ====================

    @Test
    void createCategory_shouldReturn201_whenDataIsValid() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").active(true).build();

        when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request("Electronics", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Electronics"));
    }

    @Test
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request("", null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_shouldReturn404_whenParentDoesNotExist() throws Exception {
        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new CategoryNotFoundException("دسته‌بندی والد یافت نشد"));

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request("Laptops", 99L))))
                .andExpect(status().isNotFound());
    }

    // ==================== updateCategory ====================

    @Test
    void updateCategory_shouldReturn200_whenValid() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Updated").active(true).build();

        when(categoryService.update(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request("Updated", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }

    @Test
    void updateCategory_shouldReturn400_whenHierarchyIsInvalid() throws Exception {
        when(categoryService.update(eq(1L), any(CategoryRequest.class)))
                .thenThrow(new InvalidCategoryHierarchyException("دسته‌بندی نمی‌تواند والد خودش باشد"));

        mockMvc.perform(put("/api/categories/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request("Electronics", 1L))))
                .andExpect(status().isBadRequest());
    }

    // ==================== deleteCategory ====================

    @Test
    void deleteCategory_shouldReturn200_whenDeletable() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("CATEGORY_DELETED"));
    }

    @Test
    void deleteCategory_shouldReturn409_whenHasChildren() throws Exception {
        org.mockito.Mockito.doThrow(new CategoryHasChildrenException("این دسته‌بندی زیردسته دارد و قابل حذف نیست"))
                .when(categoryService).delete(eq(1L));

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCategory_shouldReturn409_whenInUse() throws Exception {
        org.mockito.Mockito.doThrow(new CategoryInUseException("در حال استفاده است"))
                .when(categoryService).delete(eq(1L));

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isConflict());
    }

    // ==================== getAllCategoriesForAdmin ====================

    @Test
    void getAllCategoriesForAdmin_shouldReturn200_withList() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").active(false).build();

        when(categoryService.getAllCategoriesForAdmin()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/categories/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].active").value(false));
    }

    // ==================== activate / deactivate ====================

    @Test
    void activateCategory_shouldReturn200_whenCurrentlyInactive() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").active(true).build();

        when(categoryService.activate(eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/categories/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void activateCategory_shouldReturn409_whenAlreadyActive() throws Exception {
        when(categoryService.activate(eq(1L)))
                .thenThrow(new CategoryStateConflictException("دسته‌بندی از قبل فعال است"));

        mockMvc.perform(patch("/api/categories/1/activate"))
                .andExpect(status().isConflict());
    }

    @Test
    void deactivateCategory_shouldReturn200_whenCurrentlyActive() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").active(false).build();

        when(categoryService.deactivate(eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/categories/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void deactivateCategory_shouldReturn409_whenAlreadyInactive() throws Exception {
        when(categoryService.deactivate(eq(1L)))
                .thenThrow(new CategoryStateConflictException("دسته‌بندی از قبل غیرفعال است"));

        mockMvc.perform(patch("/api/categories/1/deactivate"))
                .andExpect(status().isConflict());
    }
}
