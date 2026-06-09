package com.solarshield.ingest.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${solar.rabbitmq.exchange}")
    private String exchange;

    @Value("${solar.rabbitmq.queue}")
    private String queue;

    @Value("${solar.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public TopicExchange solarExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue solarAlertsQueue() {
        return QueueBuilder.durable(queue).build();
    }

    @Bean
    public Binding binding(Queue solarAlertsQueue, TopicExchange solarExchange) {
        return BindingBuilder.bind(solarAlertsQueue).to(solarExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
