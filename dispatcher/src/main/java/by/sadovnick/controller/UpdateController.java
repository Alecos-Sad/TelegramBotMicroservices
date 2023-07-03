package by.sadovnick.controller;

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

  public UpdateController(MessageUtils messageUtils) {
    this.messageUtils = messageUtils;
  }

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
      log.error("Received unsupported message type");
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

  private void setUnsupportedMessageTypeView(Update update) {
    SendMessage sendMessage = messageUtils.generateSendMessageWithText(update,
        "Неподдерживаемый тип сообщения!");
    setView(sendMessage);
  }

  /**
   * Передача сообщения. Почему нельзя вызвать на телеграмботе напрямую метод #sendAnswerMessage()? Потому, что будем передавать в
   * {@link UpdateController} также сообщения из сервисов, то из сервисов не сможем вызвать метод на телеграмботе. Этот метод будет пробрасывать ответ
   * дальше в телеграмбот.
   * Также можно централизованно добалять логику для всех вызовов.
   *
   * @param sendMessage - ответ бота.
   */
  private void setView(SendMessage sendMessage) {
    telegramBot.sendAnswerMessage(sendMessage);
  }

  private void processPhotoMessage(Update update) {
  }

  private void processDocMessage(Update update) {
  }

  private void processTextMessage(Update update) {
  }
}