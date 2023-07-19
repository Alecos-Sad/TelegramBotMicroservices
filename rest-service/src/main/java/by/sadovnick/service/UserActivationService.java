package by.sadovnick.service;

/**
 * Обработка запроса на активацию пользователя
 */
public interface UserActivationService {
    boolean activation(String cryptoUserId);
}
