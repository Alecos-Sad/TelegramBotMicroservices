package by.sadovnick.service;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Сервис для считывания сообщений из брокера
 */
public interface ConsumerService {
    void consumeTextMessageUpdates(Update update);
    void consumeDocumentMessageUpdates(Update update);
    void consumePhotoMessageUpdates(Update update);
}
