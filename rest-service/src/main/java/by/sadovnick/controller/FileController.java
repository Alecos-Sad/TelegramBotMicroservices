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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public void getDoc(@RequestParam("id") String id, HttpServletResponse response) {
        //todo для формирования badRequest добавить ControllerAdvise
        AppDocument doc = fileService.getDocument(id);
        if (doc == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setContentType(MediaType.parseMediaType(doc.getMimeType()).toString());
        response.setHeader("Content-disposition", "attachment; filename=" + doc.getDocName());
        response.setStatus(HttpServletResponse.SC_OK);
        BinaryContent binaryContent = doc.getBinaryContent();
        try {
            ServletOutputStream out = response.getOutputStream();
            out.write(binaryContent.getFileArrayOfByte());
            out.close();
        } catch (IOException e) {
            log.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get-photo")
    public void getPhoto(@RequestParam("id") String id, HttpServletResponse response) {
        //todo для формирования badRequest добавить ControllerAdvise
        AppPhoto photo = fileService.getPhoto(id);
        if (photo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setContentType(MediaType.IMAGE_JPEG.toString());
        response.setHeader("Content-disposition", "attachment");
        response.setStatus(HttpServletResponse.SC_OK);
        BinaryContent binaryContent = photo.getBinaryContent();
        try {
            ServletOutputStream out = response.getOutputStream();
            out.write(binaryContent.getFileArrayOfByte());
            out.close();
        } catch (IOException e) {
            log.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
