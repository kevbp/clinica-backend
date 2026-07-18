package com.clinica.historias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HistoriasClinicasApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistoriasClinicasApplication.class, args);
    }
}
