package com.clinica.citas.client;

import com.clinica.citas.client.dto.ProgramacionHorarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "ms-horarios")
public interface HorariosFeignClient {

    @GetMapping("/programacion-horarios/personal/{idPersonal}")
    ResponseEntity<List<ProgramacionHorarioDTO>> getHorariosPorPersonal(@PathVariable Long idPersonal);
}
