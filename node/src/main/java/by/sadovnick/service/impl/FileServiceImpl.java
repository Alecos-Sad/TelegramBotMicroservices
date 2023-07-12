package by.sadovnick.service.impl;

import by.sadovnick.CryptoTool;
import by.sadovnick.dao.AppDocumentDao;
import by.sadovnick.dao.AppPhotoDao;
import by.sadovnick.dao.BinaryContentDao;
import by.sadovnick.entity.AppDocument;
import by.sadovnick.entity.AppPhoto;
import by.sadovnick.entity.BinaryContent;
import by.sadovnick.exceptions.UploadFileException;
import by.sadovnick.service.FileService;
import by.sadovnick.service.enums.LinkType;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

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
    @Value("${link.address}")
    private String linkAddress;
    private final AppDocumentDao appDocumentDao;
    private final BinaryContentDao binaryContentDao;
    private final AppPhotoDao appPhotoDao;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentDao appDocumentDao, BinaryContentDao binaryContentDao,
                           AppPhotoDao photoDao, AppPhotoDao appPhotoDao, CryptoTool cryptoTool) {
        this.appDocumentDao = appDocumentDao;
        this.binaryContentDao = binaryContentDao;
        this.appPhotoDao = appPhotoDao;
        this.cryptoTool = cryptoTool;
    }

    /**
     * Сохранение файла в бд.
     */
    @Override
    public AppDocument processDoc(Message telegramMessage) {
        Document telegramDoc = telegramMessage.getDocument();
        //достаем id документа
        String field = telegramDoc.getFileId();
        //запрос к серверу телеграмма
        ResponseEntity<String> response = getFieldPath(field);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            //генерим окончательную сущность AppDocument
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDao.save(Objects.requireNonNull(transientAppDoc));
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    /**
     * Сохранение фото в бд.
     */
    @Override
    public AppPhoto processPhoto(Message telegramMessage) {
        //решение проблемы с маленьким скачанным размером фото
        //Фото сохраняется в бд как массив данных разных размеров. И для того чтобы при загрузки из бд
        //не достать первую самую маленькую картинку, достаем самый последний. Телега позволяет хранить
        //в одном файле фото нескольких размеров. Поэтому и класс называется PhotoSize.
        int photoSizeCount = telegramMessage.getPhoto().size();
        int photoIndex = photoSizeCount > 1 ? telegramMessage.getPhoto().size() - 1 : 0;
        //todo реализовать обработку альбома
        PhotoSize telegramPhoto = telegramMessage.getPhoto().get(photoIndex);
        //достаем id документа
        String field = telegramPhoto.getFileId();
        //запрос к серверу телеграмма
        ResponseEntity<String> response = getFieldPath(field);
        if (response.getStatusCode() == HttpStatus.OK) {
            BinaryContent persistentBinaryContent = getPersistentBinaryContent(response);
            //генерим окончательную сущность AppDocument
            AppPhoto transientAppPhoto = buildTransientAppPhoto(telegramPhoto, persistentBinaryContent);
            return appPhotoDao.save(Objects.requireNonNull(transientAppPhoto));
        } else {
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }

    /**
     * Получает хэш и вставляет его в ссылку.
     */
    @Override
    public String generateLink(Long docId, LinkType linkType) {
        String hash = cryptoTool.hashOf(docId);
        return "http:// " + linkAddress + "/" + linkType + "?id=" + hash;
    }

    /**
     * Сохранение объекта в бд
     */
    private BinaryContent getPersistentBinaryContent(ResponseEntity<String> response) {
        String filePath = getFilePath(response);
        //скачиваем по этому пути
        byte[] fileInByte = downloadFile(filePath);
        //объект до сохранения в бд
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileArrayOfByte(fileInByte)
                .build();
        //объект после сохранения в бд. В контексте лежит и с ключом присвоенным бд.
        return binaryContentDao.save(transientBinaryContent);
    }

    /**
     * Получение пути файла
     */
    private String getFilePath(ResponseEntity<String> response) {
        //пришедшие данные из телеги
        JSONObject jsonObject = new JSONObject(response.getBody());
        //достаем путь
        return String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
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
     * Формирование AppPhoto из  телеграмовского докуммента
     */
    private AppPhoto buildTransientAppPhoto(PhotoSize telegramPhoto, BinaryContent persistentBinaryContent) {
        return AppPhoto.builder()
                .telegramField(telegramPhoto.getFileId())
                .binaryContent(persistentBinaryContent)
                .fileSize(telegramPhoto.getFileSize())
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
