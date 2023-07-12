package by.sadovnick.service;

import by.sadovnick.CryptoTool;
import by.sadovnick.dao.AppDocumentDao;
import by.sadovnick.dao.AppPhotoDao;
import by.sadovnick.entity.AppDocument;
import by.sadovnick.entity.AppPhoto;
import by.sadovnick.entity.BinaryContent;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * @see FileService
 */
@Log4j
@Service
public class FileServiceImpl implements FileService {
    private final AppDocumentDao appDocumentDao;
    private final AppPhotoDao appPhotoDao;
    private final CryptoTool cryptoTool;

    public FileServiceImpl(AppDocumentDao appDocumentDao, AppPhotoDao appPhotoDao, CryptoTool cryptoTool) {
        this.appDocumentDao = appDocumentDao;
        this.appPhotoDao = appPhotoDao;
        this.cryptoTool = cryptoTool;
    }

    /**
     * Получение файла из бд
     */
    @Override
    public AppDocument getDocument(String hash) {
        Long id = cryptoTool.idOf(hash);
        if (id == null){
            return null;
        }
        return appDocumentDao.findById(id).orElse(null);
    }

    /**
     * Получение фото из бд
     */
    @Override
    public AppPhoto getPhoto(String hash) {
        Long id = cryptoTool.idOf(hash);
        if (id == null){
            return null;
        }
        return appPhotoDao.findById(id).orElse(null);
    }

    /**
     * Получение и преобразование файла из бд в виде потока байт.
     */
    @Override
    public FileSystemResource getFileSystemResource(BinaryContent binaryContent) {
        try {
            //todo добавить генерацию имени временного файла. Т.к если будет создаваться 2 то будет конфликт
            //создаем временный файл
            File temp = File.createTempFile("tempFile", "bin");
            //помещаем файл в очередь на удаление при завершении приложения.
            temp.deleteOnExit();
            //запись во временный файл бинарные данные из бд
            FileUtils.writeByteArrayToFile(temp, binaryContent.getFileArrayOfByte());
            //FileSystemResource является классом из Spring Framework, предназначенным для
            //представления ресурса файловой системы. Он обеспечивает доступ к файлам в файловой
            //системе, позволяя выполнять операции чтения и записи.
            return new FileSystemResource(temp);
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }
}
