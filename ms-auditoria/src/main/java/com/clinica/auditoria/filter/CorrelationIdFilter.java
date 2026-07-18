package com.clinica.auditoria.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    public static final String CORRELATION_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cid = request.getHeader(CORRELATION_HEADER);
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", cid);
        MDC.put("usuarioId", extractSub(request.getHeader("Authorization")));
        response.setHeader(CORRELATION_HEADER, cid);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long latencia = System.currentTimeMillis() - start;
            MDC.put("logType", "ARCH_LOG");
            MDC.put("httpMethod", request.getMethod());
            MDC.put("httpPath", request.getRequestURI());
            MDC.put("httpStatus", String.valueOf(response.getStatus()));
            MDC.put("latenciaMs", String.valueOf(latencia));
            log.info("ARCH_LOG method={} path={} status={} latenciaMs={}ms usuario={}",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), latencia, MDC.get("usuarioId"));
            MDC.remove("logType");
            MDC.remove("httpMethod");
            MDC.remove("httpPath");
            MDC.remove("httpStatus");
            MDC.remove("latenciaMs");
            MDC.remove("correlationId");
            MDC.remove("usuarioId");
        }
    }

    private String extractSub(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return "anonimo";
            String[] parts = authHeader.substring(7).split("\\.");
            if (parts.length < 2) return "anonimo";
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            int idx = payload.indexOf("\"sub\":\"");
            if (idx == -1) return "anonimo";
            int start = idx + 7;
            int end   = payload.indexOf('"', start);
            return end > start ? payload.substring(start, end) : "anonimo";
        } catch (Exception e) {
            return "anonimo";
        }
    }
}
