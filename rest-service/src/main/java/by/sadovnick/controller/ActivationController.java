package by.sadovnick.controller;

import by.sadovnick.service.UserActivationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Обработка запросов по ссылке с письма
 */
@RestController
@RequestMapping("/user")
public class ActivationController {
    private final UserActivationService userActivationService;

    public ActivationController(UserActivationService userActivationService) {
        this.userActivationService = userActivationService;
    }

    /**
     * Активация пользователя.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/activation")
    public ResponseEntity<?> activation(@RequestParam("id") String id) {
        boolean resultActivation = userActivationService.activation(id);
        if (resultActivation) {
            return ResponseEntity.ok().body("Регистрация завершена успешно");
        }
        return ResponseEntity.internalServerError().build();
    }
}
