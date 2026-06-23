package com.clinica.atencion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AtencionMedicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtencionMedicaApplication.class, args);
    }
}
