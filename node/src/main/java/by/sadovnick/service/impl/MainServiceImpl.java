package by.sadovnick.service.impl;

import by.sadovnick.dao.AppUserDao;
import by.sadovnick.dao.RowDataDao;
import by.sadovnick.entity.AppUser;
import by.sadovnick.entity.RowData;
import by.sadovnick.entity.UserState;
import by.sadovnick.service.MainService;
import by.sadovnick.service.ProducerService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static by.sadovnick.entity.UserState.BASIC_STATE;
import static by.sadovnick.entity.UserState.WAIT_FOR_EMAIL_STATE;
import static by.sadovnick.service.ServiceCommands.*;

/**
 * @see MainService
 */
@Service
@Log4j
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
     * Основной метод по обработке текстовых сообщений.
     * Отправка сообщения в чат.
     *
     * @param update - соощение.
     */
    @Override
    public void processTextMessage(Update update) {
        saveRowData(update);
        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getUserState();
        String text = update.getMessage().getText();
        String output = "";

        if (CANSEL.equals(text)) {
            output = canselProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //todo добавить обработку email
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cansel и попробуйте снова!";
        }
        //отправка ответа в чат пользователя
        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRowData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        //todo добавить сохранение документа.
        String answer = "Документ успешно загружен! Ссылка для скачивания http://www.google.com/doc";
        sendAnswer(answer, chatId);
    }


    @Override
    public void processPhotoMessage(Update update) {
        saveRowData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        //todo добавить сохранение фото.
        String answer = "Фото успешно загружено! Ссылка для скачивания http://www.google.com/photo";
        sendAnswer(answer, chatId);
    }

    /**
     * Описание случаев, когда загрузка контента запрещена.
     */
    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getUserState();
        if (!appUser.getIsActive()) {
            String error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            String error = "Отмените текущую команду с помощью /cansel для отправки файлов";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    /**
     * Отправка ответа в чат пользователя.
     *
     * @param output - ответ.
     * @param chatId - id чата.
     */
    private void sendAnswer(String output, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.produceAnswer(sendMessage);
    }

    /**
     * Обработка команд введенных пользователем.
     */
    private String processServiceCommand(AppUser appUser, String cmd) {
        if (REGISTRATION.equals(cmd)) {
            //todo добавить регистрацию.
            return "Временно недоступна";
        } else if (HELP.equals(cmd)) {
            return help();
        } else if (START.equals(cmd)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите /help";
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команд:\n"
                + "/cansel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя";
    }

    /**
     * Сохраняет пользователю базовое состояние и сохраняет обновленные данные в бд.
     */
    private String canselProcess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Команда отменена!";
    }

    /**
     * Достает пользователя из телеграм user или создает и помещает в бд нового.
     *
     * @return - entity user
     */
    private AppUser findOrSaveAppUser(Update update) {
        User telegramUser = update.getMessage().getFrom();
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
