package com.clinica.caja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CajaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CajaApplication.class, args);
    }
}
