package com.example.secondhand.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger + Scalar documentation configuration.
 * <p>
 * Docs UIs:
 * - Swagger UI: /swagger-ui.html
 * - Scalar UI : /scalar
 * - Raw spec  : /v3/api-docs
 * <p>
 * How to get a JWT token for testing:
 * 1) POST /api/auth/register (or use an existing account).
 * 2) POST /api/auth/login -> response contains "token".
 * 3) Click "Authorize" (Swagger UI) or the lock/auth icon (Scalar),
 *    paste the token value WITHOUT the word "Bearer", and confirm.
 * 4) Protected endpoints will then be called with "Authorization: Bearer <token>" automatically.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * ساخت و پیکربندی Bean اصلی مستندات {@link OpenAPI} برنامه.
     * <p>
     * این متد اطلاعات کلی API (عنوان، نسخه، توضیحات، اطلاعات تماس و لایسنس)،
     * طرح امنیتی Bearer JWT با نام {@value #SECURITY_SCHEME_NAME}، و پاسخ‌های
     * خطای رایج ({@code 400}, {@code 401}, {@code 403}, {@code 404}, {@code 409})
     * را به‌همراه نمونه پاسخ فارسی برای هرکدام تعریف می‌کند تا در Swagger UI و
     * Scalar به‌صورت یکپارچه نمایش داده شوند.
     * </p>
     *
     * @return شیء {@link OpenAPI} پیکربندی‌شده که توسط springdoc-openapi برای
     *         تولید مستندات API استفاده می‌شود
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecondHand API")
                        .version("1.0.0")
                        .description("""
                                ## سامانه ثبت آگهی خرید و فروش دست دوم

                                ### ویژگی‌های اصلی
                                - 🔐 احراز هویت با **JWT**
                                - 📦 مدیریت آگهی (ثبت، ویرایش، حذف، جست‌وجو و فیلتر)
                                - 💬 گفت‌وگو (چت) بین خریدار و فروشنده
                                - ⭐ امتیازدهی به فروشنده
                                - ❤️ علاقه‌مندی‌ها
                                - 🛠️ پنل مدیریت (بررسی/تایید/رد آگهی، مدیریت کاربران و دسته‌بندی‌ها)

                                ### نحوه گرفتن توکن برای تست
                                1. از `POST /api/auth/register` ثبت‌نام کن (یا از یک حساب موجود استفاده کن)
                                2. از `POST /api/auth/login` توکن بگیر
                                3. روی دکمه‌ی **Authorize** (سواگر) یا آیکن قفل/Auth (اسکالر) کلیک کن
                                4. توکن رو وارد کن — **بدون** کلمه‌ی Bearer

                                ### نقش‌ها
                                | نقش | دسترسی |
                                |-----|---------|
                                | `USER` | عملیات عادی (ثبت آگهی، چت، علاقه‌مندی، امتیازدهی) |
                                | `ADMIN` | مدیریت کامل (تایید/رد آگهی، مدیریت کاربران و دسته‌بندی‌ها) |
                                """)
                        .contact(new Contact()
                                .name("SecondHand Team")
                                .url("https://github.com"))
                        .license(new License()
                                .name("Academic Project — AUT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("توکن JWT دریافتی از /api/auth/login را وارد کن — **بدون** کلمه Bearer."))
                        .addResponses("400", new ApiResponse()
                                .description("داده ورودی نامعتبر")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().addExamples("default",
                                                new Example().value("""
                                                        {
                                                          "error": "داده ورودی نامعتبر است"
                                                        }""")))))
                        .addResponses("401", new ApiResponse()
                                .description("احراز هویت ناموفق")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().addExamples("default",
                                                new Example().value("""
                                                        {
                                                          "error": "ابتدا وارد حساب کاربری شوید"
                                                        }""")))))
                        .addResponses("403", new ApiResponse()
                                .description("دسترسی ممنوع")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().addExamples("default",
                                                new Example().value("""
                                                        {
                                                          "error": "کاربر اجازه انجام این عملیات را ندارد"
                                                        }""")))))
                        .addResponses("404", new ApiResponse()
                                .description("یافت نشد")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().addExamples("default",
                                                new Example().value("""
                                                        {
                                                          "error": "مورد مورد نظر یافت نشد"
                                                        }""")))))
                        .addResponses("409", new ApiResponse()
                                .description("تکراری / تعارض وضعیت")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().addExamples("default",
                                                new Example().value("""
                                                        {
                                                          "error": "این مورد قبلاً ثبت شده است"
                                                        }"""))))));
    }
}
