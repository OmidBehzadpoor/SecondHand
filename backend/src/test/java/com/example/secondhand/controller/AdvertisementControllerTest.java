package com.example.secondhand.controller;

import com.example.secondhand.dto.AdvertisementRequest;
import com.example.secondhand.dto.response.AdvertisementResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.AdvertisementStatus;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.service.AdvertisementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdvertisementControllerTest {

    @Mock
    private AdvertisementService advertisementService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final User CURRENT_USER = User.builder()
            .id(1L).username("ali123").role(Role.USER).build();

    @BeforeEach
    void setUp() {
        AdvertisementController controller = new AdvertisementController(advertisementService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        CURRENT_USER, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AdvertisementRequest validRequest() {
        return AdvertisementRequest.builder()
                .title("Laptop")
                .description("Used laptop in good condition")
                .price(18000000L)
                .categoryId(1L)
                .cityId(1L)
                .imageUrls(List.of())
                .build();
    }

    // ==================== create ====================

    @Test
    void create_shouldReturn201_whenDataIsValid() throws Exception {
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(1L).title("Laptop").status(AdvertisementStatus.PENDING).build();

        when(advertisementService.create(any(AdvertisementRequest.class), eq(CURRENT_USER)))
                .thenReturn(response);

        mockMvc.perform(post("/api/advertisements")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageCode").value("ADVERTISEMENT_CREATED"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void create_shouldReturn400_whenPriceIsNegative() throws Exception {
        AdvertisementRequest request = validRequest();
        request.setPrice(-100L);

        mockMvc.perform(post("/api/advertisements")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenTitleIsBlank() throws Exception {
        AdvertisementRequest request = validRequest();
        request.setTitle("");

        mockMvc.perform(post("/api/advertisements")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenCategoryIdIsMissing() throws Exception {
        AdvertisementRequest request = validRequest();
        request.setCategoryId(null);

        mockMvc.perform(post("/api/advertisements")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== getById ====================

    @Test
    void getById_shouldReturn200_whenAdvertisementExists() throws Exception {
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(5L).title("Laptop").status(AdvertisementStatus.APPROVED).build();

        when(advertisementService.getById(eq(5L), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(get("/api/advertisements/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.title").value("Laptop"));
    }

    @Test
    void getById_shouldReturn404_whenAdvertisementDoesNotExist() throws Exception {
        when(advertisementService.getById(eq(99L), eq(CURRENT_USER)))
                .thenThrow(new AdvertisementNotFoundException("آگهی یافت نشد"));

        mockMvc.perform(get("/api/advertisements/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("آگهی یافت نشد"));
    }

    // ==================== getAll ====================

    @Test
    void getAll_shouldReturn200_withResults() throws Exception {
        AdvertisementResponse ad1 = AdvertisementResponse.builder().id(1L).title("Laptop").build();
        AdvertisementResponse ad2 = AdvertisementResponse.builder().id(2L).title("Phone").build();

        when(advertisementService.getAll(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(ad1, ad2));

        mockMvc.perform(get("/api/advertisements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // ==================== update ====================

    @Test
    void update_shouldReturn200_whenUserIsOwner() throws Exception {
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(1L).title("Updated Title").status(AdvertisementStatus.PENDING).build();

        when(advertisementService.update(eq(1L), any(AdvertisementRequest.class), eq(CURRENT_USER)))
                .thenReturn(response);

        mockMvc.perform(put("/api/advertisements/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Title"));
    }

    @Test
    void update_shouldReturn403_whenUserIsNotOwner() throws Exception {
        when(advertisementService.update(eq(1L), any(AdvertisementRequest.class), eq(CURRENT_USER)))
                .thenThrow(new UnauthorizedActionException("اجازه‌ی این عملیات را ندارید"));

        mockMvc.perform(put("/api/advertisements/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("اجازه‌ی این عملیات را ندارید"));
    }

    // ==================== delete ====================

    @Test
    void delete_shouldReturn200_whenUserIsOwner() throws Exception {
        mockMvc.perform(delete("/api/advertisements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("ADVERTISEMENT_DELETED"));
    }

    @Test
    void delete_shouldReturn404_whenAdvertisementDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new AdvertisementNotFoundException("آگهی یافت نشد"))
                .when(advertisementService).delete(eq(99L), eq(CURRENT_USER));

        mockMvc.perform(delete("/api/advertisements/99"))
                .andExpect(status().isNotFound());
    }

    // ==================== markAsSold ====================

    @Test
    void markAsSold_shouldReturn200_whenUserIsOwner() throws Exception {
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(1L).status(AdvertisementStatus.SOLD).build();

        when(advertisementService.markAsSold(eq(1L), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(patch("/api/advertisements/1/sold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SOLD"));
    }
}
