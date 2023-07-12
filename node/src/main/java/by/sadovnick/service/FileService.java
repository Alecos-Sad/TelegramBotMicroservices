package by.sadovnick.service;

import by.sadovnick.entity.AppDocument;
import by.sadovnick.entity.AppPhoto;
import by.sadovnick.service.enums.LinkType;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Сервис для работы с файлами.
 */
public interface FileService {
    AppDocument processDoc(Message message);
    AppPhoto processPhoto(Message message);
    String generateLink(Long docId, LinkType linkType);
}
