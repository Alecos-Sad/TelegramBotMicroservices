package by.sadovnick.controller;

import by.sadovnick.entity.AppDocument;
import by.sadovnick.entity.AppPhoto;
import by.sadovnick.entity.BinaryContent;
import by.sadovnick.service.FileService;
import lombok.extern.log4j.Log4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Для обработки Http запроса со стороны пользователя.
 */
@Log4j
@RequestMapping("/file")
@RestController
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Скачивание файла из бд.
     * ResponseEntity - позволяет удобно собрать Http ответ
     *
     * @RequestParam("id")- id = 1 при localhost:8086/file/get-doc?id=1
     * в contentType передаем хедер, в котором будет формат контента, чтобы браузер мог из потока
     * байт создать файл с нужным расширением.
     * header "Content-disposition" - показывает браузеру как именно воспринимать полученную информацию.
     * attachment - чтобы браузер скачал полученный файл. Иначе просто откроет его в окне.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/get-doc")
    public ResponseEntity<?> getDoc(@RequestParam("id") String id) {
        //todo для формирования badRequest добавить ControllerAdvise
        AppDocument doc = fileService.getDocument(id);
        if (doc == null) {
            return ResponseEntity.badRequest().build();
        }
        BinaryContent binaryContent = doc.getBinaryContent();
        FileSystemResource fileSystemResource = fileService.getFileSystemResource(binaryContent);
        if (fileSystemResource == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getMimeType()))
                .header("Content-disposition", "attachment; filename" + doc.getDocName())
                .body(fileSystemResource);
    }

    /**
     * @see FileController#getDoc(String)
     */
    @RequestMapping(method = RequestMethod.GET, value = "/get-photo")
    public ResponseEntity<?> getPhoto(@RequestParam("id") String id) {
        //todo для формирования badRequest добавить ControllerAdvise
        AppPhoto photo = fileService.getPhoto(id);
        if (photo == null) {
            return ResponseEntity.badRequest().build();
        }
        BinaryContent binaryContent = photo.getBinaryContent();
        FileSystemResource fileSystemResource = fileService.getFileSystemResource(binaryContent);
        if (fileSystemResource == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header("Content-disposition", "attachment; filename")
                .body(fileSystemResource);
    }
}
