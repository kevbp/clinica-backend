package com.clinica.notificaciones.exception;

import jakarta.mail.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDbDown(DataAccessException ex) {
        log.error("Base de datos no disponible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("mensaje",
                        "El servicio no se encuentra disponible en este momento, " +
                        "por favor intente nuevamente en unos segundos."));
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<Map<String, String>> handleMail(MailException ex) {
        log.warn("Fallo al enviar correo de prueba: {}", ex.getMessage());
        String causaRaiz = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        boolean credencialesInvalidas = ex.getMostSpecificCause() instanceof AuthenticationFailedException;
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("mensaje", credencialesInvalidas
                        ? "El servidor SMTP rechazó las credenciales. Verifique usuario y password."
                        : "No se pudo conectar al servidor SMTP: " + causaRaiz));
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

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        log.warn("Error de negocio [{}]: {}", ex.getStatusCode(), ex.getReason());
        String mensaje = ex.getReason() != null && !ex.getReason().isBlank()
                ? ex.getReason()
                : "Error en la operación.";
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("mensaje", mensaje));
    }
}
