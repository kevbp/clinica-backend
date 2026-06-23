package com.clinica.notificaciones.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges declarados por los emisores (ms-citas y ms-caja) — este servicio solo los referencia
    public static final String EXCHANGE_CITAS = "citas.events";
    public static final String EXCHANGE_CAJA  = "caja.events";

    // Colas propias de este servicio
    public static final String QUEUE_CITA_CREADA       = "notificaciones.cita.creada";
    public static final String QUEUE_PAGO_CONFIRMADO   = "notificaciones.pago.confirmado";

    // Routing keys (deben coincidir con las que usan los emisores)
    private static final String RK_CITA_CREADA       = "cita.creada";
    private static final String RK_PAGO_CONFIRMADO   = "pago-consulta.confirmado";

    @Bean
    public TopicExchange citasExchange() {
        return new TopicExchange(EXCHANGE_CITAS, true, false);
    }

    @Bean
    public TopicExchange cajaExchange() {
        return new TopicExchange(EXCHANGE_CAJA, true, false);
    }

    @Bean
    public Queue citaCreadaQueue() {
        return new Queue(QUEUE_CITA_CREADA, true);
    }

    @Bean
    public Queue pagoConfirmadoQueue() {
        return new Queue(QUEUE_PAGO_CONFIRMADO, true);
    }

    @Bean
    public Binding citaCreadaBinding(Queue citaCreadaQueue, TopicExchange citasExchange) {
        return BindingBuilder.bind(citaCreadaQueue).to(citasExchange).with(RK_CITA_CREADA);
    }

    @Bean
    public Binding pagoConfirmadoBinding(Queue pagoConfirmadoQueue, TopicExchange cajaExchange) {
        return BindingBuilder.bind(pagoConfirmadoQueue).to(cajaExchange).with(RK_PAGO_CONFIRMADO);
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
