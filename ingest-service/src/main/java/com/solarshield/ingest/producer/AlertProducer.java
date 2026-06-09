package com.solarshield.ingest.producer;

import com.solarshield.ingest.model.AlertMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Producer RabbitMQ: publica AlertMessage na exchange configurada.
 */
@Component
public class AlertProducer {

    private static final Logger log = LoggerFactory.getLogger(AlertProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${solar.rabbitmq.exchange}")
    private String exchange;

    @Value("${solar.rabbitmq.routing-key}")
    private String routingKey;

    public AlertProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(AlertMessage message) {
        log.info("Publicando evento no RabbitMQ | eventId={} | severity={}",
                message.getEventId(), message.getSeverity());
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
