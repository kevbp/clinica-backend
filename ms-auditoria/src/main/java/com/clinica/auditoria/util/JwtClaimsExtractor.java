package com.clinica.auditoria.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * Extrae claims del JWT sin revalidar la firma — el api-gateway ya lo validó.
 * Un JWT es: base64url(header).base64url(payload).base64url(signature)
 * Solo necesitamos decodificar el payload (parte del medio).
 */
@Slf4j
@UtilityClass
public class JwtClaimsExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String extractUserId(String authHeader) {
        JsonNode payload = decodePayload(authHeader);
        return payload != null ? payload.path("sub").asText(null) : null;
    }

    public static String extractRol(String authHeader) {
        JsonNode payload = decodePayload(authHeader);
        if (payload == null) return null;

        // Keycloak expone los roles en realm_access.roles (lista)
        JsonNode roles = payload.path("realm_access").path("roles");
        if (roles.isArray() && roles.size() > 0) {
            // Prioridad: rol clínico definido en el sistema antes que roles de Keycloak internos
            for (JsonNode role : roles) {
                String r = role.asText();
                if (!r.startsWith("default-roles") && !r.equals("offline_access")
                        && !r.equals("uma_authorization")) {
                    return r;
                }
            }
        }
        return null;
    }

    private static JsonNode decodePayload(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return MAPPER.readTree(decoded);
        } catch (Exception e) {
            log.warn("No se pudo decodificar el JWT: {}", e.getMessage());
            return null;
        }
    }
}
