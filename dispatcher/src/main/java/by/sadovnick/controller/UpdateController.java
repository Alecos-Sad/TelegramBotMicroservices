package by.sadovnick.controller;

import static by.sadovnick.RabbitQueue.DOC_MESSAGE_UPDATE;
import static by.sadovnick.RabbitQueue.PHOTO_MESSAGE_UPDATE;
import static by.sadovnick.RabbitQueue.TEXT_MESSAGE_UPDATE;

import by.sadovnick.service.UpdateProducer;
import by.sadovnick.utils.MessageUtils;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Класс который распределяет сообщения приходящие от бота
 */
@Controller
@Log4j
public class UpdateController {

  private TelegramBot telegramBot;
  private final MessageUtils messageUtils;
  private final UpdateProducer updateProducer;

  public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
    this.messageUtils = messageUtils;
    this.updateProducer = updateProducer;
  }

  /**
   * Метод для избежания циклической зависимости с {@link TelegramBot}
   */
  public void registerBot(TelegramBot telegramBot) {
    this.telegramBot = telegramBot;
  }

  /**
   * Проведение первичной валидации входящих данных. Могут быть разные типы входящих данных, такие как сообения из приватных чатов, отредактированные
   * сообщения из приватных чатов, сообщения из каналов, групп и т.д. Метод будет обрабатывать только неотредактированные сообщения из приватных
   * чатов.
   *
   * @param update - входящие данные
   */
  public void processUpdate(Update update) {
    if (update == null) {
      log.error("Received update is null");
    }
    if (update.getMessage() != null) {
      distributeMessagesByType(update);
    } else {
      log.error("Unsupported message type is received: " + update);
    }
  }

  /**
   * Проверка типа сообщения.
   *
   * @param update - входящие данные
   */
  private void distributeMessagesByType(Update update) {
    Message message = update.getMessage();
    if (message.getText() != null) {
      processTextMessage(update);
    } else if (message.getDocument() != null) {
      processDocMessage(update);
    } else if (message.getPhoto() != null) {
      processPhotoMessage(update);
    } else {
      setUnsupportedMessageTypeView(update);
    }
  }

  /**
   * Ответ при неподдерживаемом типе сообщения.
   *
   * @param update - сообщение
   */
  private void setUnsupportedMessageTypeView(Update update) {
    SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
        "Неподдерживаемый тип сообщения!");
    setView(sendMessage);
  }

  /**
   * Промежуточный ответ. Т.к требуется время на обработку файла.
   *
   * @param update - сообщение.
   */
  private void setFileIsReceivedView(Update update) {
    SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
        "Файл получен... Обрабатывается...");
    setView(sendMessage);
  }

  /**
   * Передача сообщения. Почему нельзя вызвать на телеграмботе напрямую метод #sendAnswerMessage()? Потому, что будем передавать в
   * {@link UpdateController} также сообщения из сервисов, то из сервисов не сможем вызвать метод на телеграмботе. Этот метод будет пробрасывать ответ
   * дальше в телеграмбот. Также можно централизованно добалять логику для всех вызовов.
   *
   * @param sendMessage - ответ бота.
   */
  private void setView(SendMessage sendMessage) {
    telegramBot.sendAnswerMessage(sendMessage);
  }

  /**
   * Передача фото сообщения в брокер.
   *
   * @param update - сообщение.
   */
  private void processPhotoMessage(Update update) {
    updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
    setFileIsReceivedView(update);
  }

  /**
   * Передача документа в брокер.
   *
   * @param update - сообщение.
   */
  private void processDocMessage(Update update) {
    updateProducer.produce(DOC_MESSAGE_UPDATE, update);
    setFileIsReceivedView(update);
  }

  /**
   * Передача текстового сообщения в брокер.
   *
   * @param update - сообщение.
   */
  private void processTextMessage(Update update) {
    updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
  }
}
