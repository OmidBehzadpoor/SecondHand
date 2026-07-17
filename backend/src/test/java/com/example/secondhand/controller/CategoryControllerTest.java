package com.example.secondhand.controller;

import com.example.secondhand.dto.CategoryRequest;
import com.example.secondhand.dto.response.CategoryResponse;
import com.example.secondhand.exception.CategoryInUseException;
import com.example.secondhand.exception.CategoryNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    // ==================== @PreAuthorize enforcement (reflection-based) ====================

    @Test
    void createCategory_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("createCategory", CategoryRequest.class);
    }

    @Test
    void deleteCategory_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("deleteCategory", Long.class);
    }

    @Test
    void getAllCategories_shouldNotRequireAdminRole() throws NoSuchMethodException {
        Method method = CategoryController.class.getMethod("getAllCategories");
        assertNotNull(method, "getAllCategories should be a public read endpoint");
        assertEquals(null, method.getAnnotation(PreAuthorize.class),
                "getAllCategories must stay open to any authenticated/anonymous user");
    }

    private void assertRequiresAdminRole(String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = CategoryController.class.getMethod(methodName, paramTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize, "Expected @PreAuthorize on " + methodName);
        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }

    // ==================== getAllCategories ====================

    @Test
    void getAllCategories_shouldReturn200_withList() throws Exception {
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").build();

        when(categoryService.getAllCategories()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Electronics"));
    }

    @Test
    void getAllCategories_shouldReturn200_withEmptyList() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== createCategory ====================

    @Test
    void createCategory_shouldReturn201_whenDataIsValid() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        CategoryResponse response = CategoryResponse.builder().id(1L).name("Electronics").build();

        when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Electronics"));
    }

    @Test
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("");

        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== deleteCategory ====================

    @Test
    void deleteCategory_shouldReturn200_whenNotInUse() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("CATEGORY_DELETED"));
    }

    @Test
    void deleteCategory_shouldReturn404_whenCategoryDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new CategoryNotFoundException("دسته‌بندی یافت نشد"))
                .when(categoryService).delete(eq(99L));

        mockMvc.perform(delete("/api/categories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_shouldReturn409_whenCategoryIsInUse() throws Exception {
        org.mockito.Mockito.doThrow(new CategoryInUseException("این دسته‌بندی در حال استفاده است"))
                .when(categoryService).delete(eq(1L));

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isConflict());
    }
}
