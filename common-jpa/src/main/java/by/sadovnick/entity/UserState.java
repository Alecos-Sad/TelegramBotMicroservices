package by.sadovnick.entity;

/**
 * Состояние пользователя. В зависимости от состояния ожидаем определенных действий.
 * WAIT_FOR_EMAIL_STATE - приложение считает валидным только ввод email или
 * команды отмены процесса регистрации.
 *
 */
public enum UserState {
    BASIC_STATE,
    WAIT_FOR_EMAIL_STATE
}
