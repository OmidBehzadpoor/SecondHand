package com.example.secondhand.service;

import com.example.secondhand.dto.CityRequest;
import com.example.secondhand.dto.response.CityResponse;
import com.example.secondhand.exception.CityNotFoundException;
import com.example.secondhand.model.City;
import com.example.secondhand.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;

    public City create(CityRequest request) {
        City city = City.builder().name(request.getName()).build();
        return cityRepository.save(city);
    }

    public void delete(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new CityNotFoundException("شهر یافت نشد"));
        cityRepository.delete(city);
    }

    public List<CityResponse> getAllCities() {
        return cityRepository.findAll()
                .stream()
                .map(city -> CityResponse.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .build())
                .toList();
    }
}

