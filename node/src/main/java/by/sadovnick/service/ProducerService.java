package by.sadovnick.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Сервис для отправки ответов с ноды в брокер
 */
public interface ProducerService {
    void produceAnswer(SendMessage sendMessage);
}
