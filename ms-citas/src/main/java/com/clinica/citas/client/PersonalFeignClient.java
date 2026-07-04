package com.clinica.citas.client;

import com.clinica.citas.client.dto.PersonalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-personal")
public interface PersonalFeignClient {

    @GetMapping("/personal/{id}/habilitado")
    ResponseEntity<Boolean> verificarHabilitado(@PathVariable Long id);

    // Usado para embeber nombre del médico y especialidad en el evento CitaCreada
    @GetMapping("/personal/{id}")
    ResponseEntity<PersonalDTO> obtenerPersonal(@PathVariable Long id);
}
