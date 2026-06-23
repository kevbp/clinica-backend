package com.clinica.citas.exception;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<Map<String, String>> handleDbDown(CannotCreateTransactionException ex) {
        log.error("Base de datos no disponible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("mensaje",
                        "El servicio no se encuentra disponible en este momento, " +
                        "por favor intente nuevamente en unos segundos."));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handleFeign(FeignException ex) {
        // Errores de negocio del servicio destino (4xx) se propagan con su status original
        if (ex.status() >= 400 && ex.status() < 500) {
            log.warn("Error de negocio desde dependencia Feign ({}): {}", ex.status(), ex.getMessage());
            HttpStatus status = HttpStatus.resolve(ex.status());
            return ResponseEntity.status(status != null ? status : HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", ex.contentUTF8().isBlank()
                            ? "Error en la validación con el servicio dependiente."
                            : ex.contentUTF8()));
        }
        // Infraestructura caída (5xx o sin respuesta)
        log.error("Dependencia no disponible (status={}): {}", ex.status(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("mensaje",
                        "Un servicio del que depende esta operación no se encuentra disponible. " +
                        "Intente nuevamente más tarde."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de restricción: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", "Ya existe un registro con los mismos datos únicos."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "valor inválido"
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
}
