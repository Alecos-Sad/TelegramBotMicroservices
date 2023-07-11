package by.sadovnick.service;

import by.sadovnick.entity.AppDocument;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Сервис для работы с файлами.
 */
public interface FileService {
    AppDocument processDoc(Message message);
}
