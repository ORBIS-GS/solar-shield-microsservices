package com.solarshield.alert.consumer;

import com.solarshield.alert.model.AlertMessage;
import com.solarshield.alert.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer RabbitMQ: escuta solar.alerts.queue.
 * Delega ao AlertService a idempotência (RN3).
 */
@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);

    private final AlertService alertService;

    public AlertConsumer(AlertService alertService) {
        this.alertService = alertService;
    }

    @RabbitListener(queues = "${solar.rabbitmq.queue}")
    public void onMessage(AlertMessage message) {
        log.info("[CONSUMER] Mensagem recebida | eventId={} | severity={}",
                message.getEventId(), message.getSeverity());

        boolean saved = alertService.processMessage(message);

        if (saved) {
            log.info("[CONSUMER] Alerta persistido com sucesso | eventId={}", message.getEventId());
            if (message.isEmergencyNotification()) {
                log.warn("[EMERGÊNCIA] Tempestade severa detectada! | eventId={} | Kp={}",
                        message.getEventId(), message.getMaxKp());
            }
        } else {
            log.warn("[CONSUMER] Mensagem descartada (duplicata RN3) | eventId={}", message.getEventId());
        }
    }
}
