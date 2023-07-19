package by.sadovnick.service.impl;

import by.sadovnick.CryptoTool;
import by.sadovnick.dao.AppDocumentDao;
import by.sadovnick.dao.AppPhotoDao;
import by.sadovnick.entity.AppDocument;
import by.sadovnick.entity.AppPhoto;
import by.sadovnick.service.FileService;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

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
        if (id == null) {
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
        if (id == null) {
            return null;
        }
        return appPhotoDao.findById(id).orElse(null);
    }
}
