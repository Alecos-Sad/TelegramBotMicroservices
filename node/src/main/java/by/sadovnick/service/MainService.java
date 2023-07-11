package by.sadovnick.service;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Главный сервис. Сервис является связующим звеном между базой данных и продюсером сообщений
 *  * который будет передавать сообщения из RabbitMQ.
 */
public interface MainService {
    void processTextMessage(Update update);
    void processDocMessage(Update update);
    void processPhotoMessage(Update update);

}
