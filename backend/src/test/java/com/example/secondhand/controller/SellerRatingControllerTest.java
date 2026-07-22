package com.example.secondhand.controller;

import com.example.secondhand.dto.SellerRatingRequest;
import com.example.secondhand.dto.response.SellerRatingResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.exception.RatingAlreadyExistsException;
import com.example.secondhand.exception.UnauthorizedActionException;
import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import com.example.secondhand.service.SellerRatingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SellerRatingControllerTest {

    @Mock
    private SellerRatingService sellerRatingService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final User CURRENT_USER = User.builder().id(2L).username("buyer").role(Role.USER).build();

    @BeforeEach
    void setUp() {
        SellerRatingController controller = new SellerRatingController(sellerRatingService);
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

    private SellerRatingRequest request(Integer rating) {
        SellerRatingRequest request = new SellerRatingRequest();
        request.setRating(rating);
        request.setComment("عالی بود");
        return request;
    }

    @Test
    void rateAdvertisement_shouldReturn201_whenDataIsValid() throws Exception {
        SellerRatingResponse response = SellerRatingResponse.builder().id(1L).rating(5).build();

        when(sellerRatingService.rateAdvertisement(eq(10L), any(SellerRatingRequest.class), eq(CURRENT_USER)))
                .thenReturn(response);

        mockMvc.perform(post("/api/ratings/advertisements/10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    void rateAdvertisement_shouldReturn400_whenRatingIsAboveFive() throws Exception {
        mockMvc.perform(post("/api/ratings/advertisements/10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(6))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rateAdvertisement_shouldReturn400_whenRatingIsBelowOne() throws Exception {
        mockMvc.perform(post("/api/ratings/advertisements/10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(0))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rateAdvertisement_shouldReturn400_whenRatingIsMissing() throws Exception {
        mockMvc.perform(post("/api/ratings/advertisements/10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rateAdvertisement_shouldReturn409_whenAlreadyRated() throws Exception {
        when(sellerRatingService.rateAdvertisement(eq(10L), any(SellerRatingRequest.class), eq(CURRENT_USER)))
                .thenThrow(new RatingAlreadyExistsException("قبلاً امتیاز داده‌اید"));

        mockMvc.perform(post("/api/ratings/advertisements/10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(5))))
                .andExpect(status().isConflict());
    }

    @Test
    void rateAdvertisement_shouldReturn403_whenRatingOwnAdvertisement() throws Exception {
        when(sellerRatingService.rateAdvertisement(eq(10L), any(SellerRatingRequest.class), eq(CURRENT_USER)))
                .thenThrow(new UnauthorizedActionException("نمی‌توانید به خودتان امتیاز دهید"));

        mockMvc.perform(post("/api/ratings/advertisements/10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(5))))
                .andExpect(status().isForbidden());
    }

    @Test
    void rateAdvertisement_shouldReturn404_whenAdvertisementDoesNotExist() throws Exception {
        when(sellerRatingService.rateAdvertisement(eq(99L), any(SellerRatingRequest.class), eq(CURRENT_USER)))
                .thenThrow(new AdvertisementNotFoundException("آگهی یافت نشد"));

        mockMvc.perform(post("/api/ratings/advertisements/99")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request(5))))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSellerRatings_shouldReturn200_withList() throws Exception {
        SellerRatingResponse rating = SellerRatingResponse.builder().id(1L).rating(5).build();

        when(sellerRatingService.getSellerRatings(eq(1L))).thenReturn(List.of(rating));

        mockMvc.perform(get("/api/ratings/sellers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void getSellerAverageRating_shouldReturn200_withAverage() throws Exception {
        when(sellerRatingService.getSellerAverageRating(eq(1L))).thenReturn(4.5);

        mockMvc.perform(get("/api/ratings/sellers/1/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(4.5));
    }

    @Test
    void getSellerRatingCount_shouldReturn200_withCount() throws Exception {
        when(sellerRatingService.getSellerRatingCount(eq(1L))).thenReturn(3L);

        mockMvc.perform(get("/api/ratings/sellers/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3));
    }
}
