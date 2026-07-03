package com.example.secondhand.service;

import com.example.secondhand.dto.CityRequest;
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

    public List<City> getAll() {
        return cityRepository.findAll();
    }

}
