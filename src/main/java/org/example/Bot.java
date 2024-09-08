package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class Bot extends TelegramLongPollingBot {

    public Bot(String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return "Java test bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage()) {
            switch (update.getMessage().getText()) {
                case "/start" -> {
                    try {
                        sendMessage(update.getMessage().getFrom().getId(), "Приветствую вас мой дорогой друг! это тестовый телеграм бот!", "Button 1", "Button 2");
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            switch (update.getCallbackQuery().getData()) {
                case "test1" -> {
                    try {
                        sendMessage(update.getCallbackQuery().getFrom().getId(), "Команда 1 выполнено успешно!");
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                case "test2" -> {
                    try {
                        sendMessage(update.getCallbackQuery().getFrom().getId(), "Команда 2 выполнено успешно!", "Кнопка 3", "Кнопка 4");
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    // method 1
    public void sendMessage(long id, String text) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(id)
                .build();
        sendApiMethod(sendMessage);
    }
    // method2 перегрузка
    public void sendMessage(long id, String text, String nameButton1, String nameButton2) throws TelegramApiException {
        InlineKeyboardMarkup keyboardMarkup = sendCallbackQuery(nameButton1, nameButton2);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(id)
                .text(text)
                .replyMarkup(keyboardMarkup)
                .build();
        execute(sendMessage);
    }

    public InlineKeyboardMarkup sendCallbackQuery(String nameButton1, String nameButton2) {
        InlineKeyboardButton button1 = InlineKeyboardButton.builder()
                .text(nameButton1).callbackData("test1")
                .build();
        InlineKeyboardButton button2 = InlineKeyboardButton.builder()
                .text(nameButton2).callbackData("test2")
                .build();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(button1, button2))
                .build();
        return keyboard;
    }
}
