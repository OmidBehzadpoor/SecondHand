package com.example.secondhand.controller;

import com.example.secondhand.dto.AdvertisementRequest;
import com.example.secondhand.dto.response.AdvertisementResponse;
import com.example.secondhand.exception.AdvertisementNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.exception.InvalidAdvertisementStateException;
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
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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

    private static final User CURRENT_USER = User.builder().id(1L).username("ali123").role(Role.USER).build();

    @BeforeEach
    void setUp() {
        AdvertisementController controller = new AdvertisementController(advertisementService);

        LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
        validatorFactory.afterPropertiesSet();

        ProxyFactory proxyFactory = new ProxyFactory(controller);
        proxyFactory.addAdvice(new MethodValidationInterceptor(validatorFactory.getValidator()));
        AdvertisementController validatedController = (AdvertisementController) proxyFactory.getProxy();

        mockMvc = MockMvcBuilders.standaloneSetup(validatedController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver())
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
        AdvertisementRequest request = new AdvertisementRequest();
        request.setTitle("Laptop");
        request.setDescription("Used laptop in good condition");
        request.setPrice(1000L);
        request.setCategoryId(1L);
        request.setCityId(1L);
        request.setImageUrls(List.of());
        return request;
    }

    // ==================== create ====================

    @Test
    void create_shouldReturn201_whenDataIsValid() throws Exception {
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(1L).title("Laptop").status(AdvertisementStatus.PENDING).build();

        when(advertisementService.create(any(AdvertisementRequest.class), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(post("/api/advertisements")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
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

    // ==================== getById ====================

    @Test
    void getById_shouldReturn200_whenAdvertisementExists() throws Exception {
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(5L).title("Laptop").status(AdvertisementStatus.APPROVED).build();

        when(advertisementService.getById(eq(5L), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(get("/api/advertisements/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Laptop"));
    }

    @Test
    void getById_shouldReturn404_whenAdvertisementDoesNotExist() throws Exception {
        when(advertisementService.getById(eq(99L), eq(CURRENT_USER)))
                .thenThrow(new AdvertisementNotFoundException("آگهی یافت نشد"));

        mockMvc.perform(get("/api/advertisements/99"))
                .andExpect(status().isNotFound());
    }

    // ==================== getAll ====================

    @Test
    void getAll_shouldReturn200_withPagedResults() throws Exception {
        AdvertisementResponse ad1 = AdvertisementResponse.builder().id(1L).title("Laptop").build();
        AdvertisementResponse ad2 = AdvertisementResponse.builder().id(2L).title("Phone").build();
        Page<AdvertisementResponse> page = new PageImpl<>(List.of(ad1, ad2));

        when(advertisementService.getAll(any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/advertisements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void getAll_shouldReturn400_whenMinPriceIsNegative() throws Exception {
        mockMvc.perform(get("/api/advertisements").param("minPrice", "-1"))
                .andExpect(status().isBadRequest());
    }

    // ==================== getMyAdvertisements ====================

    @Test
    void getMyAdvertisements_shouldReturn200_withList() throws Exception {
        AdvertisementResponse ad = AdvertisementResponse.builder().id(1L).title("Laptop").build();

        when(advertisementService.getMyAdvertisements(eq(CURRENT_USER))).thenReturn(List.of(ad));

        mockMvc.perform(get("/api/advertisements/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    // ==================== update ====================

    @Test
    void update_shouldReturn200_whenUserIsOwner() throws Exception {
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(1L).title("Updated").status(AdvertisementStatus.PENDING).build();

        when(advertisementService.update(eq(1L), any(AdvertisementRequest.class), eq(CURRENT_USER)))
                .thenReturn(response);

        mockMvc.perform(put("/api/advertisements/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated"));
    }

    @Test
    void update_shouldReturn403_whenUserIsNotOwner() throws Exception {
        when(advertisementService.update(eq(1L), any(AdvertisementRequest.class), eq(CURRENT_USER)))
                .thenThrow(new UnauthorizedActionException("اجازه ندارید"));

        mockMvc.perform(put("/api/advertisements/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
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
    void markAsSold_shouldReturn200_whenAdvertisementIsApproved() throws Exception {
        AdvertisementResponse response = AdvertisementResponse.builder()
                .id(1L).status(AdvertisementStatus.SOLD).build();

        when(advertisementService.markAsSold(eq(1L), eq(CURRENT_USER))).thenReturn(response);

        mockMvc.perform(patch("/api/advertisements/1/sold"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SOLD"));
    }

    @Test
    void markAsSold_shouldReturn400_whenAdvertisementIsNotApproved() throws Exception {
        when(advertisementService.markAsSold(eq(1L), eq(CURRENT_USER)))
                .thenThrow(new InvalidAdvertisementStateException("فقط آگهی‌های تاییدشده را می‌توان فروخته‌شده علامت زد"));

        mockMvc.perform(patch("/api/advertisements/1/sold"))
                .andExpect(status().isBadRequest());
    }
}
