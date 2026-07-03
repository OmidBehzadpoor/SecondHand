package com.example.secondhand.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramBotService(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // TODO : بعدا توسعه داده شود
    }

    public void sendVerificationCode(String chatId, String code) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("کد تایید شماره تلفن شما:\n\n" +
                "🔐 " + code + "\n\n" +
                "این کد ۱۰ دقیقه اعتبار دارد.");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("خطا در ارسال پیام تلگرام: {}", e.getMessage());
        }
    }
}