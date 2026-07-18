package com.clinica.caja.client;

import com.clinica.caja.client.dto.OrdenDTO;
import com.clinica.caja.client.dto.RecetaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-historias-clinicas")
public interface HistoriasFeignClient {

    @GetMapping("/recetas/{idReceta}")
    ResponseEntity<RecetaDTO> obtenerRecetaPorId(@PathVariable String idReceta);

    @GetMapping("/recetas/paciente/{idPaciente}")
    ResponseEntity<List<RecetaDTO>> obtenerRecetasPorPaciente(@PathVariable Long idPaciente);

    @GetMapping("/ordenes/{idOrden}")
    ResponseEntity<OrdenDTO> obtenerOrdenPorId(@PathVariable String idOrden);

    @GetMapping("/ordenes/paciente/{idPaciente}")
    ResponseEntity<List<OrdenDTO>> obtenerOrdenesPorPaciente(@PathVariable Long idPaciente);
}
