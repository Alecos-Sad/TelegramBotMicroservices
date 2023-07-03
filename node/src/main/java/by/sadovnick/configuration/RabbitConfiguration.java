package by.sadovnick.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static by.sadovnick.RabbitQueue.*;

/**
 * Конфигурация RabbitMQ
 */
@Configuration
public class RabbitConfiguration {
    /**
     * Бин Преобразует updates в json и передает их в брокер.
     *
     * @return - перобразованный update в json.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Бин возвращает очередь с тексовыми сообщениями.
     */
    @Bean
    public Queue textMessageQueue() {
        return new Queue(TEXT_MESSAGE_UPDATE);
    }

    /**
     * Бин возвращает очередь с документами.
     */
    @Bean
    public Queue docMessageQueue() {
        return new Queue(DOC_MESSAGE_UPDATE);
    }

    /**
     * Бин возвращает очередь с фотографиями
     */
    @Bean
    public Queue photoMessageQueue() {
        return new Queue(PHOTO_MESSAGE_UPDATE);
    }

    /**
     * Бин возвращает очередь с другим ответом
     */
    @Bean
    public Queue answerMessageQueue() {
        return new Queue(ANSWER_MESSAGE);
    }
}
