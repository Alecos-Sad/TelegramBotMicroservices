package by.sadovnick.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Dto для того чтобы Spring мог на лету смапить входящий json от почты и отдать
 * из контроллера в сервис для дальнейшей обработки запроса.
 */
@Getter
@Setter
@RequiredArgsConstructor
public class MailParams {
    private String id;
    //адрес на который следует отправить письмо
    private String emailTo;
}
