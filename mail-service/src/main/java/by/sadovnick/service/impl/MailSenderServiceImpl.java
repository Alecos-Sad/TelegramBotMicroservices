package by.sadovnick.service.impl;

import by.sadovnick.dto.MailParams;
import by.sadovnick.service.MailSenderService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @see MailSenderService
 */
@Service
public class MailSenderServiceImpl implements MailSenderService {
    private final JavaMailSender javaMailSender;
    //откуда идет рассылка
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.url}")
    private String activationServiceUri;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Формирование и отправка почты
     * @param mailParams
     */
    @Override
    public void send(MailParams mailParams) {
        //тема письма
        String subject = "Активация учетной записи";
        //тема письма
        @NonNull
        String messageBody = getActivationMailBody(mailParams.getId());
        String emailTo = mailParams.getEmailTo();

        //Спринговый класс для формирования почтового сообщения.
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(messageBody);

        javaMailSender.send(mailMessage);
    }

    private String getActivationMailBody(String id) {
        String msg = String.format("Для завершения регистрации пройдите по ссылке:\n%s", activationServiceUri);
        return msg.replace("{id}", id);
    }
}
