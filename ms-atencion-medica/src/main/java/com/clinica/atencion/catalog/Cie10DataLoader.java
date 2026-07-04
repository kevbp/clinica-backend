package com.clinica.atencion.catalog;

import com.clinica.atencion.catalog.repository.Cie10Repository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Cie10DataLoader {

    private final Cie10Repository repository;

    @PostConstruct
    public void cargarSiVacio() {
        if (repository.count() > 0) return;

        log.info("Cargando catálogo CIE-10...");
        List<Cie10> registros = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("cie10.csv").getInputStream(),
                        StandardCharsets.UTF_8))) {

            String linea;
            boolean primera = true;
            while ((linea = reader.readLine()) != null) {
                if (primera) { primera = false; continue; } // saltar cabecera
                String[] partes = linea.split(",", 3);
                if (partes.length < 2) continue;
                String codigo     = partes[0].trim();
                String descripcion = partes[1].trim();
                String categoria  = partes.length > 2 ? partes[2].trim() : null;
                registros.add(new Cie10(codigo, descripcion, categoria));
            }

            repository.saveAll(registros);
            log.info("Catálogo CIE-10 cargado: {} registros", registros.size());

        } catch (Exception ex) {
            log.error("Error al cargar catálogo CIE-10: {}", ex.getMessage());
        }
    }
}
