package com.clinica.atencion.catalog.controller;

import com.clinica.atencion.catalog.Cie10;
import com.clinica.atencion.catalog.repository.Cie10Repository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "CIE-10", description = "Búsqueda en el catálogo de códigos CIE-10")
@RestController
@RequestMapping("/atenciones/cie10")
@RequiredArgsConstructor
public class Cie10Controller {

    private final Cie10Repository repository;

    @Operation(summary = "Buscar código CIE-10",
               description = "Busca por código (prefijo) o descripción (contiene). Devuelve hasta 30 resultados.")
    @GetMapping
    public ResponseEntity<List<Cie10>> buscar(
            @RequestParam(required = false, defaultValue = "") String q) {
        if (q.trim().length() < 2) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(repository.buscar(q.trim()));
    }
}
