package by.sadovnick.service;

import by.sadovnick.controller.UpdateProcessor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Принимает ответы из RabbitMQ и передача в {@link UpdateProcessor}
 */
public interface AnswerConsumer {

  void consume(SendMessage sendMessage);
}
