package by.sadovnick.service.impl;

import by.sadovnick.service.ConsumerService;
import by.sadovnick.service.MainService;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import static by.sadovnick.RabbitQueue.*;

/**
 * @see ConsumerService
 */
@Service
@Log4j
public class ConsumerServiceImpl implements ConsumerService {
    private final MainService mainService;

    @Autowired
    public ConsumerServiceImpl(MainService mainService) {
        this.mainService = mainService;
    }

    /**
     * Метод слушает очередь TEXT
     */
    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessageUpdates(Update update) {
        log.debug("NODE: Text message is received");
        mainService.processTextMessage(update);
    }

    /**
     * Метод слушает очередь DOC
     */
    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocumentMessageUpdates(Update update) {
        log.debug("NODE: Document message is received");
        mainService.processDocMessage(update);
    }

    /**
     * Метод слушает очередь PHOTO
     */
    @Override
    @RabbitListener(queues = PHOTO_MESSAGE_UPDATE)
    public void consumePhotoMessageUpdates(Update update) {
        log.debug("NODE: Photo message is received");
        mainService.processPhotoMessage(update);
    }
}
