package by.sadovnick.service.impl;

import by.sadovnick.dao.AppUserDao;
import by.sadovnick.dao.RowDataDao;
import by.sadovnick.entity.AppUser;
import by.sadovnick.entity.RowData;
import by.sadovnick.service.MainService;
import by.sadovnick.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static by.sadovnick.entity.UserState.BASIC_STATE;

/**
 * @see MainService
 */
@Service
public class MainServiceImpl implements MainService {
    private final RowDataDao rowDataDao;
    private final ProducerService producerService;
    private final AppUserDao appUserDao;

    @Autowired
    public MainServiceImpl(RowDataDao rowDataDao, ProducerService producerService, AppUserDao appUserDao) {
        this.rowDataDao = rowDataDao;
        this.producerService = producerService;
        this.appUserDao = appUserDao;
    }

    /**
     * Отправка сообщения в чат.
     *
     * @param update - соощение.
     */
    @Override
    public void processTextMessage(Update update) {
        saveRowData(update);
        Message textMessage = update.getMessage();
        User telegramUser = textMessage.getFrom();
        AppUser appUser = findOrSaveAppUser(telegramUser);

        Message message = update.getMessage();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText("Hello from Node");
        producerService.produceAnswer(sendMessage);
    }

    /**
     * Достает пользователя из телеграм user или создает и помещает в бд нового.
     * @param telegramUser- телеграм user
     * @return - entity user
     */
    private AppUser findOrSaveAppUser(User telegramUser) {
        AppUser persistentAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //todo изменить значение по умолчанию после добавления реистрации
                    .isActive(true)
                    .userState(BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser;
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
