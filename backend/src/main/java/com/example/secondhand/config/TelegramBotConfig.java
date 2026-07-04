package com.example.secondhand.config;

import com.example.secondhand.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final TelegramBotService telegramBotService;
    private boolean registered = false;

    @Value("${app.phone.verification.enabled:false}")
    private boolean phoneVerificationEnabled;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (registered || !phoneVerificationEnabled) return;
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(telegramBotService);
            registered = true;
            log.info("بات تلگرام با موفقیت راه‌اندازی شد");
        } catch (TelegramApiException e) {
            log.error("بات تلگرام راه‌اندازی نشد — بقیه سرویس‌ها نرمال کار میکنن: {}", e.getMessage());
        }
    }
}