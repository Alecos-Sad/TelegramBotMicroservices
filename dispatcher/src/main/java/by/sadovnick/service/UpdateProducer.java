package by.sadovnick.service;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Сервис для приема сообщений из приложения и передачи обновлений в RabbitMQ
 */
public interface UpdateProducer {

  void produce(String rabbitQueue, Update update);
}
