package by.sadovnick.dto;

import lombok.*;

/**
 * Dto для того чтобы Spring мог на лету смапить входящий json от почты и отдать
 * из контроллера в сервис для дальнейшей обработки запроса.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailParams {
    private String id;
    //адрес на который следует отправить письмо
    private String emailTo;
}
