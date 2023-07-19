package by.sadovnick.controller;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;

/**
 * Telegramm Bot главный класс
 */
@Component
@Log4j
public class TelegramBot extends TelegramWebhookBot {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.uri}")
    private String botUri;
    private UpdateProcessor updateProcessor;

    @Override
    public String getBotUsername() {
        return botName;
    }

    public TelegramBot(UpdateProcessor updateProcessor) {
        this.updateProcessor = updateProcessor;
    }

    /**
     * Метод постинициализации. Внедрение UpdateProcessor через init - чтобы избежать циклической зависимости
     * между{@link TelegramBot} и {@link UpdateProcessor}.
     */
    @PostConstruct
    public void init() {
            updateProcessor.registerBot(this);
            try {
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(botUri)
                    .build();
            this.setWebhook(setWebhook);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Ответ бота
     *
     * @param message - посылаемое сообщение
     */
    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        }
    }

    /**
     * Не нужен. Оставляем null.
     */
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    /**
     * Путь на который тг-бот будет отправлять updates
     */
    @Override
    public String getBotPath() {
        return "/update";
    }
}
