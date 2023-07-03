package by.sadovnick;

/**
 * Класс описаний для RabbitMq.
 * "Зашить" названия очередей брокера в виде констант в коде - это не best-practice.
 * На реальных проектах так лучше не делать.
 */
public class RabbitQueue {
    public static final String DOC_MESSAGE_UPDATE = "doc_message_update";
    public static final String PHOTO_MESSAGE_UPDATE = "photo_message_update";
    public static final String TEXT_MESSAGE_UPDATE = "text_message_update";
    public static final String ANSWER_MESSAGE = "answer_message";
}
