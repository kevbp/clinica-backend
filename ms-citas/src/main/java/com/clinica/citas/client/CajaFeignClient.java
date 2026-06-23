package com.clinica.citas.client;

import com.clinica.citas.client.dto.NotaCreditoRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-caja")
public interface CajaFeignClient {

    @PostMapping("/notas-credito")
    ResponseEntity<Void> emitirNotaCredito(@RequestBody NotaCreditoRequestDTO request);
}
