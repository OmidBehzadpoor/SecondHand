package com.example.secondhand.service;

import com.example.secondhand.dto.CityRequest;
import com.example.secondhand.dto.response.CityResponse;
import com.example.secondhand.exception.CityInUseException;
import com.example.secondhand.exception.CityNotFoundException;
import com.example.secondhand.model.City;
import com.example.secondhand.repository.AdvertisementRepository;
import com.example.secondhand.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;
    private final AdvertisementRepository advertisementRepository;

    public CityResponse create(CityRequest request) {
        City city = cityRepository.save(City.builder().name(request.getName()).build());

        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }

    public void delete(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new CityNotFoundException("شهر یافت نشد"));
        if (advertisementRepository.existsByCityId(id)) {
            throw new CityInUseException("این شهر در آگهی‌های فعال استفاده شده و قابل حذف نیست");
        }
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

