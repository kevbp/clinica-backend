package com.clinica.atencion.exception;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Redis no disponible: Spring lanza RedisConnectionFailureException (RuntimeException)
    @ExceptionHandler(org.springframework.data.redis.RedisConnectionFailureException.class)
    public ResponseEntity<Map<String, String>> handleRedisDown(Exception ex) {
        log.error("Redis no disponible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("mensaje",
                        "El servicio no se encuentra disponible en este momento, " +
                        "por favor intente nuevamente en unos segundos."));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handleFeign(FeignException ex) {
        if (ex.status() >= 400 && ex.status() < 500) {
            HttpStatus status = HttpStatus.resolve(ex.status());
            return ResponseEntity.status(status != null ? status : HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", ex.contentUTF8().isBlank()
                            ? "Error en la validación con el servicio dependiente."
                            : ex.contentUTF8()));
        }
        log.error("Dependencia no disponible (status={}): {}", ex.status(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("mensaje",
                        "Un servicio del que depende esta operación no se encuentra disponible. " +
                        "Intente nuevamente más tarde."));
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
