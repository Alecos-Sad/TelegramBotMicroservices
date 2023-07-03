package by.sadovnick.controller;

import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Telegramm Bot главный класс
 */
@Component
@Log4j
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;
    private UpdateController updateController;

    @Override
    public String getBotUsername() {
        return botName;
    }

    public TelegramBot(UpdateController updateController) {
        this.updateController = updateController;
    }

    /**
     * Метод постинициализации. Внедрение UpdateController через init - чтобы избежать циклической зависимости
     *  между{@link TelegramBot} и {@link UpdateController}.
     */
    @PostConstruct
    public void init(){
        updateController.registerBot(this);
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    /**
     * Получение и отправка сообщений ботом
     * @param update - полученное сообщение
     */
    @Override
    public void onUpdateReceived(Update update) {
        updateController.processUpdate(update);
    }

    /**
     * Ответ бота
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
}
