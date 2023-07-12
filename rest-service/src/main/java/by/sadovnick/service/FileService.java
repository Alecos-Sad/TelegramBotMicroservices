package by.sadovnick.service;

import by.sadovnick.entity.AppDocument;
import by.sadovnick.entity.AppPhoto;
import by.sadovnick.entity.BinaryContent;
import org.springframework.core.io.FileSystemResource;

/**
 * Класс для работы с полученным файлом с телеграма.
 */
public interface FileService {
    AppDocument getDocument(String id);
    AppPhoto getPhoto(String id);
    FileSystemResource getFileSystemResource(BinaryContent binaryContent);
}
