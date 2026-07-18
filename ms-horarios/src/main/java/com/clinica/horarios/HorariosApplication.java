package com.clinica.horarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HorariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(HorariosApplication.class, args);
    }
}
