package com.clinica.laboratorio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LaboratorioApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaboratorioApplication.class, args);
    }
}
