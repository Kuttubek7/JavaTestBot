package org.example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "7289283734:AAGCxXpSlVDRKQJtZ6pEYIFByme0iEIwIy0";
    private static final String BOT_USERNAME = "BudgetAssistantBot";

    // Класс для хранения промежуточных данных пользователя
    private static class UserData {
        public double budget;
        public List<Expense> expenses;
    }

    // класс для представления отдельного расхода
    private static class Expense {
        public Date date; // Дата расхода
        public double amount; // Сумма расхода
        public String category; // Категория расхода
    }

    private enum UserState {
        WAITING_FOR_BUDGET,
        WAITING_FOR_AMOUNT,
        WAITING_FOR_CATEGORY,
        WAITING_FOR_REPORT_PERIOD
    }

    // Отслеживание состояния пользователя
//    private enum UserState { WAITING_FOR_REPORT_PERIOD };
    private Map<Long, UserState> userStates = new HashMap<>();
    private Map<Long, UserData> userData = new HashMap<>(); // Хранение промежуточных данных

    public Bot(String botToken) { super(BOT_TOKEN); }

    @Override
    public String getBotToken() { return BOT_TOKEN; }

    @Override
    public String getBotUsername() { return BOT_USERNAME; }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                switch (messageText) {
                    case "/start":
                        sendMessage(update.getMessage().getFrom().getId(), "Приветствую вас мой дорогой друг! Начнем управление вашим бюджетом.");
                        break;
                    case "/help":
                        sendMessage(update.getMessage().getFrom().getId(), "Доступные команды:\n/add_expense\n/create_budget\n/report");
                        break;
                    case "/add_expense":
                        sendMessage(update.getMessage().getFrom().getId(), "Пожалуйста введите сумму вашего расхода: ");
                        userStates.put(chatId, UserState.WAITING_FOR_AMOUNT);
                        break;
                    case "/create_budget":
                        sendMessage(chatId, "Пожалуйста, введите сумму вашего месячного бюджета:");
                        userStates.put(chatId, UserState.WAITING_FOR_BUDGET);
                        break;
                    case "/report":
                        sendMessage(chatId, "Пожалуйста, укажите период для отчета (например неделя, месяц): ");
                        userStates.put(chatId, UserState.WAITING_FOR_REPORT_PERIOD);
                        break;
                    default:
                        handleUserInput(chatId, messageText);
                        break;
                }
            }

    }

    private void handleUserInput(long chatId, String messageText) {
        UserState state = userStates.getOrDefault(chatId, null);
        if (state == null) {
            sendMessage(chatId, "Не понял вас. Попробуйте еще раз.");
            return;
        }

        switch (state) {
            case WAITING_FOR_REPORT_PERIOD:
                if (isValidReportPeriod(messageText)) {
                    generateReport(chatId, messageText); // Генерируем отчет
                    userStates.remove(chatId); // Сбрасываем состояние пользователя
                } else {
                    sendMessage(chatId, "Неправильный период. Пожалуйста, попробуйте снова.");
                }
                break;
            case WAITING_FOR_AMOUNT:
                if (isValidAmount(messageText)) {
                    double amount = Double.parseDouble(messageText);
                    UserData data = userData.get(chatId);
                    Expense expense = new Expense();
                    expense.amount = amount;
                    userData.put(chatId, data);
                    sendMessage(chatId, "Теперь выберите категорию расхода (например, еда, транспорт):");
                    userStates.put(chatId, UserState.WAITING_FOR_CATEGORY);
                    data.expenses.add(expense);
                } else {
                    sendMessage(chatId, "Неверная сумма. Пожалуйста, попробуйте снова.");
                }
                break;
            case WAITING_FOR_CATEGORY:
                if (isValidCategory(messageText)) {
                    UserData data = userData.get(chatId);
                    String category = messageText.toLowerCase(); // Приводим к нижнему регистру для унификации
                    try {

                        saveExpense(chatId, expence.amount, category); // Сохраняем расход
                        sendMessage(chatId, "Расход успешно добавлен!");
                        userStates.remove(chatId); // Сбрасываем состояние пользователя
                        userData.remove(chatId); // Удаляем промежуточные данные
                    } catch (Exception e) {
                        sendMessage(chatId, "Произошла ошибка при добавлении расхода.");
                    }
                } else {
                    sendMessage(chatId, "Неверная категория. Пожалуйста, попробуйте снова.");
                }
                break;
        }
    }

    private boolean isValidReportPeriod(String period) {
        // Простая проверка валидности периода (можно расширить)
        return period.equalsIgnoreCase("неделя") || period.equalsIgnoreCase("месяц");
    }

    private void generateReport(long chatId, String reportPeriod) {
        UserData data = userData.get(chatId);
        double totalExpenses = calculateTotalExpenses(data.expenses, reportPeriod);
        double remainingBudget = data.budget - totalExpenses;
        String report = formatReport(totalExpenses, remainingBudget, data.expenses, reportPeriod);
        sendMessage(chatId, report);
    }

    private double calculateTotalExpenses(List<Expense> expenses, String reportPeriod) {
        // Логика расчета общих расходов за период
        double total = 0.0;
        for (Expense expense : expenses) {
            if (isInReportPeriod(expense.date, reportPeriod)) {
                total += expense.amount;
            }
        }
        return total;
    }

    private boolean isInReportPeriod(Date expenseDate, String reportPeriod) {
        // Логика проверки, попадает ли дата расхода в указанный период
        // Зависит от реализации даты и периода
        return true; // Временная заглушка
    }

    private String formatReport(double totalExpenses, double remainingBudget, List<Expense> expenses, String reportPeriod) {
        StringBuilder sb = new StringBuilder();
        sb.append("Отчет за ").append(reportPeriod).append(":\n");
        sb.append("Общие расходы: ").append(totalExpenses).append("\n");
        sb.append("Оставшийся бюджет: ").append(remainingBudget).append("\n");
        sb.append("Список расходов:\n");
        for (Expense expense : expenses) {
            sb.append("- ").append(expense.amount).append(" ").append(expense.category).append(" (").append(expense.date).append(")\n");
        }
        return sb.toString();
    }


    private boolean isValidBudget(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidAmount(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidCategory(String input) {
        // Можно добавить проверку на допустимые категории, если такие существуют
        return !input.isEmpty();
    }

    private void saveExpense(long chatId, double amount, String category) throws Exception {
        // Здесь сохраняется расход в базу данных или файл
        System.out.println("Saving expense for user " + chatId + ": " + amount + " " + category);
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message); // Отправляем сообщение
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
