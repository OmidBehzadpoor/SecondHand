package com.example.secondhand.controller;

import com.example.secondhand.dto.response.FavoriteResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.FavoriteAlreadyExistsException;
import com.example.secondhand.exception.FavoriteNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.service.FavoriteService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    private MockMvc mockMvc;

    private static final User CURRENT_USER = User.builder().id(1L).username("ali123").role(Role.USER).build();

    @BeforeEach
    void setUp() {
        FavoriteController controller = new FavoriteController(favoriteService);
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

    @Test
    void addFavorite_shouldReturn201_whenSuccessful() throws Exception {
        FavoriteResponse response = FavoriteResponse.builder().id(1L).advertisementId(10L).build();

        when(favoriteService.addFavorite(eq(10L), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(post("/api/favorites/10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.advertisementId").value(10));
    }

    @Test
    void addFavorite_shouldReturn409_whenAlreadyFavorited() throws Exception {
        when(favoriteService.addFavorite(eq(10L), eq(CURRENT_USER)))
                .thenThrow(new FavoriteAlreadyExistsException("قبلاً به علاقه‌مندی‌ها اضافه شده"));

        mockMvc.perform(post("/api/favorites/10"))
                .andExpect(status().isConflict());
    }

    @Test
    void addFavorite_shouldReturn404_whenAdvertisementDoesNotExist() throws Exception {
        when(favoriteService.addFavorite(eq(99L), eq(CURRENT_USER)))
                .thenThrow(new AdvertisementNotFoundException("آگهی یافت نشد"));

        mockMvc.perform(post("/api/favorites/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFavorite_shouldReturn200_whenSuccessful() throws Exception {
        mockMvc.perform(delete("/api/favorites/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("FAVORITE_REMOVED"));
    }

    @Test
    void removeFavorite_shouldReturn404_whenFavoriteDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new FavoriteNotFoundException("علاقه‌مندی یافت نشد"))
                .when(favoriteService).removeFavorite(eq(10L), eq(CURRENT_USER));

        mockMvc.perform(delete("/api/favorites/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMyFavorites_shouldReturn200_withList() throws Exception {
        FavoriteResponse response = FavoriteResponse.builder().id(1L).advertisementId(10L).build();

        when(favoriteService.getMyFavorites(eq(CURRENT_USER))).thenReturn(List.of(response));

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getMyFavorites_shouldReturn200_withEmptyList() throws Exception {
        when(favoriteService.getMyFavorites(eq(CURRENT_USER))).thenReturn(List.of());

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
