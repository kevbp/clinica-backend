package com.clinica.atencion.client;

import com.clinica.atencion.client.dto.CitaMedicaDTO;
import com.clinica.atencion.client.dto.EstadoCitaUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-citas")
public interface CitasFeignClient {

    @GetMapping("/citas/{id}")
    ResponseEntity<CitaMedicaDTO> obtenerCita(@PathVariable Long id);

    @PatchMapping("/citas/{id}/estado")
    ResponseEntity<CitaMedicaDTO> actualizarEstado(@PathVariable Long id,
                                                    @RequestBody EstadoCitaUpdateDTO request);
}
