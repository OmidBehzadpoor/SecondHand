package com.example.secondhand.service;

import com.example.secondhand.model.User;
import com.example.secondhand.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final UserRepository userRepository;

    @Value("${telegram.bot.username}")
    private String botUsername;

    public TelegramBotService(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.proxy.host:}") String proxyHost,
            @Value("${telegram.proxy.port:0}") int proxyPort,
            UserRepository userRepository) {
        super(buildOptions(proxyHost, proxyPort), botToken);
        this.userRepository = userRepository;
    }

    private static DefaultBotOptions buildOptions(String proxyHost, int proxyPort) {
        DefaultBotOptions options = new DefaultBotOptions();
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            options.setProxyHost(proxyHost);
            options.setProxyPort(proxyPort);
            options.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        }
        return options;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        Message message = update.getMessage();
        String chatId = message.getChatId().toString();

        // کاربر شماره رو share کرد
        if (message.hasContact()) {
            handleContactShared(message, chatId);
            return;
        }

        // کاربر /start زد
        if (message.hasText() && message.getText().equals("/start")) {
            sendSharePhoneButton(chatId);
        }
    }

    private void handleContactShared(Message message, String chatId) {
        Contact contact = message.getContact();
        String phone = normalizePhone(contact.getPhoneNumber());

        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isEmpty()) {
            sendMessage(chatId, "❌ شماره تلفن شما در سیستم ثبت نشده است.\nلطفاً ابتدا در سامانه ثبت نام کنید.");
            return;
        }

        User user = userOpt.get();

        if (user.isPhoneVerified()) {
            sendMessage(chatId, "✅ شماره تلفن شما قبلاً تایید شده است.");
            return;
        }

        // کد تایید بساز
        String code = generateCode();
        user.setTelegramChatId(chatId);
        user.setPhoneVerificationCode(code);
        user.setPhoneVerificationExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        sendMessage(chatId, "کد تایید شما:\n\n🔐 " + code + "\n\nاین کد ۱۰ دقیقه اعتبار دارد.");
    }

    private void sendSharePhoneButton(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("برای تایید شماره تلفن، روی دکمه زیر کلیک کنید:");

        // ساخت دکمه share phone
        KeyboardButton button = new KeyboardButton();
        button.setText("📱 اشتراک‌گذاری شماره تلفن");
        button.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(button);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("خطا در ارسال دکمه: {}", e.getMessage());
        }
    }

    public void sendVerificationCode(String chatId, String code) {
        sendMessage(chatId, "کد تایید شما:\n\n🔐 " + code + "\n\nاین کد ۱۰ دقیقه اعتبار دارد.");
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("خطا در ارسال پیام تلگرام: {}", e.getMessage());
            log.error("جزئیات خطا: ", e);
        }
    }

    private String normalizePhone(String phone) {
        // تلگرام شماره رو با کد کشور میفرسته مثلاً 989123456789
        // تبدیل به فرمت 09123456789
        if (phone.startsWith("98")) {
            return "0" + phone.substring(2);
        }
        if (phone.startsWith("+98")) {
            return "0" + phone.substring(3);
        }
        return phone;
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}