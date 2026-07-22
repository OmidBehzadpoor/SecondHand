package com.example.secondhand.service;

import com.example.secondhand.dto.CityRequest;
import com.example.secondhand.dto.response.CityResponse;
import com.example.secondhand.exception.CityInUseException;
import com.example.secondhand.exception.CityNotFoundException;
import com.example.secondhand.model.City;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @InjectMocks
    private CityService cityService;

    private CityRequest request(String name) {
        CityRequest request = new CityRequest();
        request.setName(name);
        return request;
    }

    // ==================== create ====================

    @Test
    void create_shouldCreateCity_whenNameIsValid() {
        City saved = City.builder().id(1L).name("Tehran").build();

        when(cityRepository.save(any(City.class))).thenReturn(saved);

        CityResponse response = cityService.create(request("Tehran"));

        assertEquals(1L, response.getId());
        assertEquals("Tehran", response.getName());
    }

    // ==================== delete ====================

    @Test
    void delete_shouldDeleteCity_whenNotInUse() {
        City city = City.builder().id(1L).name("Tehran").build();

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(advertisementRepository.existsByCityId(1L)).thenReturn(false);

        cityService.delete(1L);

        verify(cityRepository, times(1)).delete(city);
    }

    @Test
    void delete_shouldThrowCityNotFoundException_whenCityDoesNotExist() {
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CityNotFoundException.class, () -> cityService.delete(99L));

        verify(cityRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowCityInUseException_whenUsedByAdvertisement() {
        City city = City.builder().id(1L).name("Tehran").build();

        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(advertisementRepository.existsByCityId(1L)).thenReturn(true);

        assertThrows(CityInUseException.class, () -> cityService.delete(1L));

        verify(cityRepository, never()).delete(any());
    }

    // ==================== getAllCities ====================

    @Test
    void getAllCities_shouldReturnAllCities_whenSomeExist() {
        City c1 = City.builder().id(1L).name("Tehran").build();
        City c2 = City.builder().id(2L).name("Isfahan").build();

        when(cityRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CityResponse> result = cityService.getAllCities();

        assertEquals(2, result.size());
    }

    @Test
    void getAllCities_shouldReturnEmptyList_whenNoneExist() {
        when(cityRepository.findAll()).thenReturn(List.of());

        List<CityResponse> result = cityService.getAllCities();

        assertTrue(result.isEmpty());
    }
}
