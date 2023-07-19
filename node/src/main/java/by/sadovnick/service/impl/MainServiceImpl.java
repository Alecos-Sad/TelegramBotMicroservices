package by.sadovnick.service.impl;

import by.sadovnick.dao.AppUserDao;
import by.sadovnick.dao.RowDataDao;
import by.sadovnick.entity.AppDocument;
import by.sadovnick.entity.AppPhoto;
import by.sadovnick.entity.AppUser;
import by.sadovnick.entity.RowData;
import by.sadovnick.entity.enums.UserState;
import by.sadovnick.exceptions.UploadFileException;
import by.sadovnick.service.AppUserService;
import by.sadovnick.service.FileService;
import by.sadovnick.service.MainService;
import by.sadovnick.service.ProducerService;
import by.sadovnick.service.enums.LinkType;
import by.sadovnick.service.enums.ServiceCommand;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static by.sadovnick.entity.enums.UserState.BASIC_STATE;
import static by.sadovnick.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static by.sadovnick.service.enums.ServiceCommand.*;

/**
 * @see MainService
 */
@Service
@Log4j
public class MainServiceImpl implements MainService {
    private final RowDataDao rowDataDao;
    private final ProducerService producerService;
    private final AppUserDao appUserDao;
    private final FileService fileService;
    private final AppUserService appUserService;

    @Autowired
    public MainServiceImpl(RowDataDao rowDataDao, ProducerService producerService,
                           AppUserDao appUserDao, FileService fileService, AppUserService appUserService) {
        this.rowDataDao = rowDataDao;
        this.producerService = producerService;
        this.appUserDao = appUserDao;
        this.fileService = fileService;
        this.appUserService = appUserService;
    }

    /**
     * Основной метод по обработке текстовых сообщений.
     * Отправка сообщения в чат.
     */
    @Override
    public void processTextMessage(Update update) {
        saveRowData(update);
        AppUser appUser = findOrSaveAppUser(update);
        UserState userState = appUser.getUserState();
        String text = update.getMessage().getText();
        String output = "";
        ServiceCommand serviceCommand = fromValue(text);
        if (CANSEL.equals(serviceCommand)) {
            output = canselProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            output = appUserService.setEmail(appUser, text);
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите /cansel и попробуйте снова!";
        }
        //отправка ответа в чат пользователя
        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    /**
     * Основной метод по обработке документов сообщений.
     * Отправка сообщения в чат.
     */
    @Override
    public void processDocMessage(Update update) {
        saveRowData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            String link = fileService.generateLink(doc.getId(), LinkType.GET_DOC);
            String answer = "Документ успешно загружен! Ссылка для скачивания " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "К сожалению, загрузка файла не удалась. Повторите попытку позже!";
            sendAnswer(error, chatId);
        }
    }


    @Override
    public void processPhotoMessage(Update update) {
        saveRowData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        try {
            AppPhoto photo = fileService.processPhoto(update.getMessage());
            String link = fileService.generateLink(photo.getId(), LinkType.GET_DOC);
            String answer = "Фото успешно загружено! Ссылка для скачивания " + link;
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex) {
            log.error(ex);
            String error = "К сожалению, загрузка фото не удалась. Повторите попытку позже!";
            sendAnswer(error, chatId);
        }
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
        ServiceCommand serviceCommand = fromValue(cmd);
        if (REGISTRATION.equals(serviceCommand)) {
            return appUserService.registerUser(appUser);
        } else if (HELP.equals(serviceCommand)) {
            return help();
        } else if (START.equals(serviceCommand)) {
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
        Optional<AppUser> optional = appUserDao.findByTelegramUserId(telegramUser.getId());
        if (optional.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    .isActive(false)
                    .userState(BASIC_STATE)
                    .build();
            return appUserDao.save(transientAppUser);
        }
        return optional.get();
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
