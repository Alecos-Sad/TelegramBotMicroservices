package by.sadovnick.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Бин для генерации ответа бота.
 */
@Component
public class MessageUtils {

  /**
   * Ответ бота.
   * @param update - входящие данные
   * @param text - посылаемый текст
   */
  public SendMessage generateSendMessageWithText(Update update, String text){
    Message message = update.getMessage();
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(message.getChatId());
    sendMessage.setText(text);
    return sendMessage;
  }
}
