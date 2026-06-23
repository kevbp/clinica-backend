package com.clinica.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }
}
