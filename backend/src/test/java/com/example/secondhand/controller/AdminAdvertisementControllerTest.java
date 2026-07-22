package com.example.secondhand.controller;

import com.example.secondhand.dto.AdminRejectRequest;
import com.example.secondhand.dto.response.AdminAdvertisementResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.exception.InvalidAdvertisementStateException;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.service.AdvertisementService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminAdvertisementControllerTest {

    @Mock
    private AdvertisementService advertisementService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AdminAdvertisementController controller = new AdminAdvertisementController(advertisementService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getPending_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("getPending");
    }

    @Test
    void approve_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("approve", Long.class);
    }

    @Test
    void reject_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("reject", Long.class, AdminRejectRequest.class);
    }

    @Test
    void delete_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("delete", Long.class);
    }

    private void assertRequiresAdminRole(String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = AdminAdvertisementController.class.getMethod(methodName, paramTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize, "Expected @PreAuthorize on " + methodName);
        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }

    @Test
    void getPending_shouldReturn200_withList() throws Exception {
        AdminAdvertisementResponse response = AdminAdvertisementResponse.builder()
                .id(1L).title("Laptop").status(AdvertisementStatus.PENDING).build();

        when(advertisementService.getPendingAdvertisements()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/advertisements/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void approve_shouldReturn200_whenPending() throws Exception {
        AdminAdvertisementResponse response = AdminAdvertisementResponse.builder()
                .id(1L).status(AdvertisementStatus.APPROVED).build();

        when(advertisementService.approve(eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/advertisements/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void approve_shouldReturn400_whenAdvertisementIsNotPending() throws Exception {
        when(advertisementService.approve(eq(1L)))
                .thenThrow(new InvalidAdvertisementStateException("فقط آگهی‌های در انتظار بررسی قابل تایید هستند"));

        mockMvc.perform(patch("/api/admin/advertisements/1/approve"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_shouldReturn404_whenAdvertisementDoesNotExist() throws Exception {
        when(advertisementService.approve(eq(99L)))
                .thenThrow(new AdvertisementNotFoundException("آگهی یافت نشد"));

        mockMvc.perform(patch("/api/admin/advertisements/99/approve"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reject_shouldReturn200_whenPending() throws Exception {
        AdminRejectRequest request = new AdminRejectRequest();
        request.setReason("محتوای نامناسب");

        AdminAdvertisementResponse response = AdminAdvertisementResponse.builder()
                .id(1L).status(AdvertisementStatus.REJECTED).rejectionReason("محتوای نامناسب").build();

        when(advertisementService.reject(eq(1L), eq("محتوای نامناسب"))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/advertisements/1/reject")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    void reject_shouldReturn400_whenReasonIsBlank() throws Exception {
        AdminRejectRequest request = new AdminRejectRequest();
        request.setReason("");

        mockMvc.perform(patch("/api/admin/advertisements/1/reject")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn200_whenAdvertisementIsDeletable() throws Exception {
        mockMvc.perform(delete("/api/admin/advertisements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("ADVERTISEMENT_DELETED_BY_ADMIN"));
    }

    @Test
    void delete_shouldReturn400_whenAdvertisementIsAlreadyDeleted() throws Exception {
        org.mockito.Mockito.doThrow(new InvalidAdvertisementStateException("این آگهی قبلاً حذف شده است"))
                .when(advertisementService).adminDelete(eq(1L));

        mockMvc.perform(delete("/api/admin/advertisements/1"))
                .andExpect(status().isBadRequest());
    }
}
