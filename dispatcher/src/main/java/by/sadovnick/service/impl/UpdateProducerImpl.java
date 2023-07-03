package by.sadovnick.service.impl;

import by.sadovnick.service.UpdateProducer;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @see UpdateProducer
 */
@Service
@Log4j
public class UpdateProducerImpl implements UpdateProducer {

  /**
   * Метод принимает сообщения из приложения и отправляет сообщения в брокер.
   * @param rabbitQueue - имя очереди в брокере.
   * @param update - сообщение.
   */
  @Override
  public void produce(String rabbitQueue, Update update) {
    log.debug(update.getMessage().getText());
  }
}
