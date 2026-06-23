package com.clinica.citas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-personal")
public interface PersonalFeignClient {

    @GetMapping("/personal/{id}/habilitado")
    ResponseEntity<Boolean> verificarHabilitado(@PathVariable Long id);
}
