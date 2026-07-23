package com.example.secondhand.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * <h2>WebConfig</h2>
 * <p>
 * کلاس پیکربندی Spring MVC مسئول در دسترس قرار دادن <b>فایل‌های آپلودشده</b>
 * (تصاویر آگهی‌ها) به‌عنوان منابع استاتیک قابل دسترسی از طریق HTTP.
 * </p>
 * <p>
 * با پیاده‌سازی {@link WebMvcConfigurer}، مسیر فیزیکی ذخیره‌سازی تصاویر (که از
 * طریق {@code app.upload.dir} در تنظیمات برنامه خوانده می‌شود) به مسیر عمومی
 * {@code /uploads/advertisements/**} نگاشت می‌شود.
 * </p>
 *
 * @author تیم بک‌اند
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * ثبت هندلر منابع استاتیک برای نگاشت مسیر {@code /uploads/advertisements/**}
     * به دایرکتوری فیزیکی ذخیره‌سازی تصاویر آگهی‌ها روی دیسک.
     *
     * @param registry رجیستری منابع Spring MVC که هندلر جدید به آن اضافه می‌شود
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/advertisements/**")
                .addResourceLocations(Paths.get(uploadDir).toUri().toString());
    }
}
