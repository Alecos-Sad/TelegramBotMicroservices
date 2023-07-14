package by.sadovnick.service;

import by.sadovnick.dto.MailParams;

/**
 * Отправка на почту пользователю
 */
public interface MailSenderService {
    void send(MailParams mailParams);
}


