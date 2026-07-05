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

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecondHand API")
                        .version("1.0.0")
                        .description("""
                                ## سامانه خرید و فروش کالای دست دوم
                                
                                ### ویژگی‌های امنیتی
                                - 🔐 احراز هویت با **JWT**
                                - 📧 تایید ایمیل با **Resend**
                                - 📱 تایید شماره تلفن با **Telegram**
                                - 🤖 **Google reCAPTCHA v2**
                                
                                ### نحوه استفاده
                                1. از endpoint `/api/auth/register` ثبت‌نام کن
                                2. از endpoint `/api/auth/login` توکن بگیر
                                3. روی دکمه **Authorize** کلیک کن
                                4. توکن رو وارد کن — **بدون** کلمه Bearer
                                
                                ### نقش‌ها
                                | نقش | دسترسی |
                                |-----|---------|
                                | `USER` | عملیات عادی |
                                | `ADMIN` | مدیریت کامل |
                                """)
                        .contact(new Contact()
                                .name("Omid Behzadpoor")
                                .email("omidbehzadpoor1386@gmail.com"))
                        .license(new License()
                                .name("Academic Project — AUT")
                                .url("https://github.com/OmidBehzadpoor/SecondHand")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("توکن JWT رو اینجا وارد کن — **بدون** Bearer"))
                        // خطاهای استاندارد — فرمت درست با success: false
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
                                                          "error": "توکن نامعتبر یا منقضی شده است"
                                                        }""")))))
                        .addResponses("403", new ApiResponse()
                                .description("دسترسی ممنوع")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().addExamples("default",
                                                new Example().value("""
                                                        {
                                                          "error": "دسترسی ممنوع است"
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
                                .description("تکراری")
                                .content(new Content().addMediaType("application/json",
                                        new MediaType().addExamples("default",
                                                new Example().value("""
                                                        {
                                                          "error": "این مورد قبلاً ثبت شده است"
                                                        }"""))))));
    }
}