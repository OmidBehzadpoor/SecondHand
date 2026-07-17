package com.example.secondhand.controller;

import com.example.secondhand.dto.CityRequest;
import com.example.secondhand.dto.response.CityResponse;
import com.example.secondhand.exception.CityInUseException;
import com.example.secondhand.exception.CityNotFoundException;
import com.example.secondhand.exception.GlobalExceptionHandler;
import com.example.secondhand.service.CityService;
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
class CityControllerTest {

    @Mock
    private CityService cityService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CityController controller = new CityController(cityService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== @PreAuthorize enforcement (reflection-based) ====================

    @Test
    void createCity_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("createCity", CityRequest.class);
    }

    @Test
    void deleteCity_shouldRequireAdminRole() throws NoSuchMethodException {
        assertRequiresAdminRole("deleteCity", Long.class);
    }

    @Test
    void getAllCities_shouldNotRequireAdminRole() throws NoSuchMethodException {
        Method method = CityController.class.getMethod("getAllCities");
        assertNotNull(method, "getAllCities should be a public read endpoint");
        assertEquals(null, method.getAnnotation(PreAuthorize.class),
                "getAllCities must stay open to any authenticated/anonymous user");
    }

    private void assertRequiresAdminRole(String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method = CityController.class.getMethod(methodName, paramTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize, "Expected @PreAuthorize on " + methodName);
        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }

    // ==================== getAllCities ====================

    @Test
    void getAllCities_shouldReturn200_withList() throws Exception {
        CityResponse response = CityResponse.builder().id(1L).name("Tehran").build();

        when(cityService.getAllCities()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Tehran"));
    }

    @Test
    void getAllCities_shouldReturn200_withEmptyList() throws Exception {
        when(cityService.getAllCities()).thenReturn(List.of());

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== createCity ====================

    @Test
    void createCity_shouldReturn201_whenDataIsValid() throws Exception {
        CityRequest request = new CityRequest();
        request.setName("Tehran");

        CityResponse response = CityResponse.builder().id(1L).name("Tehran").build();

        when(cityService.create(any(CityRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/cities")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Tehran"));
    }

    @Test
    void createCity_shouldReturn400_whenNameIsBlank() throws Exception {
        CityRequest request = new CityRequest();
        request.setName("");

        mockMvc.perform(post("/api/cities")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== deleteCity ====================

    @Test
    void deleteCity_shouldReturn200_whenNotInUse() throws Exception {
        mockMvc.perform(delete("/api/cities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("CITY_DELETED"));
    }

    @Test
    void deleteCity_shouldReturn404_whenCityDoesNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new CityNotFoundException("شهر یافت نشد"))
                .when(cityService).delete(eq(99L));

        mockMvc.perform(delete("/api/cities/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCity_shouldReturn409_whenCityIsInUse() throws Exception {
        org.mockito.Mockito.doThrow(new CityInUseException("این شهر در حال استفاده است"))
                .when(cityService).delete(eq(1L));

        mockMvc.perform(delete("/api/cities/1"))
                .andExpect(status().isConflict());
    }
}
