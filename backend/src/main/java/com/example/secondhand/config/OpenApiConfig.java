package com.example.secondhand.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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
                                - 🔐 احراز هویت با JWT
                                - 📧 تایید ایمیل با Resend
                                - 📱 تایید شماره تلفن با تلگرام
                                - 🤖 Google reCAPTCHA v2
                                
                                ### نحوه استفاده
                                ابتدا از endpoint `/api/auth/login` توکن بگیر،
                                سپس روی دکمه Authorize کلیک کن و توکن رو وارد کن.
                                """))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("توکن JWT رو اینجا وارد کن — بدون Bearer")));
    }
}