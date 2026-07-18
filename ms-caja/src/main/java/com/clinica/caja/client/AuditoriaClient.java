package com.clinica.caja.client;

import com.clinica.caja.dto.AccionAuditoriaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ms-auditoria", path = "/auditoria")
public interface AuditoriaClient {

    @PostMapping("/acciones")
    void registrar(@RequestBody AccionAuditoriaDTO dto,
                   @RequestHeader("Authorization") String authHeader);
}
