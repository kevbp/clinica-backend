package com.clinica.apigateway.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Cliente reactivo no-bloqueante hacia ms-auditoria.
 * Fire-and-forget: nunca añade latencia al flujo del Gateway.
 */
@Component
public class AuditoriaWebClient {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaWebClient.class);

    private final WebClient webClient;

    public AuditoriaWebClient(
            @Value("${auditoria.url:http://ms-auditoria:8091}") String auditoriaUrl) {
        this.webClient = WebClient.builder().baseUrl(auditoriaUrl).build();
    }

    public void registrar(Map<String, Object> body) {
        webClient.post()
                .uri("/auditoria/acciones")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    r  -> { /* ok */ },
                    ex -> log.debug("Auditoria ARCH_LOG no registrado: {}", ex.getMessage())
                );
    }
}
