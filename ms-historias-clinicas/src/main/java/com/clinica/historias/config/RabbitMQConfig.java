package com.clinica.historias.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_ATENCION    = "atencion.events";
    public static final String QUEUE_EPISODIO       = "historias.episodio.finalizado";
    public static final String ROUTING_KEY_EPISODIO = "episodio.finalizado";

    // Dead-Letter
    public static final String DLX_HISTORIAS = "historias.dlx";
    public static final String DLQ_HISTORIAS = "historias.dlq";

    // ── Dead-Letter Infrastructure ─────────────────────────────────────────

    @Bean
    public DirectExchange dlxHistorias() {
        return new DirectExchange(DLX_HISTORIAS, true, false);
    }

    @Bean
    public Queue dlqHistorias() {
        return QueueBuilder.durable(DLQ_HISTORIAS).build();
    }

    @Bean
    public Binding dlqHistoriasBinding(Queue dlqHistorias, DirectExchange dlxHistorias) {
        return BindingBuilder.bind(dlqHistorias).to(dlxHistorias).with(DLQ_HISTORIAS);
    }

    // ── Cola principal con DLX ─────────────────────────────────────────────

    @Bean
    public TopicExchange atencionExchange() {
        return new TopicExchange(EXCHANGE_ATENCION, true, false);
    }

    @Bean
    public Queue episodioFinalizadoQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_HISTORIAS);
        args.put("x-dead-letter-routing-key", DLQ_HISTORIAS);
        return QueueBuilder.durable(QUEUE_EPISODIO).withArguments(args).build();
    }

    @Bean
    public Binding episodioBinding(Queue episodioFinalizadoQueue, TopicExchange atencionExchange) {
        return BindingBuilder.bind(episodioFinalizadoQueue)
                .to(atencionExchange)
                .with(ROUTING_KEY_EPISODIO);
    }

    // ── Container factory: fallo → NACK automático → DLQ ──────────────────

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);
        // Excepción no capturada → NACK sin requeue → mensaje va al DLX configurado en la cola
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    // ── Conversor y template ───────────────────────────────────────────────

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
