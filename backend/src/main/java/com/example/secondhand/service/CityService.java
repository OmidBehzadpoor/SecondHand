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

/**
 * <h2>CityService</h2>
 * <p>
 * سرویس مدیریت <b>شهرها</b> در سامانه. این کلاس عملیات ساده‌ی مربوط به ایجاد،
 * حذف و دریافت لیست شهرهایی که در فرم ثبت آگهی و فیلترهای جست‌وجو استفاده
 * می‌شوند را فراهم می‌کند.
 * </p>
 * <p>
 * پیش از حذف یک شهر، بررسی می‌شود که هیچ آگهی‌ای به آن شهر ارجاع نداده باشد.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.model.City
 */
@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;
    private final AdvertisementRepository advertisementRepository;

    /**
     * ایجاد یک شهر جدید در سامانه.
     *
     * @param request اطلاعات شهر جدید شامل نام آن
     * @return {@link CityResponse} حاوی اطلاعات شهر تازه‌ایجادشده
     */
    public CityResponse create(CityRequest request) {
        City city = cityRepository.save(City.builder().name(request.getName()).build());

        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .build();
    }

    /**
     * حذف یک شهر از سامانه.
     * <p>
     * در صورتی که این شهر در هر یک از آگهی‌های ثبت‌شده استفاده شده باشد،
     * امکان حذف آن وجود ندارد.
     * </p>
     *
     * @param id شناسه شهری که باید حذف شود
     * @throws CityNotFoundException در صورتی که شهر مورد نظر یافت نشود
     * @throws CityInUseException در صورتی که این شهر در آگهی‌های فعال استفاده شده باشد
     */
    public void delete(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new CityNotFoundException("شهر یافت نشد"));
        if (advertisementRepository.existsByCityId(id)) {
            throw new CityInUseException("این شهر در آگهی‌های فعال استفاده شده و قابل حذف نیست");
        }
        cityRepository.delete(city);
    }

    /**
     * دریافت لیست تمام شهرهای ثبت‌شده در سامانه.
     *
     * @return لیستی از {@link CityResponse} شامل تمام شهرها
     */
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
