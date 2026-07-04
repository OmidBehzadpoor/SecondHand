package com.example.secondhand.config;

import com.example.secondhand.service.TelegramBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@RequiredArgsConstructor
public class TelegramBotConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final TelegramBotService telegramBotService;
    private boolean registered = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (registered) return;
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(telegramBotService);
            registered = true;
        } catch (TelegramApiException e) {
            throw new RuntimeException("خطا در راه‌اندازی بات تلگرام", e);
        }
    }
}