package com.clinica.caja.client;

import com.clinica.caja.client.dto.PersonalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-personal")
public interface PersonalFeignClient {

    // Lee idEspecialidad del médico para calcular TarifaConsulta
    @GetMapping("/personal/{id}")
    ResponseEntity<PersonalDTO> obtenerPersonal(@PathVariable Long id);
}
