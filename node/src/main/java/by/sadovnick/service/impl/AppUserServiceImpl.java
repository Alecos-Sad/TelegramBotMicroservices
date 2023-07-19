package by.sadovnick.service.impl;

import by.sadovnick.CryptoTool;
import by.sadovnick.dao.AppUserDao;
import by.sadovnick.dto.MailParams;
import by.sadovnick.entity.AppUser;
import by.sadovnick.service.AppUserService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.InternetAddress;
import java.util.Optional;

import static by.sadovnick.entity.enums.UserState.BASIC_STATE;
import static by.sadovnick.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;

@Service
@Log4j
public class AppUserServiceImpl implements AppUserService {
    private final AppUserDao appUserDao;
    private final CryptoTool cryptoTool;
    @Value("${service.mail.url}")
    private String mailServiceUri;

    public AppUserServiceImpl(AppUserDao appUserDao, CryptoTool cryptoTool) {
        this.appUserDao = appUserDao;
        this.cryptoTool = cryptoTool;
    }

    @Override
    public String registerUser(AppUser appUser) {
        if (appUser.getIsActive()) {
            return "Вы уже зарегистрированы";
        } else if (appUser.getEmail() != null) {
            return "Вам на почту уже было отправлено письмо. " +
                    "Перейдите по ссылке в письме для подтверждения регистрации!";
        }
        appUser.setUserState(WAIT_FOR_EMAIL_STATE);
        appUserDao.save(appUser);
        return "Введите, пожалуйста, свой email: ";
    }

    /**
     * Валидация и отправка email.
     */
    @Override
    public String setEmail(AppUser appUser, String email) {
        try {
            InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        } catch (Exception e) {
            return "Введите, пожалуйста, корректный email! Для отмены команды введите /cansel";
        }
        Optional<AppUser> optional = appUserDao.findByEmail(email);
        if (optional.isEmpty()) {
            appUser.setEmail(email);
            appUser.setUserState(BASIC_STATE);
            appUser = appUserDao.save(appUser);
            String cryptoUserId = cryptoTool.hashOf(appUser.getId());
            ResponseEntity<String> response = sendRequestToMailService(cryptoUserId, email);
            if (response.getStatusCode() != HttpStatus.OK) {
                String msg = String.format("Отправка эл.письма на почту %s не удалась.", email);
                log.error(msg);
                appUser.setEmail(null);
                appUserDao.save(appUser);
                return msg;
            }
            return "Вам на почту было отправлено письмо. Перейдите по ссылке в письме для регистрации.";
        } else {
            return "Этот  email уже используется. Введите корректный email. " +
                    "Для отмены команды введите /cansel";
        }
    }

    private ResponseEntity<String> sendRequestToMailService(String cryptoUserId, String email) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        MailParams mailParams = MailParams.builder()
                .id(cryptoUserId)
                .emailTo(email)
                .build();
        HttpEntity<MailParams> request = new HttpEntity<>(mailParams, httpHeaders);
        return restTemplate.exchange(mailServiceUri, HttpMethod.POST, request, String.class);
    }
}
