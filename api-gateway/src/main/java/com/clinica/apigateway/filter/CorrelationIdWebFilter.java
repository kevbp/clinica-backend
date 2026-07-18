package com.clinica.apigateway.filter;

import com.clinica.apigateway.client.AuditoriaWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@Order(-1)
public class CorrelationIdWebFilter implements WebFilter {

    private static final Logger      log                = LoggerFactory.getLogger(CorrelationIdWebFilter.class);
    public  static final String      CORRELATION_HEADER = "X-Correlation-ID";
    private static final Set<String> METODOS_AUDITABLES = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditoriaWebClient auditoriaClient;

    public CorrelationIdWebFilter(AuditoriaWebClient auditoriaClient) {
        this.auditoriaClient = auditoriaClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String  cid       = correlationId;
        final long    start     = System.currentTimeMillis();
        final String  method    = exchange.getRequest().getMethod().name();
        final String  path      = exchange.getRequest().getPath().value();
        final String  ip        = obtenerIp(exchange);
        // Solo auditar operaciones que modifican estado; GETs de navegación generan ruido innecesario
        final boolean auditable = METODOS_AUDITABLES.contains(method);

        MDC.put("logType", "ARCH_LOG");
        MDC.put("event", "ENTRY");
        MDC.put("correlationId", cid);
        log.info("ARCH_LOG ENTRY method={} path={} correlationId={} origen={}", method, path, cid, ip);
        MDC.remove("event");
        MDC.remove("logType");
        MDC.remove("correlationId");

        if (auditable) {
            auditoriaClient.registrar(buildBody("REQUEST_IN", "EXITO", cid, method, path, null, null, ip, start));
        }

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.header(CORRELATION_HEADER, cid))
                .build();
        mutated.getResponse().getHeaders().add(CORRELATION_HEADER, cid);

        return chain.filter(mutated).doFinally(signal -> {
            long    latencia  = System.currentTimeMillis() - start;
            Integer status    = mutated.getResponse().getStatusCode() != null
                              ? mutated.getResponse().getStatusCode().value() : 0;
            String  resultado = status < 400 ? "EXITO" : "ERROR";

            MDC.put("logType", "ARCH_LOG");
            MDC.put("event", "EXIT");
            MDC.put("correlationId", cid);
            MDC.put("httpStatus", String.valueOf(status));
            MDC.put("latenciaMs", String.valueOf(latencia));
            log.info("ARCH_LOG EXIT method={} path={} status={} latenciaMs={}ms", method, path, status, latencia);
            MDC.clear();

            if (auditable) {
                auditoriaClient.registrar(buildBody("RESPONSE_OUT", resultado, cid, method, path, status, latencia, null, start + latencia));
            }
        });
    }

    private Map<String, Object> buildBody(String accion, String resultado, String cid,
                                           String method, String path,
                                           Integer status, Long duracion, String origen,
                                           long timestampMs) {
        Map<String, Object> body = new HashMap<>();
        body.put("logType",       "ARCH_LOG");
        body.put("modulo",        "GATEWAY");
        body.put("accion",        accion);
        body.put("resultado",     resultado);
        body.put("correlationId", cid);
        body.put("httpMethod",    method);
        body.put("httpPath",      path);
        body.put("timestamp",     timestampMs);
        if (status   != null) body.put("httpStatus",  status);
        if (duracion != null) body.put("duracionMs",  duracion);
        if (origen   != null) body.put("origen",      origen);
        return body;
    }

    private String obtenerIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return exchange.getRequest().getRemoteAddress() != null
             ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
             : "unknown";
    }
}
