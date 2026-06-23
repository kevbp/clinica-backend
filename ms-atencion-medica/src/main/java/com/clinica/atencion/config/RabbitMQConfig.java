package com.clinica.atencion.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Debe coincidir con el exchange que consume ms-historias-clinicas
    public static final String EXCHANGE_ATENCION        = "atencion.events";
    public static final String ROUTING_KEY_EPISODIO     = "episodio.finalizado";

    @Bean
    public TopicExchange atencionExchange() {
        return new TopicExchange(EXCHANGE_ATENCION, true, false);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }
}
