package by.sadovnick.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Принимает ответы из RabbitMQ и передача в {@link by.sadovnick.controller.UpdateController}
 */
public interface AnswerConsumer {

  void consume(SendMessage sendMessage);
}
