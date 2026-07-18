package com.clinica.citas.filter;

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

    private static final Logger log    = LoggerFactory.getLogger(CorrelationIdFilter.class);
    public  static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String MODULO = "CITAS";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cid = request.getHeader(CORRELATION_HEADER);
        if (cid == null || cid.isBlank()) cid = UUID.randomUUID().toString();
        MDC.put("correlationId", cid);
        MDC.put("usuarioId", extractSub(request.getHeader("Authorization")));
        response.setHeader(CORRELATION_HEADER, cid);

        final long   start  = System.currentTimeMillis();
        final String method = request.getMethod();
        final String path   = request.getRequestURI();

        try {
            chain.doFilter(request, response);
        } finally {
            long latencia = System.currentTimeMillis() - start;
            int  status   = response.getStatus();

            MDC.put("logType",    "ARCH_LOG");
            MDC.put("httpMethod", method);
            MDC.put("httpPath",   path);
            MDC.put("httpStatus", String.valueOf(status));
            MDC.put("latenciaMs", String.valueOf(latencia));
            log.info("ARCH_LOG modulo={} method={} path={} status={} latenciaMs={}ms usuario={}",
                    MODULO, method, path, status, latencia, MDC.get("usuarioId"));
            MDC.clear();
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
            int s = idx + 7, e = payload.indexOf('"', s);
            return e > s ? payload.substring(s, e) : "anonimo";
        } catch (Exception e) { return "anonimo"; }
    }
}