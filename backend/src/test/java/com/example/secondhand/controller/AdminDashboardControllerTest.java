package com.example.secondhand.controller;

import com.example.secondhand.dto.response.AdminDashboardResponse;
import com.example.secondhand.service.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private AdminDashboardService adminDashboardService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminDashboardController controller = new AdminDashboardController(adminDashboardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getDashboard_shouldRequireAdminRole() throws NoSuchMethodException {
        Method method = AdminDashboardController.class.getMethod("getDashboard");
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }

    @Test
    void getDashboard_shouldReturn200_withAggregatedCounts() throws Exception {
        AdminDashboardResponse response = AdminDashboardResponse.builder()
                .totalUsers(100L).activeUsers(90L).blockedUsers(10L)
                .totalAdvertisements(50L).pendingAdvertisements(5L)
                .approvedAdvertisements(30L).rejectedAdvertisements(3L)
                .soldAdvertisements(10L).deletedAdvertisements(2L)
                .totalCategories(15L).totalCities(8L)
                .totalConversations(40L).totalMessages(200L)
                .totalFavorites(60L).totalRatings(25L)
                .build();

        when(adminDashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.blockedUsers").value(10))
                .andExpect(jsonPath("$.data.totalAdvertisements").value(50));
    }
}
