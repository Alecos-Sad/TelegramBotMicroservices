package by.sadovnick.service.impl;

import by.sadovnick.dao.RowDataDao;
import by.sadovnick.entity.RowData;
import by.sadovnick.service.MainService;
import by.sadovnick.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @see MainService
 */
@Service
public class MainServiceImpl implements MainService {
    private final RowDataDao rowDataDao;
    private final ProducerService producerService;

    @Autowired
    public MainServiceImpl(RowDataDao rowDataDao, ProducerService producerService) {
        this.rowDataDao = rowDataDao;
        this.producerService = producerService;
    }

    /**
     * Отправка сообщения в чат.
     *
     * @param update - соощение.
     */
    @Override
    public void processTextMessage(Update update) {
        saveRowData(update);
        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from Node");
        producerService.produceAnswer(sendMessage);
    }

    /**
     * Сохранение в БД.
     *
     * @param update сообщение.
     */
    private void saveRowData(Update update) {
        RowData rowData = RowData.builder()
                .event(update)
                .build();
        rowDataDao.save(rowData);
    }
}
