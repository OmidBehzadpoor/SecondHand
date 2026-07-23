package com.example.secondhand.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * <h2>City</h2>
 * <p>
 * موجودیت (Entity) نگاشت‌شده به جدول {@code cities}، نماینده‌ی یک <b>شهر</b>
 * که در فرم ثبت آگهی و فیلترهای جست‌وجو مورد استفاده قرار می‌گیرد.
 * </p>
 *
 * @author تیم بک‌اند
 * @see com.example.secondhand.service.CityService
 */
@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    /** شناسه یکتای شهر (کلید اصلی، تولید خودکار). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** نام شهر؛ باید در سامانه یکتا باشد. */
    @Column(nullable = false, unique = true)
    private String name;

}
