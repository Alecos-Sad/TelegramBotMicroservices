package by.sadovnick.service.impl;

import by.sadovnick.dao.AppDocumentDao;
import by.sadovnick.dao.BinaryContentDao;
import by.sadovnick.entity.AppDocument;
import by.sadovnick.entity.BinaryContent;
import by.sadovnick.exceptions.UploadFileException;
import by.sadovnick.service.FileService;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

/**
 * Получит телеграмм message, выполнит все действия для скачивания файла и
 * сохранит его в бд.
 */
@Log4j
@Service
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    private final AppDocumentDao appDocumentDao;
    private final BinaryContentDao binaryContentDao;

    public FileServiceImpl(AppDocumentDao appDocumentDao, BinaryContentDao binaryContentDao) {
        this.appDocumentDao = appDocumentDao;
        this.binaryContentDao = binaryContentDao;
    }

    /**
     * Сохранение файла в бд.
     */
    @Override
    public AppDocument processDoc(Message telegramMessage) {
        //достаем id документа
        String field = telegramMessage.getDocument().getFileId();
        //запрос к серверу телеграмма
        ResponseEntity<String> response = getFieldPath(field);
        if (response.getStatusCode() == HttpStatus.OK) {
            //пришедшие данные из телеги
            JSONObject jsonObject = new JSONObject(response.getBody());
            //достаем путь
            String filePath = String.valueOf(jsonObject
                    .getJSONObject("result")
                    .getString("file_path"));
            //скачиваем по этому пути
            byte[] fileInByte = downloadFile(filePath);
            //объект до сохранения в бд
            BinaryContent transientBinaryContent = BinaryContent.builder()
                    .fileArrayOfByte(fileInByte)
                    .build();
            //объект после сохранения в бд. В контексте лежит и с ключом присвоенным бд.
            BinaryContent persistentBinaryContent = binaryContentDao.save(transientBinaryContent);
            Document telegramDoc = telegramMessage.getDocument();
            //генерим окончательную сущность AppDocument
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDao.save(Objects.requireNonNull(transientAppDoc));
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    /**
     * Формирование AppDoc из телеграммовского документа.
     */
    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramField(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }

    /**
     * Скачивание файла
     */
    private byte[] downloadFile(String filePath) {
        String fulUri = fileStorageUri
                .replace("{token}", token)
                .replace("{filePath}", filePath);
        URL urlObj = null;
        try {
            urlObj = new URL(fulUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }
        //todo провести оптимизацию.
        //скачивается одним куском и хранится в оперативке. Что плохо.
        try (InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    /**
     * Http запрос к телеграмму.
     */
    private ResponseEntity<String> getFieldPath(String field) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(httpHeaders);
        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class
                , token
                , field
        );
    }
}
