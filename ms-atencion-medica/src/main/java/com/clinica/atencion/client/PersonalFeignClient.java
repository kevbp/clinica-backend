package com.clinica.atencion.client;

import com.clinica.atencion.client.dto.PersonalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-personal")
public interface PersonalFeignClient {

    @GetMapping("/personal/{id}")
    ResponseEntity<PersonalDTO> obtenerPersonal(@PathVariable Long id);
}
