package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
         // токен бота в телеграме в избранном 09.09.2024 00:30
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            Bot bot = new Bot("");

//             Удаляем вебхук перед регистрацией бота
            try {
                // Создаем запрос на удаление вебхука
                DeleteWebhook deleteWebhook = new DeleteWebhook();
                bot.execute(deleteWebhook);
                System.out.println("Webhook removed successfully!");
            } catch (TelegramApiRequestException e) {
                // Обрабатываем ошибку 404 (вебхук не найден)
                if (e.getErrorCode() == 404) {
                    System.out.println("No webhook found, skipping removal.");
                } else {
                    // Обрабатываем любые другие ошибки
                    System.err.println("An error occurred while removing the webhook: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (TelegramApiException e) {
                // Обрабатываем общие ошибки выполнения запроса
                System.err.println("A general exception occurred while removing the webhook: " + e.getMessage());
                e.printStackTrace();
            }

            botsApi.registerBot(bot);
            System.out.println("Bot started in polling mode!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}