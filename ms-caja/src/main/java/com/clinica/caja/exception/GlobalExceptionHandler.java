package com.clinica.caja.exception;

import com.clinica.caja.client.AuditoriaClient;
import com.clinica.caja.dto.AccionAuditoriaDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final AuditoriaClient auditoriaClient;

    @ExceptionHandler(org.springframework.transaction.CannotCreateTransactionException.class)
    public ResponseEntity<Map<String, String>> handleDbDown(Exception ex) {
        log.error("Base de datos no disponible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("mensaje",
                        "El servicio no se encuentra disponible en este momento, " +
                        "por favor intente nuevamente en unos segundos."));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handleFeign(FeignException ex) {
        String msDestino = extraerMsDestino(ex);
        String cid       = MDC.get("correlationId");
        int    httpSt    = ex.status();
        String errMsg    = ex.getMessage() != null
                ? ex.getMessage().replace("\"", "'").substring(0, Math.min(200, ex.getMessage().length()))
                : "sin detalle";

        final String cidFinal = cid;
        final String msFinal  = msDestino;
        final String errFinal = errMsg;
        CompletableFuture.runAsync(() -> {
            try {
                auditoriaClient.registrar(AccionAuditoriaDTO.builder()
                        .logType("ARCH_LOG")
                        .modulo("CAJA")
                        .accion("SERVICE_ERR")
                        .resultado("ERROR")
                        .correlationId(cidFinal)
                        .origen(msFinal)
                        .metadatos("{\"msDestino\":\"" + msFinal
                                + "\",\"httpStatus\":" + httpSt
                                + ",\"error\":\"" + errFinal + "\"}")
                        .build(), null);
            } catch (Exception ignored) { }
        });

        if (ex.status() >= 400 && ex.status() < 500) {
            log.warn("SERVICE_ERR 4xx desde {} ({}): {}", msDestino, ex.status(), ex.getMessage());
            HttpStatus status = HttpStatus.resolve(ex.status());
            return ResponseEntity.status(status != null ? status : HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", ex.contentUTF8().isBlank()
                            ? "Error en la validacion con el servicio dependiente."
                            : ex.contentUTF8()));
        }
        log.error("SERVICE_ERR 5xx desde {} ({}): {}", msDestino, ex.status(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("mensaje",
                        "Un servicio del que depende esta operacion no se encuentra disponible. " +
                        "Intente nuevamente mas tarde."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violacion de restriccion: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "Ya existe un registro con los mismos datos unicos."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "valor invalido"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        log.warn("Error de negocio [{}]: {}", ex.getStatusCode(), ex.getReason());
        String mensaje = ex.getReason() != null && !ex.getReason().isBlank()
                ? ex.getReason() : "Error en la operacion.";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("mensaje", mensaje));
    }

    private String extraerMsDestino(FeignException ex) {
        try { return java.net.URI.create(ex.request().url()).getHost(); }
        catch (Exception e) { return "desconocido"; }
    }
}
