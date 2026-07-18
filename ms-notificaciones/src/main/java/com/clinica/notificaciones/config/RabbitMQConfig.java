package com.clinica.notificaciones.config;

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

    // Exchanges declarados por los emisores
    public static final String EXCHANGE_CITAS    = "citas.events";
    public static final String EXCHANGE_CAJA     = "caja.events";
    public static final String EXCHANGE_ATENCION = "atencion.events";

    // Dead-Letter Exchange y cola para notificaciones fallidas
    public static final String DLX_NOTIFICACIONES = "notificaciones.dlx";
    public static final String DLQ_NOTIFICACIONES  = "notificaciones.dlq";

    // Colas principales
    public static final String QUEUE_CITA_CREADA          = "notificaciones.cita.creada";
    public static final String QUEUE_CITA_CANCELADA       = "notificaciones.cita.cancelada";
    public static final String QUEUE_CITA_REAGENDADA      = "notificaciones.cita.reagendada";
    public static final String QUEUE_PAGO_CONFIRMADO      = "notificaciones.pago.confirmado";
    public static final String QUEUE_EPISODIO_ATENDIDO    = "notificaciones.episodio.atendido";
    public static final String QUEUE_COMPROBANTE_REENVIAR = "notificaciones.comprobante.reenviar";
    public static final String QUEUE_NC_REENVIAR          = "notificaciones.nc.reenviar";
    public static final String QUEUE_RETIRO_SOLICITADO    = "notificaciones.retiro.solicitado";

    // Routing keys
    private static final String RK_CITA_CREADA          = "cita.creada";
    private static final String RK_CITA_CANCELADA       = "cita.cancelada";
    private static final String RK_CITA_REAGENDADA      = "cita.reagendada";
    private static final String RK_PAGO_CONFIRMADO      = "pago-consulta.confirmado";
    private static final String RK_EPISODIO_ATENDIDO    = "episodio.atendido.notificacion";
    private static final String RK_COMPROBANTE_REENVIAR = "comprobante.reenviar";
    private static final String RK_NC_REENVIAR          = "nota-credito.reenviar";
    private static final String RK_RETIRO_SOLICITADO    = "retiro.solicitado";

    // ── Dead-Letter Infrastructure ─────────────────────────────────────────

    @Bean
    public DirectExchange dlxNotificaciones() {
        return new DirectExchange(DLX_NOTIFICACIONES, true, false);
    }

    @Bean
    public Queue dlqNotificaciones() {
        return QueueBuilder.durable(DLQ_NOTIFICACIONES).build();
    }

    @Bean
    public Binding dlqBinding(Queue dlqNotificaciones, DirectExchange dlxNotificaciones) {
        return BindingBuilder.bind(dlqNotificaciones).to(dlxNotificaciones).with(DLQ_NOTIFICACIONES);
    }

    private Map<String, Object> dlqArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_NOTIFICACIONES);
        args.put("x-dead-letter-routing-key", DLQ_NOTIFICACIONES);
        return args;
    }

    // ── Exchanges de origen ────────────────────────────────────────────────

    @Bean public TopicExchange citasExchange()    { return new TopicExchange(EXCHANGE_CITAS,    true, false); }
    @Bean public TopicExchange cajaExchange()     { return new TopicExchange(EXCHANGE_CAJA,     true, false); }
    @Bean public TopicExchange atencionExchange() { return new TopicExchange(EXCHANGE_ATENCION, true, false); }

    // ── Colas con DLX configurado ──────────────────────────────────────────

    @Bean public Queue citaCreadaQueue()          { return QueueBuilder.durable(QUEUE_CITA_CREADA).withArguments(dlqArgs()).build(); }
    @Bean public Queue citaCanceladaQueue()       { return QueueBuilder.durable(QUEUE_CITA_CANCELADA).withArguments(dlqArgs()).build(); }
    @Bean public Queue citaReagendadaQueue()      { return QueueBuilder.durable(QUEUE_CITA_REAGENDADA).withArguments(dlqArgs()).build(); }
    @Bean public Queue pagoConfirmadoQueue()      { return QueueBuilder.durable(QUEUE_PAGO_CONFIRMADO).withArguments(dlqArgs()).build(); }
    @Bean public Queue episodioAtendidoQueue()    { return QueueBuilder.durable(QUEUE_EPISODIO_ATENDIDO).withArguments(dlqArgs()).build(); }
    @Bean public Queue comprobanteReenviarQueue() { return QueueBuilder.durable(QUEUE_COMPROBANTE_REENVIAR).withArguments(dlqArgs()).build(); }
    @Bean public Queue ncReenviarQueue()          { return QueueBuilder.durable(QUEUE_NC_REENVIAR).withArguments(dlqArgs()).build(); }
    @Bean public Queue retiroSolicitadoQueue()    { return QueueBuilder.durable(QUEUE_RETIRO_SOLICITADO).withArguments(dlqArgs()).build(); }

    // ── Bindings ───────────────────────────────────────────────────────────

    @Bean public Binding citaCreadaBinding(Queue citaCreadaQueue, TopicExchange citasExchange)                { return BindingBuilder.bind(citaCreadaQueue).to(citasExchange).with(RK_CITA_CREADA); }
    @Bean public Binding citaCanceladaBinding(Queue citaCanceladaQueue, TopicExchange citasExchange)          { return BindingBuilder.bind(citaCanceladaQueue).to(citasExchange).with(RK_CITA_CANCELADA); }
    @Bean public Binding citaReagendadaBinding(Queue citaReagendadaQueue, TopicExchange citasExchange)        { return BindingBuilder.bind(citaReagendadaQueue).to(citasExchange).with(RK_CITA_REAGENDADA); }
    @Bean public Binding pagoConfirmadoBinding(Queue pagoConfirmadoQueue, TopicExchange cajaExchange)         { return BindingBuilder.bind(pagoConfirmadoQueue).to(cajaExchange).with(RK_PAGO_CONFIRMADO); }
    @Bean public Binding episodioAtendidoBinding(Queue episodioAtendidoQueue, TopicExchange atencionExchange) { return BindingBuilder.bind(episodioAtendidoQueue).to(atencionExchange).with(RK_EPISODIO_ATENDIDO); }
    @Bean public Binding comprobanteReenviarBinding(Queue comprobanteReenviarQueue, TopicExchange cajaExchange){ return BindingBuilder.bind(comprobanteReenviarQueue).to(cajaExchange).with(RK_COMPROBANTE_REENVIAR); }
    @Bean public Binding ncReenviarBinding(Queue ncReenviarQueue, TopicExchange cajaExchange)                 { return BindingBuilder.bind(ncReenviarQueue).to(cajaExchange).with(RK_NC_REENVIAR); }
    @Bean public Binding retiroSolicitadoBinding(Queue retiroSolicitadoQueue, TopicExchange cajaExchange)     { return BindingBuilder.bind(retiroSolicitadoQueue).to(cajaExchange).with(RK_RETIRO_SOLICITADO); }

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
