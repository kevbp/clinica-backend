package com.clinica.caja.client;

import com.clinica.caja.client.dto.EstadoCitaUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-citas")
public interface CitasFeignClient {

    @PatchMapping("/citas/{id}/estado")
    ResponseEntity<Void> actualizarEstado(@PathVariable Long id,
                                          @RequestBody EstadoCitaUpdateDTO request);

    @PostMapping("/citas/{id}/compensar-pago-fallido")
    ResponseEntity<Void> compensarPagoFallido(@PathVariable Long id);
}
