package com.clinica.notificaciones.controller;

import com.clinica.notificaciones.dto.ConfiguracionSmtpRequestDTO;
import com.clinica.notificaciones.dto.ConfiguracionSmtpResponseDTO;
import com.clinica.notificaciones.dto.ProbarConexionRequestDTO;
import com.clinica.notificaciones.service.ConfiguracionSmtpService;
import com.clinica.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Configuración SMTP", description = "Configuración del servidor de correo usado para notificaciones. Solo ADMIN.")
@RestController
@RequestMapping("/notificaciones/configuracion/smtp")
@RequiredArgsConstructor
public class ConfiguracionSmtpController {

    private final ConfiguracionSmtpService configuracionSmtpService;
    private final NotificacionService notificacionService;

    @Operation(summary = "Obtener la configuración SMTP activa",
            description = "El password nunca se devuelve en texto plano; solo se indica si ya hay uno guardado.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Configuración actual") })
    @GetMapping
    public ResponseEntity<ConfiguracionSmtpResponseDTO> obtener() {
        return ResponseEntity.ok(configuracionSmtpService.obtener());
    }

    @Operation(summary = "Actualizar la configuración SMTP",
            description = "El password se cifra (AES/GCM) antes de persistirse. Si se omite o llega vacío, se conserva el actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuración actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PutMapping
    public ResponseEntity<ConfiguracionSmtpResponseDTO> actualizar(@Valid @RequestBody ConfiguracionSmtpRequestDTO request) {
        return ResponseEntity.ok(configuracionSmtpService.actualizar(request));
    }

    @Operation(summary = "Enviar un correo de prueba con la configuración guardada",
            description = "Usa la configuración SMTP persistida (no la del request) para validar que las credenciales funcionan.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo de prueba enviado"),
            @ApiResponse(responseCode = "412", description = "La configuración SMTP está incompleta"),
            @ApiResponse(responseCode = "502", description = "El servidor SMTP rechazó la conexión o las credenciales")
    })
    @PostMapping("/probar")
    public ResponseEntity<Void> probar(@Valid @RequestBody ProbarConexionRequestDTO request) {
        notificacionService.enviarPrueba(request.getCorreoDestino());
        return ResponseEntity.ok().build();
    }
}
