package com.clinica.historias.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_ATENCION   = "atencion.events";
    public static final String QUEUE_EPISODIO      = "historias.episodio.finalizado";
    public static final String ROUTING_KEY_EPISODIO = "episodio.finalizado";

    @Bean
    public TopicExchange atencionExchange() {
        return new TopicExchange(EXCHANGE_ATENCION, true, false);
    }

    @Bean
    public Queue episodioFinalizadoQueue() {
        return new Queue(QUEUE_EPISODIO, true);
    }

    @Bean
    public Binding episodioBinding(Queue episodioFinalizadoQueue, TopicExchange atencionExchange) {
        return BindingBuilder.bind(episodioFinalizadoQueue)
                .to(atencionExchange)
                .with(ROUTING_KEY_EPISODIO);
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
