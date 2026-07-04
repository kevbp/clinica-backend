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

    // Exchanges declarados por los emisores (ms-citas, ms-caja y ms-atencion-medica) — este servicio solo los referencia
    public static final String EXCHANGE_CITAS    = "citas.events";
    public static final String EXCHANGE_CAJA     = "caja.events";
    public static final String EXCHANGE_ATENCION = "atencion.events";

    // Colas propias de este servicio
    public static final String QUEUE_CITA_CREADA        = "notificaciones.cita.creada";
    public static final String QUEUE_CITA_CANCELADA     = "notificaciones.cita.cancelada";
    public static final String QUEUE_CITA_REAGENDADA    = "notificaciones.cita.reagendada";
    public static final String QUEUE_PAGO_CONFIRMADO    = "notificaciones.pago.confirmado";
    public static final String QUEUE_EPISODIO_ATENDIDO       = "notificaciones.episodio.atendido";
    public static final String QUEUE_COMPROBANTE_REENVIAR    = "notificaciones.comprobante.reenviar";
    public static final String QUEUE_NC_REENVIAR             = "notificaciones.nc.reenviar";
    public static final String QUEUE_RETIRO_SOLICITADO       = "notificaciones.retiro.solicitado";

    // Routing keys (deben coincidir con las que usan los emisores)
    private static final String RK_CITA_CREADA              = "cita.creada";
    private static final String RK_CITA_CANCELADA           = "cita.cancelada";
    private static final String RK_CITA_REAGENDADA          = "cita.reagendada";
    private static final String RK_PAGO_CONFIRMADO          = "pago-consulta.confirmado";
    private static final String RK_EPISODIO_ATENDIDO        = "episodio.atendido.notificacion";
    private static final String RK_COMPROBANTE_REENVIAR     = "comprobante.reenviar";
    private static final String RK_NC_REENVIAR              = "nota-credito.reenviar";
    private static final String RK_RETIRO_SOLICITADO        = "retiro.solicitado";

    @Bean
    public TopicExchange citasExchange() {
        return new TopicExchange(EXCHANGE_CITAS, true, false);
    }

    @Bean
    public TopicExchange cajaExchange() {
        return new TopicExchange(EXCHANGE_CAJA, true, false);
    }

    @Bean
    public TopicExchange atencionExchange() {
        return new TopicExchange(EXCHANGE_ATENCION, true, false);
    }

    @Bean
    public Queue citaCreadaQueue() {
        return new Queue(QUEUE_CITA_CREADA, true);
    }

    @Bean
    public Queue citaCanceladaQueue() {
        return new Queue(QUEUE_CITA_CANCELADA, true);
    }

    @Bean
    public Queue citaReagendadaQueue() {
        return new Queue(QUEUE_CITA_REAGENDADA, true);
    }

    @Bean
    public Queue pagoConfirmadoQueue() {
        return new Queue(QUEUE_PAGO_CONFIRMADO, true);
    }

    @Bean
    public Queue episodioAtendidoQueue() {
        return new Queue(QUEUE_EPISODIO_ATENDIDO, true);
    }

    @Bean
    public Queue comprobanteReenviarQueue() {
        return new Queue(QUEUE_COMPROBANTE_REENVIAR, true);
    }

    @Bean
    public Binding citaCreadaBinding(Queue citaCreadaQueue, TopicExchange citasExchange) {
        return BindingBuilder.bind(citaCreadaQueue).to(citasExchange).with(RK_CITA_CREADA);
    }

    @Bean
    public Binding citaCanceladaBinding(Queue citaCanceladaQueue, TopicExchange citasExchange) {
        return BindingBuilder.bind(citaCanceladaQueue).to(citasExchange).with(RK_CITA_CANCELADA);
    }

    @Bean
    public Binding citaReagendadaBinding(Queue citaReagendadaQueue, TopicExchange citasExchange) {
        return BindingBuilder.bind(citaReagendadaQueue).to(citasExchange).with(RK_CITA_REAGENDADA);
    }

    @Bean
    public Binding pagoConfirmadoBinding(Queue pagoConfirmadoQueue, TopicExchange cajaExchange) {
        return BindingBuilder.bind(pagoConfirmadoQueue).to(cajaExchange).with(RK_PAGO_CONFIRMADO);
    }

    @Bean
    public Binding episodioAtendidoBinding(Queue episodioAtendidoQueue, TopicExchange atencionExchange) {
        return BindingBuilder.bind(episodioAtendidoQueue).to(atencionExchange).with(RK_EPISODIO_ATENDIDO);
    }

    @Bean
    public Binding comprobanteReenviarBinding(Queue comprobanteReenviarQueue, TopicExchange cajaExchange) {
        return BindingBuilder.bind(comprobanteReenviarQueue).to(cajaExchange).with(RK_COMPROBANTE_REENVIAR);
    }

    @Bean
    public Queue ncReenviarQueue() {
        return new Queue(QUEUE_NC_REENVIAR, true);
    }

    @Bean
    public Queue retiroSolicitadoQueue() {
        return new Queue(QUEUE_RETIRO_SOLICITADO, true);
    }

    @Bean
    public Binding ncReenviarBinding(Queue ncReenviarQueue, TopicExchange cajaExchange) {
        return BindingBuilder.bind(ncReenviarQueue).to(cajaExchange).with(RK_NC_REENVIAR);
    }

    @Bean
    public Binding retiroSolicitadoBinding(Queue retiroSolicitadoQueue, TopicExchange cajaExchange) {
        return BindingBuilder.bind(retiroSolicitadoQueue).to(cajaExchange).with(RK_RETIRO_SOLICITADO);
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
